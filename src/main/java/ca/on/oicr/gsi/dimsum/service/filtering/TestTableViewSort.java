package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.dimsum.data.TestTableView;
import ca.on.oicr.gsi.dimsum.data.external.ExternalTestTableView;

public enum TestTableViewSort {

  // @formatter:off
  TEST("Test", Comparator.comparing(testTableView -> testTableView.getTest().getName()),
      Comparator.comparing(testTableView -> testTableView.test().name())),
  ASSAY("Assay", Comparator.comparing(testTableView -> testTableView.getAssayName()),
      Comparator.comparing(ExternalTestTableView::assayName)),
  DONOR("Donor", Comparator.comparing(testTableView -> testTableView.getDonor().getName()),
      Comparator.comparing(testTableView -> testTableView.donor().getName())),
  LAST_ACTIVITY("Latest Activity", Comparator.comparing(TestTableView::getLatestActivityDate),
      Comparator.comparing(ExternalTestTableView::latestActivityDate));
  // @formatter:on

  private static final Map<String, TestTableViewSort> map = Stream.of(TestTableViewSort.values())
      .collect(Collectors.toMap(TestTableViewSort::getLabel, Function.identity()));

  public static TestTableViewSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<TestTableView> comparator;
  private final Comparator<ExternalTestTableView> externalComparator;

  private TestTableViewSort(String label, Comparator<TestTableView> comparator,
      Comparator<ExternalTestTableView> externalComparator) {
    this.label = label;
    this.comparator = comparator;
    this.externalComparator = externalComparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<TestTableView> comparator() {
    return comparator;
  }

  public Comparator<ExternalTestTableView> externalComparator() {
    return externalComparator;
  }

}
