package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;

public enum ProjectSummarySort {

  // @formatter:off
  NAME("Name", Comparator.comparing(ProjectSummary::getName));

  // @formatter:on
  private static final Map<String, ProjectSummarySort> map = Stream.of(ProjectSummarySort.values())
      .collect(Collectors.toMap(ProjectSummarySort::getLabel, Function.identity()));

  public static ProjectSummarySort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<ProjectSummary> comparator;

  private ProjectSummarySort(String label, Comparator<ProjectSummary> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<ProjectSummary> comparator() {
    return comparator;
  }
}
