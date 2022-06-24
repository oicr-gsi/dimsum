package ca.on.oicr.gsi.dimsum.controller.rest.request;

import java.util.Map;

public class DataQuery {

  private int pageNumber;
  private int pageSize;
  private Map<String, String> filters;
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

  public Map<String, String> getFilters() {
    return filters;
  }

  public void setFilters(Map<String, String> filters) {
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
