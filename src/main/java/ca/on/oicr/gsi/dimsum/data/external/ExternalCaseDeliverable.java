package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.DeliverableType;

public record ExternalCaseDeliverable(DeliverableType deliverableType,
    AnalysisReviewQcStatus analysisReviewQcStatus, LocalDate analysisReviewQcDate,
    ReleaseApprovalQcStatus releaseApprovalQcStatus, LocalDate releaseApprovalQcDate,
    List<ExternalCaseRelease> releases) {

  public ExternalCaseDeliverable(CaseDeliverable from) {
    this(from.getDeliverableType(),
        from.getAnalysisReviewQcStatus(),
        from.getAnalysisReviewQcDate(),
        from.getReleaseApprovalQcStatus(),
        from.getReleaseApprovalQcDate(),
        from.getReleases().stream().map(ExternalCaseRelease::new)
            .collect(Collectors.toUnmodifiableList()));
  }

}
