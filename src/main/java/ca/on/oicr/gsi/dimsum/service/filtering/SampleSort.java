package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.dimsum.data.Sample;

public enum SampleSort {

  NAME("Name", Comparator.comparing(Sample::getName));

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

}
