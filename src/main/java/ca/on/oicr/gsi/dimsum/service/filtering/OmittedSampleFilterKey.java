package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.function.Function;
import java.util.function.Predicate;
import ca.on.oicr.gsi.cardea.data.OmittedSample;

public enum OmittedSampleFilterKey {

  // @formatter:off
  DONOR(string -> sample -> 
      sample.getDonor().getName().toLowerCase().startsWith(string.toLowerCase())
      || sample.getDonor().getExternalName().toLowerCase().contains(string.toLowerCase())),
  PROJECT(string -> sample -> sample.getProject().equalsIgnoreCase(string)),
  REQUISITION(string -> sample -> sample.getRequisitionName() != null
      && sample.getRequisitionName().toLowerCase().startsWith(string.toLowerCase()));
  // @formatter:on

  private final Function<String, Predicate<OmittedSample>> create;

  private OmittedSampleFilterKey(Function<String, Predicate<OmittedSample>> create) {
    this.create = create;
  }

  public Function<String, Predicate<OmittedSample>> create() {
    return create;
  }

}
