package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@Immutable
@JsonDeserialize(builder = ProjectSummary.Builder.class)
public class ProjectSummary {
  private final String name;

  private final int totalTestCount;
  // receipt
  private final int receiptPendingQcCount;
  private final int receiptCompletedCount;
  // extraction
  private final int extractionPendingCount;
  private final int extractionPendingQcCount;
  private final int extractionCompletedCount;
  // library preparation
  private final int libraryPrepPendingCount;
  private final int libraryPrepPendingQcCount;
  private final int libraryPrepCompletedCount;
  // library qualification
  private final int libraryQualPendingCount;
  private final int libraryQualPendingQcCount;
  private final int libraryQualCompletedCount;
  // full depth sequencing
  private final int fullDepthSeqPendingCount;
  private final int fullDepthSeqPendingQcCount;
  private final int fullDepthSeqCompletedCount;
  // informatics review
  private final int informaticsPendingCount;
  private final int informaticsCompletedCount;
  // draft report
  private final int draftReportPendingCount;
  private final int draftReportCompletedCount;
  // final report
  private final int finalReportPendingCount;
  private final int finalReportCompletedCount;

  private ProjectSummary(Builder builder) {
    this.name = requireNonNull(builder.name);
    this.totalTestCount = builder.totalTestCount;
    // receipt
    this.receiptPendingQcCount = builder.receiptPendingQcCount;
    this.receiptCompletedCount = builder.receiptCompletedCount;
    // extraction
    this.extractionPendingCount = builder.extractionPendingCount;
    this.extractionPendingQcCount = builder.extractionPendingQcCount;
    this.extractionCompletedCount = builder.extractionCompletedCount;
    // library preparation
    this.libraryPrepPendingCount = builder.libraryPrepPendingCount;
    this.libraryPrepPendingQcCount = builder.libraryPrepPendingQcCount;
    this.libraryPrepCompletedCount = builder.libraryPrepCompletedCount;
    // library qualification
    this.libraryQualPendingCount = builder.libraryQualPendingCount;
    this.libraryQualPendingQcCount = builder.libraryQualPendingQcCount;
    this.libraryQualCompletedCount = builder.libraryQualCompletedCount;
    // full depth sequencing
    this.fullDepthSeqPendingCount = builder.fullDepthSeqPendingCount;
    this.fullDepthSeqPendingQcCount = builder.fullDepthSeqPendingQcCount;
    this.fullDepthSeqCompletedCount = builder.fullDepthSeqCompletedCount;
    // informatics review
    this.informaticsPendingCount = builder.informaticsPendingCount;
    this.informaticsCompletedCount = builder.informaticsCompletedCount;
    // draft report
    this.draftReportPendingCount = builder.draftReportPendingCount;
    this.draftReportCompletedCount = builder.draftReportCompletedCount;
    // final report
    this.finalReportPendingCount = builder.finalReportPendingCount;
    this.finalReportCompletedCount = builder.finalReportCompletedCount;


  }

