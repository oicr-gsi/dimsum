package ca.on.oicr.gsi.dimsum.data;

import java.time.LocalDate;

public class Sample {

  private String id;
  private String name;
  private String tissueOrigin;
  private String tissueType;
  private String timepoint;
  private String groupId;
  private String targetedSequencing;
  private Boolean qcPassed;
  private String qcReason;
  private String qcUser;
  private LocalDate qcDate;
  private Boolean dataReviewPassed;
  private String dataReviewUser;
  private LocalDate dataReviewDate;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public void setTissueOrigin(String tissueOrigin) {
    this.tissueOrigin = tissueOrigin;
  }

  public String getTissueType() {
    return tissueType;
  }

  public void setTissueType(String tissueType) {
    this.tissueType = tissueType;
  }

  public String getTimepoint() {
    return timepoint;
  }

  public void setTimepoint(String timepoint) {
    this.timepoint = timepoint;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getTargetedSequencing() {
    return targetedSequencing;
  }

  public void setTargetedSequencing(String targetedSequencing) {
    this.targetedSequencing = targetedSequencing;
  }

  public Boolean getQcPassed() {
    return qcPassed;
  }

  public void setQcPassed(Boolean qcPassed) {
    this.qcPassed = qcPassed;
  }

  public String getQcReason() {
    return qcReason;
  }

  public void setQcReason(String qcReason) {
    this.qcReason = qcReason;
  }

  public String getQcUser() {
    return qcUser;
  }

  public void setQcUser(String qcUser) {
    this.qcUser = qcUser;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

  public void setQcDate(LocalDate qcDate) {
    this.qcDate = qcDate;
  }

  public Boolean getDataReviewPassed() {
    return dataReviewPassed;
  }

  public void setDataReviewPassed(Boolean dataReviewPassed) {
    this.dataReviewPassed = dataReviewPassed;
  }

  public String getDataReviewUser() {
    return dataReviewUser;
  }

  public void setDataReviewUser(String dataReviewUser) {
    this.dataReviewUser = dataReviewUser;
  }

  public LocalDate getDataReviewDate() {
    return dataReviewDate;
  }

  public void setDataReviewDate(LocalDate dataReviewDate) {
    this.dataReviewDate = dataReviewDate;
  }
}
