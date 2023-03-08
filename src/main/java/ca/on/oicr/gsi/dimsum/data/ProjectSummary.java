package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;

@Immutable
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

  public ProjectSummary(Builder builder) {
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

  public static class Builder {

    private String name;
    private int totalTestCount;
    // receipt
    private int receiptPendingQcCount;
    private int receiptCompletedCount;
    // extraction
    private int extractionPendingCount;
    private int extractionPendingQcCount;
    private int extractionCompletedCount;
    // library preparation
    private int libraryPrepPendingCount;
    private int libraryPrepPendingQcCount;
    private int libraryPrepCompletedCount;
    // library qualification
    private int libraryQualPendingCount;
    private int libraryQualPendingQcCount;
    private int libraryQualCompletedCount;
    // full depth sequencing
    private int fullDepthSeqPendingCount;
    private int fullDepthSeqPendingQcCount;
    private int fullDepthSeqCompletedCount;
    // informatics review
    private int informaticsPendingCount;
    private int informaticsCompletedCount;
    // draft report
    private int draftReportPendingCount;
    private int draftReportCompletedCount;
    // final report
    private int finalReportPendingCount;
    private int finalReportCompletedCount;


    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder totalTestCount(int totalTestCount) {
      this.totalTestCount = totalTestCount;
      return this;
    }

    public Builder receiptPendingQcCount(int receiptPendingQcCount) {
      this.receiptPendingQcCount = receiptPendingQcCount;
      return this;
    }

    public Builder receiptCompletedCount(int receiptCompletedCount) {
      this.receiptCompletedCount = receiptCompletedCount;
      return this;
    }

    // extraction
    public Builder extractionPendingCount(int extractionPendingCount) {
      this.extractionPendingCount = extractionPendingCount;
      return this;
    }

    public Builder extractionPendingQcCount(int extractionPendingQcCount) {
      this.extractionPendingQcCount = extractionPendingQcCount;
      return this;
    }

    public Builder extractionCompletedCount(int extractionCompletedCount) {
      this.extractionCompletedCount = extractionCompletedCount;
      return this;
    }

    // library preparation
    public Builder libraryPrepPendingCount(int libraryPrepPendingCount) {
      this.libraryPrepPendingCount = libraryPrepPendingCount;
      return this;
    }

    public Builder libraryPrepPendingQcCount(int libraryPrepPendingQcCount) {
      this.libraryPrepPendingQcCount = libraryPrepPendingQcCount;
      return this;
    }

    public Builder libraryPrepCompletedCount(int libraryPrepCompletedCount) {
      this.libraryPrepCompletedCount = libraryPrepCompletedCount;
      return this;
    }

    // library qualification
    public Builder libraryQualPendingCount(int libraryQualPendingCount) {
      this.libraryQualPendingCount = libraryQualPendingCount;
      return this;
    }

    public Builder libraryQualPendingQcCount(int libraryQualPendingQcCount) {
      this.libraryQualPendingQcCount = libraryQualPendingQcCount;
      return this;
    }

    public Builder libraryQualCompletedCount(int libraryQualCompletedCount) {
      this.libraryQualCompletedCount = libraryQualCompletedCount;
      return this;
    }

    // full depth sequencing
    public Builder fullDepthSeqPendingCount(int fullDepthSeqPendingCount) {
      this.fullDepthSeqPendingCount = fullDepthSeqPendingCount;
      return this;
    }

    public Builder fullDepthSeqPendingQcCount(int fullDepthSeqPendingQcCount) {
      this.fullDepthSeqPendingQcCount = fullDepthSeqPendingQcCount;
      return this;
    }

    public Builder fullDepthSeqCompletedCount(int fullDepthSeqCompletedCount) {
      this.fullDepthSeqCompletedCount = fullDepthSeqCompletedCount;
      return this;
    }

    // informatics review
    public Builder informaticsPendingCount(int informaticsPendingCount) {
      this.informaticsPendingCount = informaticsPendingCount;
      return this;
    }

    public Builder informaticsCompletedCount(int informaticsCompletedCount) {
      this.informaticsCompletedCount = informaticsCompletedCount;
      return this;
    }

    // draft report
    public Builder draftReportPendingCount(int draftReportPendingCount) {
      this.draftReportPendingCount = draftReportPendingCount;
      return this;
    }

    public Builder draftReportCompletedCount(int draftReportCompletedCount) {
      this.draftReportCompletedCount = draftReportCompletedCount;
      return this;
    }

    // final report
    public Builder finalReportPendingCount(int finalReportPendingCount) {
      this.finalReportPendingCount = finalReportPendingCount;
      return this;
    }

    public Builder finalReportCompletedCount(int finalReportCompletedCount) {
      this.finalReportCompletedCount = finalReportCompletedCount;
      return this;
    }



    public void addCounts(ProjectSummary.Builder builder) {
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
    }

    public ProjectSummary build() {
      return new ProjectSummary(this);
    }
  }
}