  public String getName() {
    return name;
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

  // extraction
  public int getExtractionPendingCount() {
    return extractionPendingCount;
  }

  public int getExtractionPendingQcCount() {
    return extractionPendingQcCount;
  }

  public int getExtractionCompletedCount() {
    return extractionCompletedCount;
  }

  // library preparation
  public int getLibraryPrepPendingCount() {
    return libraryPrepPendingCount;
  }

  public int getLibraryPrepPendingQcCount() {
    return libraryPrepPendingQcCount;
  }

  public int getLibraryPrepCompletedCount() {
    return libraryPrepCompletedCount;
  }

  // library qualification
  public int getLibraryQualPendingCount() {
    return libraryQualPendingCount;
  }

  public int getLibraryQualPendingQcCount() {
    return libraryQualPendingQcCount;
  }

  public int getLibraryQualCompletedCount() {
    return libraryQualCompletedCount;

  }

  // full depth sequencing
  public int getFullDepthSeqPendingCount() {
    return fullDepthSeqPendingCount;
  }

  public int getFullDepthSeqPendingQcCount() {
    return fullDepthSeqPendingQcCount;
  }

  public int getFullDepthSeqCompletedCount() {
    return fullDepthSeqCompletedCount;
  }

  // informatics review
  public int getInformaticsPendingCount() {
    return informaticsPendingCount;
  }

  public int getInformaticsCompletedCount() {
    return informaticsCompletedCount;
  }

  // draft report
  public int getDraftReportPendingCount() {
    return draftReportPendingCount;
  }

  public int getDraftReportCompletedCount() {
    return draftReportCompletedCount;
  }

  // final report
  public int getFinalReportPendingCount() {
    return finalReportPendingCount;
  }

  public int getFinalReportCompletedCount() {
    return finalReportCompletedCount;
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class Builder {

    private String name;
    private int totalTestCount = 0;
    // receipt
    private int receiptPendingQcCount = 0;
    private int receiptCompletedCount = 0;
    // extraction
    private int extractionPendingCount = 0;
    private int extractionPendingQcCount = 0;
    private int extractionCompletedCount = 0;
    // library preparation
    private int libraryPrepPendingCount = 0;
    private int libraryPrepPendingQcCount = 0;
    private int libraryPrepCompletedCount = 0;
    // library qualification
    private int libraryQualPendingCount = 0;
    private int libraryQualPendingQcCount = 0;
    private int libraryQualCompletedCount = 0;
    // full depth sequencing
    private int fullDepthSeqPendingCount = 0;
    private int fullDepthSeqPendingQcCount = 0;
    private int fullDepthSeqCompletedCount = 0;
    // informatics review
    private int informaticsPendingCount = 0;
    private int informaticsCompletedCount = 0;
    // draft report
    private int draftReportPendingCount = 0;
    private int draftReportCompletedCount = 0;
    // final report
    private int finalReportPendingCount = 0;
    private int finalReportCompletedCount = 0;


    public Builder name(String name) {
      this.name = name;
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

    // extraction
    public void incrementExtractionPendingCount() {
      this.extractionPendingCount += 1;
    }

    public void incrementExtractionPendingQcCount() {
      this.extractionPendingQcCount += 1;
    }

    public void incrementExtractionCompletedCount() {
      this.extractionCompletedCount += 1;
    }

    // library preparation
    public void incrementLibraryPrepPendingCount() {
      this.libraryPrepPendingCount += 1;
    }

    public void incrementLibraryPrepPendingQcCount() {
      this.libraryPrepPendingQcCount += 1;
    }

    public void incrementLibraryPrepCompletedCount() {
      this.libraryPrepCompletedCount += 1;
    }

    // library qualification
    public void incrementLibraryQualPendingCount() {
      this.libraryQualPendingCount += 1;
    }

    public void incrementLibraryQualPendingQcCount() {
      this.libraryQualPendingQcCount += 1;
    }

    public void incrementLibraryQualCompletedCount() {
      this.libraryQualCompletedCount += 1;
    }

    // full depth sequencing
    public void incrementFullDepthSeqPendingCount() {
      this.fullDepthSeqPendingCount += 1;
    }

    public void incrementFullDepthSeqPendingQcCount() {
      this.fullDepthSeqPendingQcCount += 1;

    }

    public void incrementFullDepthSeqCompletedCount() {
      this.fullDepthSeqCompletedCount += 1;
    }

    // informatics review
    public void informaticsPendingCount(int informaticsPendingCount) {
      this.informaticsPendingCount = informaticsPendingCount;
    }

    public void informaticsCompletedCount(int informaticsCompletedCount) {
      this.informaticsCompletedCount = informaticsCompletedCount;
    }

    // draft report
    public void draftReportPendingCount(int draftReportPendingCount) {
      this.draftReportPendingCount = draftReportPendingCount;
    }

    public void draftReportCompletedCount(int draftReportCompletedCount) {
      this.draftReportCompletedCount = draftReportCompletedCount;
    }

    // final report
    public void finalReportPendingCount(int finalReportPendingCount) {
      this.finalReportPendingCount = finalReportPendingCount;
    }

    public void finalReportCompletedCount(int finalReportCompletedCount) {
      this.finalReportCompletedCount = finalReportCompletedCount;
    }



    public Builder addCounts(ProjectSummary.Builder builder) {
      this.totalTestCount += builder.totalTestCount;
      // receipt
      this.receiptPendingQcCount += builder.receiptPendingQcCount;
      this.receiptCompletedCount += builder.receiptCompletedCount;
      // extraction
      this.extractionPendingCount += builder.extractionPendingCount;
      this.extractionPendingQcCount += builder.extractionPendingQcCount;
      this.extractionCompletedCount += builder.extractionCompletedCount;
      // library preparation
      this.libraryPrepPendingCount += builder.libraryPrepPendingCount;
      this.libraryPrepPendingQcCount += builder.libraryPrepPendingQcCount;
      this.libraryPrepCompletedCount += builder.libraryPrepCompletedCount;
      // library qualification
      this.libraryQualPendingCount += builder.libraryQualPendingCount;
      this.libraryQualPendingQcCount += builder.libraryQualPendingQcCount;
      this.libraryQualCompletedCount += builder.libraryQualCompletedCount;
      // full depth sequencing
      this.fullDepthSeqPendingCount += builder.fullDepthSeqPendingCount;
      this.fullDepthSeqPendingQcCount += builder.fullDepthSeqPendingQcCount;
      this.fullDepthSeqCompletedCount += builder.fullDepthSeqCompletedCount;
      // informatics review
      this.informaticsPendingCount += builder.informaticsPendingCount;
      this.informaticsCompletedCount += builder.informaticsCompletedCount;
      // draft report
      this.draftReportPendingCount += builder.draftReportPendingCount;
      this.draftReportCompletedCount += builder.draftReportCompletedCount;
      // final report
      this.finalReportPendingCount += builder.finalReportPendingCount;
      this.finalReportCompletedCount += builder.finalReportCompletedCount;
      return this;
    }

    public ProjectSummary build() {
      return new ProjectSummary(this);
    }
  }
}
