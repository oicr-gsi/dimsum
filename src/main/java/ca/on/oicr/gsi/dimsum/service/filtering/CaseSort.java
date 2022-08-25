package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.dimsum.data.Case;

public enum CaseSort {

  // @formatter:off
  ASSAY("Assay", Comparator.comparing(kase -> kase.getAssay().getName())),
  DONOR("Donor", Comparator.comparing(kase -> kase.getDonor().getName())),
  START_DATE("Start Date", Comparator.comparing(Case::getStartDate)),
  LAST_ACTIVITY("Latest Activity", Comparator.comparing(Case::getLatestActivityDate));
  // @formatter:on

  private static final Map<String, CaseSort> map = Stream.of(CaseSort.values())
      .collect(Collectors.toMap(CaseSort::getLabel, Function.identity()));

  public static CaseSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<Case> comparator;

  private CaseSort(String label, Comparator<Case> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<Case> comparator() {
    return comparator;
  }

}
