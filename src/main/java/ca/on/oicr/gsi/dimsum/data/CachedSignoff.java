package ca.on.oicr.gsi.dimsum.data;

import java.time.LocalDate;
import ca.on.oicr.gsi.cardea.data.CaseQc;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;

public class CachedSignoff {

  private final String qcUser;
  private final CaseQc qcStatus;
  private final LocalDate qcDate;
  private final String qcNote;

  public CachedSignoff(NabuSavedSignoff nabuSignoff) {
    switch (nabuSignoff.getSignoffStepName()) {
      case ANALYSIS_REVIEW:
        this.qcStatus =
            AnalysisReviewQcStatus.of(nabuSignoff.getQcPassed(), nabuSignoff.getRelease());
        break;
      case RELEASE_APPROVAL:
        this.qcStatus =
            ReleaseApprovalQcStatus.of(nabuSignoff.getQcPassed(), nabuSignoff.getRelease());
        break;
      case RELEASE:
        this.qcStatus = ReleaseQcStatus.of(nabuSignoff.getQcPassed(), nabuSignoff.getRelease());
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid signoff step: " + nabuSignoff.getSignoffStepName());
    }
    this.qcUser = nabuSignoff.getUsername();
    this.qcDate = nabuSignoff.getCreated().toLocalDate();
    this.qcNote = nabuSignoff.getComment();
  }

  public CaseQc getQcStatus() {
    return qcStatus;
  }

  public String getQcUser() {
    return qcUser;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

  public String getQcNote() {
    return qcNote;
  }

}
