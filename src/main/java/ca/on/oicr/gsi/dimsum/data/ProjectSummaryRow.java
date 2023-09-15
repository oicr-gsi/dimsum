package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import groovy.transform.Immutable;

@Immutable
@JsonDeserialize(builder = ProjectSummaryRow.Builder.class)
public class ProjectSummaryRow {
  private final String title;
  private final ProjectSummaryField receipt;
  private final ProjectSummaryField extraction;
  private final ProjectSummaryField libraryPreparation;
  private final ProjectSummaryField libraryQualification;
  private final ProjectSummaryField fullDepthSequencing;
  private final ProjectSummaryField analysisReview;
  private final ProjectSummaryField releaseApproval;
  private final ProjectSummaryField release;

  private ProjectSummaryRow(Builder builder) {
    this.title = requireNonNull(builder.title);
    this.receipt = builder.receipt;
    this.extraction = builder.extraction;
    this.libraryPreparation = builder.libraryPreparation;
    this.libraryQualification = builder.libraryQualification;
    this.fullDepthSequencing = builder.fullDepthSequencing;
    this.analysisReview = builder.analysisReview;
    this.releaseApproval = builder.releaseApproval;
    this.release = builder.release;
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

  public ProjectSummaryField getAnalysisReview() {
    return analysisReview;
  }

  public ProjectSummaryField getReleaseApproval() {
    return releaseApproval;
  }

  public ProjectSummaryField getRelease() {
    return release;
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class Builder {
    private String title;
    private ProjectSummaryField receipt;
    private ProjectSummaryField extraction;
    private ProjectSummaryField libraryPreparation;
    private ProjectSummaryField libraryQualification;
    private ProjectSummaryField fullDepthSequencing;
    private ProjectSummaryField analysisReview;
    private ProjectSummaryField releaseApproval;
    private ProjectSummaryField release;

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

    public Builder analysisReview(ProjectSummaryField analysisReview) {
      this.analysisReview = analysisReview;
      return this;
    }

    public Builder releaseApproval(ProjectSummaryField releaseApproval) {
      this.releaseApproval = releaseApproval;
      return this;
    }

    public Builder release(ProjectSummaryField release) {
      this.release = release;
      return this;
    }

    public ProjectSummaryRow build() {
      return new ProjectSummaryRow(this);
    }
  }

}
