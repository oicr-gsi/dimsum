package ca.on.oicr.gsi.dimsum.data;

import java.util.List;

public class Requisition {

  private long id;
  private String name;
  private List<RequisitionQc> informationReviews;
  private List<RequisitionQc> draftReports;
  private List<RequisitionQc> finalReports;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<RequisitionQc> getInformationReviews() {
    return informationReviews;
  }

  public void setInformationReviews(List<RequisitionQc> informationReviews) {
    this.informationReviews = informationReviews;
  }

  public List<RequisitionQc> getDraftReports() {
    return draftReports;
  }

  public void setDraftReports(List<RequisitionQc> draftReports) {
    this.draftReports = draftReports;
  }

  public List<RequisitionQc> getFinalReports() {
    return finalReports;
  }

  public void setFinalReports(List<RequisitionQc> finalReports) {
    this.finalReports = finalReports;
  }
}
