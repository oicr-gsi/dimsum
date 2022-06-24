package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.List;

public class TableData<T> {

  private List<T> items;
  private int totalCount;
  private int filteredCount;

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  public int getFilteredCount() {
    return filteredCount;
  }

  public void setFilteredCount(int filteredCount) {
    this.filteredCount = filteredCount;
  }

}
