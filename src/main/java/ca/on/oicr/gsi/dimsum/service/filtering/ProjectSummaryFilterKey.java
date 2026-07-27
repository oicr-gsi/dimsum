package ca.on.oicr.gsi.dimsum.service.filtering;

import ca.on.oicr.gsi.dimsum.data.ProjectSummary;
import java.util.function.Function;
import java.util.function.Predicate;

public enum ProjectSummaryFilterKey {
  NAME(string -> projectSummary -> projectSummary.getName().equalsIgnoreCase(string)),
  PIPELINE(string -> projectSummary -> projectSummary.getPipeline().equals(string));

  private final Function<String, Predicate<ProjectSummary>> create;

  private ProjectSummaryFilterKey(Function<String, Predicate<ProjectSummary>> create) {
    this.create = create;
  }

  public Function<String, Predicate<ProjectSummary>> create() {
    return create;
  }
}
