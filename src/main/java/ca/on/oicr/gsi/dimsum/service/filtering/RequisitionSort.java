package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.dimsum.data.Requisition;

public enum RequisitionSort {

  NAME("Name", Comparator.comparing(Requisition::getName));

  private static final Map<String, RequisitionSort> map = Stream.of(RequisitionSort.values())
      .collect(Collectors.toMap(RequisitionSort::getLabel, Function.identity()));

  public static RequisitionSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<Requisition> comparator;

  private RequisitionSort(String label, Comparator<Requisition> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<Requisition> comparator() {
    return comparator;
  }

}
