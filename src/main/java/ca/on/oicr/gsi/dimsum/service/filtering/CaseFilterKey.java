package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.data.TestTableView;

public enum CaseFilterKey {

  // @formatter:off
  ASSAY(string -> kase -> kase.getAssayName().toLowerCase().startsWith(string.toLowerCase())
      || kase.getAssayDescription().toLowerCase().startsWith(string.toLowerCase())),
  CASE_ID(string -> kase -> kase.getId().toLowerCase().equals(string.toLowerCase())),
  DONOR(string -> kase -> kase.getDonor().getName().toLowerCase().startsWith(string.toLowerCase())
      || kase.getDonor().getExternalName().toLowerCase().contains(string.toLowerCase())),
  PENDING(string -> {
    PendingState state = getState(string);
    Predicate<Case> notStoppedOrPaused = kase ->
        (!state.isStoppable() || !kase.getRequisition().isStopped())
        && !kase.getRequisition().isPaused();
    return notStoppedOrPaused.and(state.predicate());
  }) {
    @Override
    public Function<String, Predicate<Test>> testPredicate() {
      return string -> getState(string).testPredicate();
    }

    @Override
    public Function<String, Predicate<Sample>> samplePredicate(MetricCategory requestCategory) {
      return string -> getState(string).samplePredicate(requestCategory);
    }
  },
  PIPELINE(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getPipeline().equals(string))),
  PROJECT(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getName().equalsIgnoreCase(string))),
  REQUISITION(string -> kase -> kase.getRequisition().getName().toLowerCase().startsWith(string.toLowerCase())),
  REQUISITION_ID(string -> kase -> kase.getRequisition().getId() == Long.parseLong(string)),
  TEST(string -> {
    return kase -> kase.getTests().stream().anyMatch(test -> test.getName().equalsIgnoreCase(string));
  }) {
    @Override 
    public Function<String, Predicate<Test>> testPredicate() {
      return string -> test -> test.getName().equalsIgnoreCase(string);
    }
  },
  STOPPED(string -> kase -> ("Yes".equals(string)) ? kase.getRequisition().isStopped() : !kase.getRequisition().isStopped()),
  PAUSED(string -> kase -> ("Yes".equals(string)) ? kase.getRequisition().isPaused() : !kase.getRequisition().isPaused()),
  COMPLETED(string -> {
    CompletedGate gate = getGate(string);
    return gate.predicate();
  }) {
    @Override
    public Function<String, Predicate<Test>> testPredicate() {
      return string -> getGate(string).testPredicate();
    }

    @Override
    public Function<String, Predicate<Sample>> samplePredicate(MetricCategory requestCategory) {
      return string -> getGate(string).samplePredicate(requestCategory);
    }
  },
  INCOMPLETE(string -> {
    CompletedGate gate = getGate(string);
    return gate.predicate().negate(); // Negate the completed condition
}) {
    @Override
    public Function<String, Predicate<Test>> testPredicate() {
        return string -> getGate(string).testPredicate().negate();
    }

    @Override
    public Function<String, Predicate<Sample>> samplePredicate(MetricCategory requestCategory) {
        return string -> getGate(string).samplePredicate(requestCategory).negate();
    }
},
  LIBRARY_DESIGN(string -> {return kase -> kase.getTests().stream().anyMatch(test -> test.getLibraryDesignCode().equals(string));
  }) {
      @Override 
      public Function<String, Predicate<Test>> testPredicate() {
        return string -> test -> Objects.equals(test.getLibraryDesignCode(), string);
    }
  };
  // @formatter:on

  private final Function<String, Predicate<Case>> create;

  private CaseFilterKey(Function<String, Predicate<Case>> create) {
    this.create = create;
  }

  public Function<String, Predicate<Case>> create() {
    return create;
  }

  public Function<String, Predicate<Test>> testPredicate() {
    return string -> test -> true;
  }

  public Function<String, Predicate<Sample>> samplePredicate(MetricCategory requestCategory) {
    return string -> sample -> true;
  }

  public Function<String, Predicate<TestTableView>> testTableViewPredicate() {
    return string -> view -> testPredicate().apply(string).test(view.getTest());
  }

  private static PendingState getState(String label) {
    PendingState state = PendingState.getByLabel(label);
    if (state == null) {
      throw new IllegalArgumentException(String.format("Invalid pending state: %s", label));
    }
    return state;
  }

  private static CompletedGate getGate(String label) {
    CompletedGate gate = CompletedGate.getByLabel(label);
    if (gate == null) {
      throw new IllegalArgumentException(String.format("Invalid gate: %s", label));
    }
    return gate;
  }

}
