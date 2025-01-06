package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.DeliverableType;

public class ExternalCaseDeliverable {

  private final DeliverableType deliverableType;
  private final AnalysisReviewQcStatus analysisReviewQcStatus;
  private final LocalDate analysisReviewQcDate;
  private final ReleaseApprovalQcStatus releaseApprovalQcStatus;
  private final LocalDate releaseApprovalQcDate;
  private final List<ExternalCaseRelease> releases;

  public ExternalCaseDeliverable(CaseDeliverable from) {
    deliverableType = from.getDeliverableType();
    analysisReviewQcStatus = from.getAnalysisReviewQcStatus();
    analysisReviewQcDate = from.getAnalysisReviewQcDate();
    releaseApprovalQcStatus = from.getReleaseApprovalQcStatus();
    releaseApprovalQcDate = from.getReleaseApprovalQcDate();
    releases = from.getReleases().stream().map(ExternalCaseRelease::new)
        .collect(Collectors.toUnmodifiableList());
  }

  public DeliverableType getDeliverableType() {
    return deliverableType;
  }

  public AnalysisReviewQcStatus getAnalysisReviewQcStatus() {
    return analysisReviewQcStatus;
  }

  public LocalDate getAnalysisReviewQcDate() {
    return analysisReviewQcDate;
  }

  public ReleaseApprovalQcStatus getReleaseApprovalQcStatus() {
    return releaseApprovalQcStatus;
  }

  public LocalDate getReleaseApprovalQcDate() {
    return releaseApprovalQcDate;
  }

  public List<ExternalCaseRelease> getReleases() {
    return releases;
  }

}
