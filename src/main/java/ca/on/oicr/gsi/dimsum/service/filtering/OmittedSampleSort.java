package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.OmittedSample;

public enum OmittedSampleSort {

  // @formatter:off
  NAME("Name", Comparator.comparing(OmittedSample::getName)),
  DONOR("Donor", Comparator.comparing(sample -> sample.getDonor().getName())),
  PROJECT("Project", Comparator.comparing(OmittedSample::getProject)),
  REQUISITION("Requisition", Comparator.comparing(OmittedSample::getRequisitionName,
      Comparator.nullsLast(Comparator.naturalOrder()))),
  CREATED("Created Date", Comparator.comparing(OmittedSample::getCreatedDate));
  // @formatter:off

  private static final Map<String, OmittedSampleSort> map = Stream.of(OmittedSampleSort.values())
      .collect(Collectors.toMap(OmittedSampleSort::getLabel, Function.identity()));

  public static OmittedSampleSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<OmittedSample> comparator;

  private OmittedSampleSort(String label, Comparator<OmittedSample> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<OmittedSample> comparator() {
    return comparator;
  }

}
