package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.function.Function;
import java.util.function.Predicate;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;

public enum ProjectSummaryFilterKey {
  NAME(string -> projectSummary -> projectSummary.getName().equalsIgnoreCase(string));

  private final Function<String, Predicate<ProjectSummary>> create;

  private ProjectSummaryFilterKey(Function<String, Predicate<ProjectSummary>> create) {
    this.create = create;
  }

  public Function<String, Predicate<ProjectSummary>> create() {
    return create;
  }
}
