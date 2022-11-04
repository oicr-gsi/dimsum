package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.function.Function;
import java.util.function.Predicate;

import ca.on.oicr.gsi.dimsum.data.Case;

public enum CaseFilterKey {

  // @formatter:off
  ASSAY(string -> kase -> kase.getAssay().getName().toLowerCase().startsWith(string.toLowerCase())
      || kase.getAssay().getDescription().toLowerCase().startsWith(string.toLowerCase())),
  CASE_ID(string -> kase -> kase.getId().toLowerCase().equals(string.toLowerCase())),
  DONOR(string -> kase -> kase.getDonor().getName().toLowerCase().startsWith(string.toLowerCase())
      || kase.getDonor().getExternalName().toLowerCase().contains(string.toLowerCase())),
  PENDING(string -> {
    PendingState state = PendingState.getByLabel(string);
    if (state == null) {
      throw new IllegalArgumentException(String.format("Invalid pending state: %s", string));
    }
    Predicate<Case> notStopped = kase -> !kase.isStopped();
    return notStopped.and(state.predicate());
  }),
  PIPELINE(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getPipeline().equals(string))),
  PROJECT(string -> kase -> kase.getProjects().stream()
      .anyMatch(project -> project.getName().equalsIgnoreCase(string))),
  REQUISITION(string -> kase -> kase.getRequisitions().stream()
      .anyMatch(req -> req.getName().toLowerCase().startsWith(string.toLowerCase()))),
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
