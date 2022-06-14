package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Sample {

  private final String id;
  private final String name;
  private final String tissueOrigin;
  private final String tissueType;
  private final String timepoint;
  private final String groupId;
  private final String targetedSequencing;
  private final Boolean qcPassed;
  private final String qcReason;
  private final String qcUser;
  private final LocalDate qcDate;
  private final Boolean dataReviewPassed;
  private final String dataReviewUser;
  private final LocalDate dataReviewDate;

  private Sample(Builder builder) {
    this.id = requireNonNull(builder.id);
    this.name = requireNonNull(builder.name);
    this.tissueOrigin = requireNonNull(builder.tissueOrigin);
    this.tissueType = requireNonNull(builder.tissueType);
    this.timepoint = builder.timepoint;
    this.groupId = builder.groupId;
    this.targetedSequencing = builder.targetedSequencing;
    this.qcPassed = builder.qcPassed;
    this.qcReason = builder.qcReason;
    this.qcUser = builder.qcUser;
    this.qcDate = builder.qcDate;
    this.dataReviewPassed = builder.dataReviewPassed;
    this.dataReviewUser = builder.dataReviewUser;
    this.dataReviewDate = builder.dataReviewDate;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public String getTissueType() {
    return tissueType;
  }

  public String getTimepoint() {
    return timepoint;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getTargetedSequencing() {
    return targetedSequencing;
  }

  public Boolean getQcPassed() {
    return qcPassed;
  }

  public String getQcReason() {
    return qcReason;
  }

  public String getQcUser() {
    return qcUser;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

  public Boolean getDataReviewPassed() {
    return dataReviewPassed;
  }

  public String getDataReviewUser() {
    return dataReviewUser;
  }

  public LocalDate getDataReviewDate() {
    return dataReviewDate;
  }

  public static class Builder {

    private String id;
    private String name;
    private String tissueOrigin;
    private String tissueType;
    private String timepoint;
    private String groupId;
    private String targetedSequencing;
    private Boolean qcPassed;
    private String qcReason;
    private String qcUser;
    private LocalDate qcDate;
    private Boolean dataReviewPassed;
    private String dataReviewUser;
    private LocalDate dataReviewDate;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder tissueOrigin(String tissueOrigin) {
      this.tissueOrigin = tissueOrigin;
      return this;
    }

    public Builder tissueType(String tissueType) {
      this.tissueType = tissueType;
      return this;
    }

    public Builder timepoint(String timepoint) {
      this.timepoint = timepoint;
      return this;
    }

    public Builder groupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public Builder targetedSequencing(String targetedSequencing) {
      this.targetedSequencing = targetedSequencing;
      return this;
    }

    public Builder qcPassed(Boolean qcPassed) {
      this.qcPassed = qcPassed;
      return this;
    }

    public Builder qcReason(String qcReason) {
      this.qcReason = qcReason;
      return this;
    }

    public Builder qcUser(String qcUser) {
      this.qcUser = qcUser;
      return this;
    }

    public Builder qcDate(LocalDate qcDate) {
      this.qcDate = qcDate;
      return this;
    }

    public Builder dataReviewPassed(Boolean dataReviewPassed) {
      this.dataReviewPassed = dataReviewPassed;
      return this;
    }

    public Builder dataReviewUser(String dataReviewUser) {
      this.dataReviewUser = dataReviewUser;
      return this;
    }

    public Builder dataReviewDate(LocalDate dataReviewDate) {
      this.dataReviewDate = dataReviewDate;
      return this;
    }

    public Sample build() {
      return new Sample(this);
    }

  }
}
