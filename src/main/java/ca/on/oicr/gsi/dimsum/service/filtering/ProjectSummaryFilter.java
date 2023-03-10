package ca.on.oicr.gsi.dimsum.service.filtering;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import ca.on.oicr.gsi.dimsum.data.ProjectSummary;


public class ProjectSummaryFilter {

  private final ProjectSummaryFilterKey key;
  private final String value;

  public ProjectSummaryFilter(ProjectSummaryFilterKey key, String value) {
    requireNonNull(key);
    requireNonNull(value);
    this.key = key;
    this.value = value;
  }

  public ProjectSummaryFilterKey getKey() {
    return key;
  }

  public Predicate<ProjectSummary> predicate() {
    return key.create().apply(value);
  }

}
