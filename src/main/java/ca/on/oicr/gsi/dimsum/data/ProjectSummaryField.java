package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;

@Immutable
public class ProjectSummaryField {
  private int count;
  private String filterKey;
  private String filterValue;

  public ProjectSummaryField(int count, String filterKey, String filterValue) {
    this.count = requireNonNull(count);
    this.filterKey = requireNonNull(filterKey);
    this.filterValue = requireNonNull(filterValue);
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


}
