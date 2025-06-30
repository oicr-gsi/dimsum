package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;

public record ExternalCaseDeliverable(String deliverableCategory,
    AnalysisReviewQcStatus analysisReviewQcStatus, LocalDate analysisReviewQcDate,
    ReleaseApprovalQcStatus releaseApprovalQcStatus, LocalDate releaseApprovalQcDate,
    List<ExternalCaseRelease> releases) {

  public ExternalCaseDeliverable(CaseDeliverable from) {
    this(from.getDeliverableCategory(),
        from.getAnalysisReviewQcStatus(),
        from.getAnalysisReviewQcDate(),
        from.getReleaseApprovalQcStatus(),
        from.getReleaseApprovalQcDate(),
        from.getReleases().stream().map(ExternalCaseRelease::new)
            .collect(Collectors.toUnmodifiableList()));
  }

}
