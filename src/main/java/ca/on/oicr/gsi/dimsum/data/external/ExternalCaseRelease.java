package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseRelease;

public record ExternalCaseRelease(String deliverable, ReleaseQcStatus qcStatus, LocalDate qcDate) {

  public ExternalCaseRelease(CaseRelease from) {
    this(from.getDeliverable(), from.getQcStatus(), from.getQcDate());
  }

}
