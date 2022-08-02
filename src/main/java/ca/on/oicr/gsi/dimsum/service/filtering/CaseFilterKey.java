package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.function.Function;
import java.util.function.Predicate;

import ca.on.oicr.gsi.dimsum.data.Case;

public enum CaseFilterKey {

  // @formatter:off
  ASSAY(string -> kase -> kase.getAssayName().startsWith(string)
      || kase.getAssayDescription().startsWith(string)),
  CASE_ID(string -> kase -> kase.getId().equals(string)),
  DONOR(string -> kase -> kase.getDonor().getName().startsWith(string)
      || kase.getDonor().getExternalName().contains(string)),
  PENDING(string -> {
    PendingState state = PendingState.getByLabel(string);
    if (state == null) {
      throw new IllegalArgumentException(String.format("Invalid pending state: %s", string));
    }
    return state.predicate();
  }),
  PIPELINE(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getPipeline().equals(string))),
  PROJECT(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getName().equals(string))),
  REQUISITION(string -> kase -> kase.getRequisitions().stream()
      .anyMatch(req -> req.getName().startsWith(string))),
  REQUISITION_ID(string -> kase -> kase.getRequisitions().stream()
      .anyMatch(req -> req.getId() == Long.parseLong(string)));
  // @formatter:on

  private final Function<String, Predicate<Case>> create;

  private CaseFilterKey(Function<String, Predicate<Case>> create) {
    this.create = create;
  }

  public Function<String, Predicate<Case>> create() {
    return create;
  }

}
