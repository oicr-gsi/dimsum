package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.function.Function;
import java.util.function.Predicate;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.MetricCategory;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;
import ca.on.oicr.gsi.dimsum.data.TestTableView;

public enum CaseFilterKey {

  // @formatter:off
  ASSAY(string -> kase -> kase.getAssay().getName().toLowerCase().startsWith(string.toLowerCase())
      || kase.getAssay().getDescription().toLowerCase().startsWith(string.toLowerCase())),
  CASE_ID(string -> kase -> kase.getId().toLowerCase().equals(string.toLowerCase())),
  DONOR(string -> kase -> kase.getDonor().getName().toLowerCase().startsWith(string.toLowerCase())
      || kase.getDonor().getExternalName().toLowerCase().contains(string.toLowerCase())),
  PENDING(string -> {
    PendingState state = getState(string);
    Predicate<Case> notStopped = kase -> !kase.getRequisition().isStopped();
    return notStopped.and(state.predicate());
  }) {
    @Override
    public Function<String, Predicate<Test>> testPredicate() {
      return string -> getState(string).testPredicate();
    }

    @Override
    public Function<String, Predicate<Sample>> samplePredicate(MetricCategory requestCategory) {
      return string -> getState(string).samplePredicate(requestCategory);
    }

    @Override
    public Function<String, Predicate<Requisition>> requisitionPredicate() {
      return string -> getState(string).requisitionPredicate();
    }

    @Override
    public Function<String, Predicate<TestTableView>> testTableViewPredicate() {
      return string -> getState(string).testTableViewPredicate();
    }
  },
  PIPELINE(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getPipeline().equals(string))),
  PROJECT(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getName().equalsIgnoreCase(string))),
  REQUISITION(string -> kase -> kase.getRequisition().getName().toLowerCase().startsWith(string.toLowerCase())),
  REQUISITION_ID(string -> kase -> kase.getRequisition().getId() == Long.parseLong(string)),
  TEST(string -> kase -> kase.getTests().stream().anyMatch(test -> test.getName().equalsIgnoreCase(string)));
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

  public Function<String, Predicate<Requisition>> requisitionPredicate() {
    return string -> requisition -> true;
  }

  public Function<String, Predicate<TestTableView>> testTableViewPredicate() {
    return string -> testTableViewPredicate -> true;
  }

  private static PendingState getState(String label) {
    PendingState state = PendingState.getByLabel(label);
    if (state == null) {
      throw new IllegalArgumentException(String.format("Invalid pending state: %s", label));
    }
    return state;
  }

}
