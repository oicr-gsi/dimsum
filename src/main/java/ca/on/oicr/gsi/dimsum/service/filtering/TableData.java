package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.List;

public class TableData<T> {

  private List<T> items;
  private long totalCount;
  private long filteredCount;

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }

  public long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(long totalCount) {
    this.totalCount = totalCount;
  }

  public long getFilteredCount() {
    return filteredCount;
  }

  public void setFilteredCount(long filteredCount) {
    this.filteredCount = filteredCount;
  }

}
