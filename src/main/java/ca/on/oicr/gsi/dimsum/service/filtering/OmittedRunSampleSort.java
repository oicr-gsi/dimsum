package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.OmittedRunSample;
import ca.on.oicr.gsi.dimsum.util.DataUtils;

public enum OmittedRunSampleSort {

  // @formatter:off
  NAME("Name", Comparator.comparing(OmittedRunSample::getName)),
  QC_STATUS("QC Status", Comparator.comparing(OmittedRunSampleSort::getQcStatusSortPriority));
  // @formatter:on

  private static final Map<String, OmittedRunSampleSort> map =
      Stream.of(OmittedRunSampleSort.values())
          .collect(Collectors.toMap(OmittedRunSampleSort::getLabel, Function.identity()));

  public static OmittedRunSampleSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<OmittedRunSample> comparator;

  private OmittedRunSampleSort(String label, Comparator<OmittedRunSample> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<OmittedRunSample> comparator() {
    return comparator;
  }

  protected static int getQcStatusSortPriority(OmittedRunSample sample) {
    if (sample.getQcDate() == null) {
      return 1;
    } else if (sample.getDataReviewDate() == null) {
      return 2;
    } else if (DataUtils.isTopUpRequired(sample)) {
      return 3;
    } else if (Boolean.TRUE.equals(sample.getQcPassed())) {
      return 4;
    } else {
      return 5;
    }
  }
}
