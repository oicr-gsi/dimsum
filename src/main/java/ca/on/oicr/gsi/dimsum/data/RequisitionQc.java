package ca.on.oicr.gsi.dimsum.data;

import java.time.LocalDate;

public class RequisitionQc {

  private boolean qcPassed;
  private String qcUser;
  private LocalDate qcDate;

  public boolean isQcPassed() {
    return qcPassed;
  }

  public void setQcPassed(boolean qcPassed) {
    this.qcPassed = qcPassed;
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
}
