package ca.on.oicr.gsi.dimsum.data.external;

import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
import java.time.LocalDate;

public record ExternalCaseRelease(String deliverable, ReleaseQcStatus qcStatus, LocalDate qcDate) {

  public ExternalCaseRelease(CaseRelease from) {
    this(from.getDeliverable(), from.getQcStatus(), from.getQcDate());
  }
}
