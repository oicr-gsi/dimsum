package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@Immutable
@JsonDeserialize(builder = ProjectSummary.Builder.class)
public class ProjectSummary {
  private final String name;
  private final String pipeline;

  private final int totalTestCount;
  private final int receiptPendingQcCount;
  private final int receiptCompletedCount;
  private final int extractionPendingCount;
  private final int extractionPendingQcCount;
  private final int extractionCompletedCount;
  private final int libraryPrepPendingCount;
  private final int libraryPrepPendingQcCount;
  private final int libraryPrepCompletedCount;
  private final int libraryQualPendingCount;
  private final int libraryQualPendingQcCount;
  private final int libraryQualCompletedCount;
  private final int fullDepthSeqPendingCount;
  private final int fullDepthSeqPendingQcCount;
  private final int fullDepthSeqCompletedCount;
  private final int analysisReviewPendingCount;
  private final int analysisReviewCompletedCount;
  private final int releaseApprovalPendingCount;
  private final int releaseApprovalCompletedCount;
  private final int releasePendingCount;
  private final int releaseCompletedCount;

  private ProjectSummary(Builder builder) {
    this.name = requireNonNull(builder.name);
    this.pipeline = requireNonNull(builder.pipeline);

    this.totalTestCount = builder.totalTestCount;
    this.receiptPendingQcCount = builder.receiptPendingQcCount;
    this.receiptCompletedCount = builder.receiptCompletedCount;
    this.extractionPendingCount = builder.extractionPendingCount;
    this.extractionPendingQcCount = builder.extractionPendingQcCount;
    this.extractionCompletedCount = builder.extractionCompletedCount;
    this.libraryPrepPendingCount = builder.libraryPrepPendingCount;
    this.libraryPrepPendingQcCount = builder.libraryPrepPendingQcCount;
    this.libraryPrepCompletedCount = builder.libraryPrepCompletedCount;
    this.libraryQualPendingCount = builder.libraryQualPendingCount;
    this.libraryQualPendingQcCount = builder.libraryQualPendingQcCount;
    this.libraryQualCompletedCount = builder.libraryQualCompletedCount;
    this.fullDepthSeqPendingCount = builder.fullDepthSeqPendingCount;
    this.fullDepthSeqPendingQcCount = builder.fullDepthSeqPendingQcCount;
    this.fullDepthSeqCompletedCount = builder.fullDepthSeqCompletedCount;
    this.analysisReviewPendingCount = builder.analysisReviewPendingCount;
    this.analysisReviewCompletedCount = builder.analysisReviewCompletedCount;
    this.releaseApprovalPendingCount = builder.releaseApprovalPendingCount;
    this.releaseApprovalCompletedCount = builder.releaseApprovalCompletedCount;
    this.releasePendingCount = builder.releasePendingCount;
    this.releaseCompletedCount = builder.releaseCompletedCount;


  }

  public String getName() {
    return name;
  }

  public String getPipeline() {
    return pipeline;
  }

  public int getTotalTestCount() {
    return totalTestCount;
  }

  public int getReceiptPendingQcCount() {
    return receiptPendingQcCount;
  }

  public int getReceiptCompletedCount() {
    return receiptCompletedCount;
  }

  public int getExtractionPendingCount() {
    return extractionPendingCount;
  }

  public int getExtractionPendingQcCount() {
    return extractionPendingQcCount;
  }

  public int getExtractionCompletedCount() {
    return extractionCompletedCount;
  }

  public int getLibraryPrepPendingCount() {
    return libraryPrepPendingCount;
  }

  public int getLibraryPrepPendingQcCount() {
    return libraryPrepPendingQcCount;
  }

  public int getLibraryPrepCompletedCount() {
    return libraryPrepCompletedCount;
  }

  public int getLibraryQualPendingCount() {
    return libraryQualPendingCount;
  }

  public int getLibraryQualPendingQcCount() {
    return libraryQualPendingQcCount;
  }

  public int getLibraryQualCompletedCount() {
    return libraryQualCompletedCount;

  }

  public int getFullDepthSeqPendingCount() {
    return fullDepthSeqPendingCount;
  }

  public int getFullDepthSeqPendingQcCount() {
    return fullDepthSeqPendingQcCount;
  }

  public int getFullDepthSeqCompletedCount() {
    return fullDepthSeqCompletedCount;
  }

  public int getAnalysisReviewPendingCount() {
    return analysisReviewPendingCount;
  }

  public int getAnalysisReviewCompletedCount() {
    return analysisReviewCompletedCount;
  }

  public int getReleaseApprovalPendingCount() {
    return releaseApprovalPendingCount;
  }

  public int getReleaseApprovalCompletedCount() {
    return releaseApprovalCompletedCount;
  }

  public int getReleasePendingCount() {
    return releasePendingCount;
  }

