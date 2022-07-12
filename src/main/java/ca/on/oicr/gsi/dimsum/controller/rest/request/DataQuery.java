package ca.on.oicr.gsi.dimsum.controller.rest.request;

import java.util.List;

public class DataQuery {

  private int pageNumber;
  private int pageSize;
  private List<KeyValuePair> filters;
  private String sortColumn;
  private Boolean descending;

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public List<KeyValuePair> getFilters() {
    return filters;
  }

  public void setFilters(List<KeyValuePair> filters) {
    this.filters = filters;
  }

  public String getSortColumn() {
    return sortColumn;
  }

  public void setSortColumn(String sortColumn) {
    this.sortColumn = sortColumn;
  }

  public Boolean getDescending() {
    return descending;
  }

  public void setDescending(Boolean descending) {
    this.descending = descending;
  }

}
