package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseRelease;

public class ExternalCaseRelease {

  private final String deliverable;
  private final ReleaseQcStatus qcStatus;
  private final LocalDate qcDate;

  public ExternalCaseRelease(CaseRelease from) {
    deliverable = from.getDeliverable();
    qcStatus = from.getQcStatus();
    qcDate = from.getQcDate();
  }

  public String getDeliverable() {
    return deliverable;
  }

  public ReleaseQcStatus getQcStatus() {
    return qcStatus;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

}
