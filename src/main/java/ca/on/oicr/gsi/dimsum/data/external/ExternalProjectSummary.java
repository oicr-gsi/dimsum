package ca.on.oicr.gsi.dimsum.data.external;

import ca.on.oicr.gsi.dimsum.data.ProjectSummary;

public record ExternalProjectSummary(String name, String pipeline, int totalTestCount,
    int receiptCompletedCount, int extractionCompletedCount, int libraryPrepCompletedCount,
    int libraryQualCompletedCount, int fullDepthSeqCompletedCount, int analysisReviewCompletedCount,
    int releaseApprovalCompletedCount, int releaseCompletedCount) {

  public ExternalProjectSummary(ProjectSummary from) {
    this(from.getName(),
        from.getPipeline(),
        from.getTotalTestCount(),
        from.getReceiptCompletedCount(),
        from.getExtractionCompletedCount(),
        from.getLibraryPrepCompletedCount(),
        from.getLibraryQualCompletedCount(),
        from.getFullDepthSeqCompletedCount(),
        from.getAnalysisReviewCompletedCount(),
        from.getReleaseApprovalCompletedCount(),
        from.getReleaseCompletedCount());
  }
}
