package ca.on.oicr.gsi.dimsum.data;

import ca.on.oicr.gsi.cardea.data.DeliverableType;

public class NabuSignoff {

  public enum NabuSignoffStep {
    ANALYSIS_REVIEW, RELEASE_APPROVAL, RELEASE
  };

  private Boolean qcPassed;
  private Boolean release;
  private String username;
  private NabuSignoffStep signoffStepName;
  private DeliverableType deliverableType;
  private String deliverable;
  private String comment;

  public Boolean getQcPassed() {
    return qcPassed;
  }

  public void setQcPassed(Boolean qcPassed) {
    this.qcPassed = qcPassed;
  }

  public Boolean getRelease() {
    return release;
  }

  public void setRelease(Boolean release) {
    this.release = release;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public NabuSignoffStep getSignoffStepName() {
    return signoffStepName;
  }

  public void setSignoffStepName(NabuSignoffStep signoffStepName) {
    this.signoffStepName = signoffStepName;
  }

  public DeliverableType getDeliverableType() {
    return deliverableType;
  }

  public void setDeliverableType(DeliverableType deliverableType) {
    this.deliverableType = deliverableType;
  }

  public String getDeliverable() {
    return deliverable;
  }

  public void setDeliverable(String deliverable) {
    this.deliverable = deliverable;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

}
