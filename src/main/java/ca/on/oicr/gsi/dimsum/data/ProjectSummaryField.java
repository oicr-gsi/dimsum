package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import groovy.transform.Immutable;

@Immutable
public class ProjectSummaryField {
  private int count;
  private String filterKey;
  private String filterValue;

  private ProjectSummaryField(Builder builder) {
    this.count = requireNonNull(builder.count);
    this.filterKey = requireNonNull(builder.filterKey);
    this.filterValue = requireNonNull(builder.filterValue);
  }

  public int getCount() {
    return count;
  }

  public String getFilterKey() {
    return filterKey;
  }

  public String getFilterValue() {
    return filterValue;
  }

  public static class Builder {
    private int count;
    private String filterKey;
    private String filterValue;

    public Builder count(int count) {
      this.count = count;
      return this;
    }

    public Builder filterKey(String filterKey) {
      this.filterKey = filterKey;
      return this;
    }

    public Builder filterValue(String filterValue) {
      this.filterValue = filterValue;
      return this;
    }

    public ProjectSummaryField build() {
      return new ProjectSummaryField(this);
    }

  }


}
