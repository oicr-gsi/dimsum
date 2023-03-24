package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import groovy.transform.Immutable;

@Immutable
public class ProjectSummaryRow {
  private final String title;
  private final ProjectSummaryField receipt;
  private final ProjectSummaryField extraction;
  private final ProjectSummaryField libraryPreparation;
  private final ProjectSummaryField libraryQualification;
  private final ProjectSummaryField fullDepthSequencing;
  private final ProjectSummaryField informaticsReview;
  private final ProjectSummaryField draftReport;
  private final ProjectSummaryField finalReport;

  private ProjectSummaryRow(Builder builder) {
    this.title = requireNonNull(builder.title);
    this.receipt = builder.receipt;
    this.extraction = builder.extraction;
    this.libraryPreparation = builder.libraryPreparation;
    this.libraryQualification = builder.libraryQualification;
    this.fullDepthSequencing = builder.fullDepthSequencing;
    this.informaticsReview = builder.informaticsReview;
    this.draftReport = builder.draftReport;
    this.finalReport = builder.finalReport;
  }

  public String getTitle() {
    return title;
  }

  public ProjectSummaryField getReceipt() {
    return receipt;
  }

  public ProjectSummaryField getExtraction() {
    return extraction;
  }

  public ProjectSummaryField getLibraryPreparation() {
    return libraryPreparation;
  }

  public ProjectSummaryField getLibraryQualification() {
    return libraryQualification;
  }

  public ProjectSummaryField getFullDepthSequencing() {
    return fullDepthSequencing;
  }

  public ProjectSummaryField getInformaticsReview() {
    return informaticsReview;
  }

  public ProjectSummaryField getDraftReport() {
    return draftReport;
  }

  public ProjectSummaryField getFinalReport() {
    return finalReport;
  }

  public static class Builder {
    private String title;
    private ProjectSummaryField receipt;
    private ProjectSummaryField extraction;
    private ProjectSummaryField libraryPreparation;
    private ProjectSummaryField libraryQualification;
    private ProjectSummaryField fullDepthSequencing;
    private ProjectSummaryField informaticsReview;
    private ProjectSummaryField draftReport;
    private ProjectSummaryField finalReport;

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder receipt(ProjectSummaryField receipt) {
      this.receipt = receipt;
      return this;
    }

    public Builder extraction(ProjectSummaryField extraction) {
      this.extraction = extraction;
      return this;
    }

    public Builder libraryPreparation(ProjectSummaryField libraryPreparation) {
      this.libraryPreparation = libraryPreparation;
      return this;
    }

    public Builder libraryQualification(ProjectSummaryField libraryQualification) {
      this.libraryQualification = libraryQualification;
      return this;
    }

    public Builder fullDepthSequencing(ProjectSummaryField fullDepthSequencing) {
      this.fullDepthSequencing = fullDepthSequencing;
      return this;
    }

    public Builder informaticsReview(ProjectSummaryField informaticsReview) {
      this.informaticsReview = informaticsReview;
      return this;
    }

    public Builder draftReport(ProjectSummaryField draftReport) {
      this.draftReport = draftReport;
      return this;
    }

    public Builder finalReport(ProjectSummaryField finalReport) {
      this.finalReport = finalReport;
      return this;
    }

    public ProjectSummaryRow build() {
      return new ProjectSummaryRow(this);
    }
  }

}
