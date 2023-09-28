package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Run;

public enum RunSort {

  // @formatter:off
  NAME("Name", Comparator.comparing(Run::getName)),
  COMPLETION_DATE("Completion Date",
      Comparator.comparing(Run::getCompletionDate, Comparator.nullsLast(Comparator.naturalOrder())));
  // @formatter:on

  private static final Map<String, RunSort> map = Stream.of(RunSort.values())
      .collect(Collectors.toMap(RunSort::getLabel, Function.identity()));

  public static RunSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<Run> comparator;

  private RunSort(String label, Comparator<Run> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<Run> comparator() {
    return comparator;
  }

}
