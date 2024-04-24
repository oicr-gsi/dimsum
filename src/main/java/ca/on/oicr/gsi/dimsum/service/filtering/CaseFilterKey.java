package ca.on.oicr.gsi.dimsum.service.filtering;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
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
  PENDING_RELEASE_DELIVERABLE(string -> kase -> {
    for (CaseDeliverable caseDeliverable : kase.getDeliverables()) {
      if (!Boolean.TRUE.equals(caseDeliverable.getReleaseApprovalQcPassed())) {
        // No releases pending for this deliverable type
        continue;
      }
      for (CaseRelease release : caseDeliverable.getReleases()) {
        if (Objects.equals(string, release.getDeliverable())) {
          return !Boolean.TRUE.equals(release.getQcPassed());
        }
      }
    }
    return false;
  }),
  PIPELINE(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getPipeline().equals(string))),
  PROJECT(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getName().equalsIgnoreCase(string))) {
    @Override
    public Function<String, Predicate<Sample>> samplePredicate(MetricCategory requestCategory) {
      return string -> sample -> sample.getProject().equalsIgnoreCase(string);
    }
  },
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
    Predicate<Case> applicable = x -> gate.isApplicable(x);
    return applicable.and(gate.predicate());
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
    Predicate<Case> applicable = x -> gate.isApplicable(x);
    return applicable.and(gate.predicate().negate()); // Negate the completed condition
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

    @Override
    public Function<String, Predicate<Sample>> samplePredicate(MetricCategory requestCategory) {
      return string -> sample -> Objects.equals(sample.getLibraryDesignCode(), string);
    }
  },
  STARTED_BEFORE(string -> kase -> kase.getStartDate() != null && kase.getStartDate().isBefore(LocalDate.parse(string))),
  STARTED_AFTER(string -> kase -> kase.getStartDate() != null && kase.getStartDate().isAfter(LocalDate.parse(string))),
  COMPLETED_BEFORE(string -> kase -> {
      LocalDate completionDate = getCompletionDate(kase);
      return completionDate != null && completionDate.isBefore(LocalDate.parse(string));
  }),
  COMPLETED_AFTER(string -> kase -> {
      LocalDate completionDate = getCompletionDate(kase);
      return completionDate != null && completionDate.isAfter(LocalDate.parse(string));
  });
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

  private static LocalDate getCompletionDate(Case x) {
    if (x.isStopped()) {
      return null;
    }
    List<CaseDeliverable> deliverables = x.getDeliverables();
    if (deliverables.isEmpty()) {
      return null;
    }
    List<CaseRelease> releases = deliverables.stream()
        .flatMap(deliverable -> deliverable.getReleases().stream())
        .collect(Collectors.toList());
    if (releases.isEmpty()) {
      return null;
    }
    if (releases.stream()
        .anyMatch(release -> release.getQcPassed() == null || !release.getQcPassed())) {
      return null;
    }
    return releases.stream()
        .map(CaseRelease::getQcDate)
        .max(LocalDate::compareTo)
        .orElse(null);
  }
}
