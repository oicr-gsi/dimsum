package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.dimsum.util.DataUtils;

public enum SampleSort {

  // @formatter:off
    NAME("Name", Comparator.comparing(Sample::getName)),
    LATEST_ACTIVITY("Latest Activity", Comparator.comparing(Sample::getLatestActivityDate)),
    QC_STATUS("QC Status", Comparator.comparing(SampleSort::getSampleQcStatus));
    // @formatter:on

  private static final Map<String, SampleSort> map = Stream.of(SampleSort.values())
      .collect(Collectors.toMap(SampleSort::getLabel, Function.identity()));

  public static SampleSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<Sample> comparator;

  private SampleSort(String label, Comparator<Sample> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<Sample> comparator() {
    return comparator;
  }

  protected static int getSampleQcStatus(Sample sample) {
    if (DataUtils.isPendingQc(sample)) {
      return 1;
    } else if (DataUtils.isPendingDataReview(sample)) {
      return 2;
    } else if (DataUtils.isTopUpRequired(sample)) {
      return 3;
    } else if (DataUtils.isPassed(sample)) {
      return 4;
    } else {
      return 5;
    }
  }
}
