package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.dimsum.data.TestTableView;

public enum TestTableViewSort {

  // @formatter:off
  TEST("Test", Comparator.comparing(testTableView -> testTableView.getTest().getName())),
  ASSAY("Assay", Comparator.comparing(testTableView -> testTableView.getAssayName())),
  DONOR("Donor", Comparator.comparing(testTableView -> testTableView.getDonor().getName())),
  LAST_ACTIVITY("Latest Activity", Comparator.comparing(TestTableView::getLatestActivityDate));

  // @formatter:on
  private static final Map<String, TestTableViewSort> map = Stream.of(TestTableViewSort.values())
      .collect(Collectors.toMap(TestTableViewSort::getLabel, Function.identity()));

  public static TestTableViewSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<TestTableView> comparator;

  private TestTableViewSort(String label, Comparator<TestTableView> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<TestTableView> comparator() {
    return comparator;
  }

}