  public int getReleaseCompletedCount() {
    return releaseCompletedCount;
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class Builder {

    private String name;
    private String pipeline;

    private int totalTestCount = 0;
    private int receiptPendingQcCount = 0;
    private int receiptCompletedCount = 0;
    private int extractionPendingCount = 0;
    private int extractionPendingQcCount = 0;
    private int extractionCompletedCount = 0;
    private int libraryPrepPendingCount = 0;
    private int libraryPrepPendingQcCount = 0;
    private int libraryPrepCompletedCount = 0;
    private int libraryQualPendingCount = 0;
    private int libraryQualPendingQcCount = 0;
    private int libraryQualCompletedCount = 0;
    private int fullDepthSeqPendingCount = 0;
    private int fullDepthSeqPendingQcCount = 0;
    private int fullDepthSeqCompletedCount = 0;
    private int analysisReviewPendingCount = 0;
    private int analysisReviewCompletedCount = 0;
    private int releaseApprovalPendingCount = 0;
    private int releaseApprovalCompletedCount = 0;
    private int releasePendingCount = 0;
    private int releaseCompletedCount = 0;


    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder pipeline(String pipeline) {
      this.pipeline = pipeline;
      return this;
    }

    public void totalTestCount(int totalTestCount) {
      this.totalTestCount = totalTestCount;
    }

    public void receiptPendingQcCount(int receiptPendingQcCount) {
      this.receiptPendingQcCount = receiptPendingQcCount;
    }

    public void receiptCompletedCount(int receiptCompletedCount) {
      this.receiptCompletedCount = receiptCompletedCount;
    }

    public void incrementExtractionPendingCount() {
      this.extractionPendingCount += 1;
    }

    public void incrementExtractionPendingQcCount() {
      this.extractionPendingQcCount += 1;
    }

    public void incrementExtractionCompletedCount() {
      this.extractionCompletedCount += 1;
    }

    public void incrementLibraryPrepPendingCount() {
      this.libraryPrepPendingCount += 1;
    }

    public void incrementLibraryPrepPendingQcCount() {
      this.libraryPrepPendingQcCount += 1;
    }

    public void incrementLibraryPrepCompletedCount() {
      this.libraryPrepCompletedCount += 1;
    }

    public void incrementLibraryQualPendingCount() {
      this.libraryQualPendingCount += 1;
    }

    public void incrementLibraryQualPendingQcCount() {
      this.libraryQualPendingQcCount += 1;
    }

    public void incrementLibraryQualCompletedCount() {
      this.libraryQualCompletedCount += 1;
    }

    public void incrementFullDepthSeqPendingCount() {
      this.fullDepthSeqPendingCount += 1;
    }

    public void incrementFullDepthSeqPendingQcCount() {
      this.fullDepthSeqPendingQcCount += 1;

    }

    public void incrementFullDepthSeqCompletedCount() {
      this.fullDepthSeqCompletedCount += 1;
    }

    public void analysisReviewPendingCount(int analysisReviewPendingCount) {
      this.analysisReviewPendingCount = analysisReviewPendingCount;
    }

    public void analysisReviewCompletedCount(int analysisReviewCompletedCount) {
      this.analysisReviewCompletedCount = analysisReviewCompletedCount;
    }

    public void releaseApprovalPendingCount(int releaseApprovalPendingCount) {
      this.releaseApprovalPendingCount = releaseApprovalPendingCount;
    }

    public void releaseApprovalCompletedCount(int releaseApprovalCompletedCount) {
      this.releaseApprovalCompletedCount = releaseApprovalCompletedCount;
    }

    public void releasePendingCount(int releasePendingCount) {
      this.releasePendingCount = releasePendingCount;
    }

    public void releaseCompletedCount(int releaseCompletedCount) {
      this.releaseCompletedCount = releaseCompletedCount;
    }

    public Builder addCounts(ProjectSummary.Builder builder) {
      this.totalTestCount += builder.totalTestCount;
      this.receiptPendingQcCount += builder.receiptPendingQcCount;
      this.receiptCompletedCount += builder.receiptCompletedCount;
      this.extractionPendingCount += builder.extractionPendingCount;
      this.extractionPendingQcCount += builder.extractionPendingQcCount;
      this.extractionCompletedCount += builder.extractionCompletedCount;
      this.libraryPrepPendingCount += builder.libraryPrepPendingCount;
      this.libraryPrepPendingQcCount += builder.libraryPrepPendingQcCount;
      this.libraryPrepCompletedCount += builder.libraryPrepCompletedCount;
      this.libraryQualPendingCount += builder.libraryQualPendingCount;
      this.libraryQualPendingQcCount += builder.libraryQualPendingQcCount;
      this.libraryQualCompletedCount += builder.libraryQualCompletedCount;
      this.fullDepthSeqPendingCount += builder.fullDepthSeqPendingCount;
      this.fullDepthSeqPendingQcCount += builder.fullDepthSeqPendingQcCount;
      this.fullDepthSeqCompletedCount += builder.fullDepthSeqCompletedCount;
      this.analysisReviewPendingCount += builder.analysisReviewPendingCount;
      this.analysisReviewCompletedCount += builder.analysisReviewCompletedCount;
      this.releaseApprovalPendingCount += builder.releaseApprovalPendingCount;
      this.releaseApprovalCompletedCount += builder.releaseApprovalCompletedCount;
      this.releasePendingCount += builder.releasePendingCount;
      this.releaseCompletedCount += builder.releaseCompletedCount;
      return this;
    }

    public ProjectSummary build() {
      return new ProjectSummary(this);
    }
  }
}
