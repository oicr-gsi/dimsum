package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Sample {

  private final String id;
  private final String name;
  private final Long requisitionId;
  private final String requisitionName;
  private final Long assayId;
  private final String tissueOrigin;
  private final String tissueType;
  private final String tissueMaterial;
  private final String timepoint;
  private final String secondaryId;
  private final String groupId;
  private final String project;
  private final String nucleicAcidType;
  private final Integer librarySize;
  private final String libraryDesignCode;
  private final String targetedSequencing;
  private final LocalDate createdDate;
  private final BigDecimal volume;
  private final BigDecimal concentration;
  private final String concentrationUnits;
  private final Run run;
  private final Donor donor;
  private final BigDecimal meanInsertSize;
  private final Integer clustersPerSample; // AKA "Pass Filter Clusters" for full-depth (call ready)
  private final BigDecimal duplicationRate;
  private final BigDecimal meanCoverageDeduplicated;
  private final BigDecimal rRnaContamination;
  private final BigDecimal mappedToCoding;
  private final BigDecimal rawCoverage;
  private final BigDecimal onTargetReads;
  private final Boolean qcPassed;
  private final String qcReason;
  private final String qcUser;
  private final LocalDate qcDate;
  private final Boolean dataReviewPassed;
  private final String dataReviewUser;
  private final LocalDate dataReviewDate;
  private final LocalDate latestActivityDate;
  private final String sequencingLane;

  private Sample(Builder builder) {
    this.id = requireNonNull(builder.id);
    this.name = requireNonNull(builder.name);
    this.requisitionId = builder.requisition == null ? null : builder.requisition.getId();
    this.requisitionName = builder.requisition == null ? null : builder.requisition.getName();
    this.assayId = builder.requisition == null ? null : builder.requisition.getAssayId();
    this.tissueOrigin = requireNonNull(builder.tissueOrigin);
    this.tissueType = requireNonNull(builder.tissueType);
    this.tissueMaterial = builder.tissueMaterial;
    this.timepoint = builder.timepoint;
    this.secondaryId = builder.secondaryId;
    this.groupId = builder.groupId;
    this.project = requireNonNull(builder.project);
    this.nucleicAcidType = builder.nucleicAcidType;
    this.librarySize = builder.librarySize;
    this.libraryDesignCode = builder.libraryDesignCode;
    this.targetedSequencing = builder.targetedSequencing;
    this.createdDate = requireNonNull(builder.createdDate);
    this.volume = builder.volume;
    this.concentration = builder.concentration;
    this.concentrationUnits = builder.concentrationUnits;
    this.run = builder.run;
    this.donor = requireNonNull(builder.donor);
    this.meanInsertSize = builder.meanInsertSize;
    this.clustersPerSample = builder.clustersPerSample;
    this.duplicationRate = builder.duplicationRate;
    this.meanCoverageDeduplicated = builder.meanCoverageDeduplicated;
    this.rRnaContamination = builder.rRnaContamination;
    this.mappedToCoding = builder.mappedToCoding;
    this.rawCoverage = builder.rawCoverage;
    this.onTargetReads = builder.onTargetReads;
    this.qcPassed = builder.qcPassed;
    this.qcReason = builder.qcReason;
    this.qcUser = builder.qcUser;
    this.qcDate = builder.qcDate;
    this.dataReviewPassed = builder.dataReviewPassed;
    this.dataReviewUser = builder.dataReviewUser;
    this.dataReviewDate = builder.dataReviewDate;
    this.sequencingLane = builder.sequencingLane;
    this.latestActivityDate = Stream.of(createdDate, qcDate, dataReviewDate)
        .filter(Objects::nonNull).max(LocalDate::compareTo).orElseThrow();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getRequisitionId() {
    return requisitionId;
  }

  public String getRequisitionName() {
    return requisitionName;
  }

  public Long getAssayId() {
    return assayId;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public String getTissueType() {
    return tissueType;
  }

  public String getTissueMaterial() {
    return tissueMaterial;
  }

  public String getTimepoint() {
    return timepoint;
  }

  public String getSecondaryId() {
    return secondaryId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getProject() {
    return project;
  }

  public String getNucleicAcidType() {
    return nucleicAcidType;
  }

  public Integer getLibrarySize() {
    return librarySize;
  }

  public String getLibraryDesignCode() {
    return libraryDesignCode;
  }

  public String getTargetedSequencing() {
    return targetedSequencing;
  }

  public LocalDate getCreatedDate() {
    return createdDate;
  }

  public BigDecimal getVolume() {
    return volume;
  }

  public BigDecimal getConcentration() {
    return concentration;
  }

  public String getConcentrationUnits() {
    return concentrationUnits;
  }

  public Run getRun() {
    return run;
  }

  public Donor getDonor() {
    return donor;
  }

  public BigDecimal getMeanInsertSize() {
    return meanInsertSize;
  }

  public Integer getClustersPerSample() {
    return clustersPerSample;
  }

  public BigDecimal getDuplicationRate() {
    return duplicationRate;
  }

  public BigDecimal getMeanCoverageDeduplicated() {
    return meanCoverageDeduplicated;
  }

  public BigDecimal getrRnaContamination() {
    return rRnaContamination;
  }

  public BigDecimal getMappedToCoding() {
    return mappedToCoding;
  }

  public BigDecimal getRawCoverage() {
    return rawCoverage;
  }

  public BigDecimal getOnTargetReads() {
    return onTargetReads;
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

  public LocalDate getLatestActivityDate() {
    return latestActivityDate;
  }

  public String getSequencingLane() {
    return sequencingLane;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, run, sequencingLane);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Sample other = (Sample) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(run, other.run)
        && Objects.equals(sequencingLane, other.sequencingLane);
  }

  public static class Builder {

    private String id;
    private String name;
    private Requisition requisition;
    private String tissueOrigin;
    private String tissueType;
    private String tissueMaterial;
    private String timepoint;
    private String secondaryId;
    private String groupId;
    private String project;
    private String nucleicAcidType;
    private Integer librarySize;
    private String libraryDesignCode;
    private String targetedSequencing;
    private LocalDate createdDate;
    private BigDecimal volume;
    private BigDecimal concentration;
    private String concentrationUnits;
    private Run run;
    private Donor donor;
    private BigDecimal meanInsertSize;
    private Integer clustersPerSample;
    private BigDecimal duplicationRate;
    private BigDecimal meanCoverageDeduplicated;
    private BigDecimal rRnaContamination;
    private BigDecimal mappedToCoding;
    private BigDecimal rawCoverage;
    private BigDecimal onTargetReads;
    private Boolean qcPassed;
    private String qcReason;
    private String qcUser;
    private LocalDate qcDate;
    private Boolean dataReviewPassed;
    private String dataReviewUser;
    private LocalDate dataReviewDate;
    private String sequencingLane;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder requisition(Requisition requisition) {
      this.requisition = requisition;
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

    public Builder tissueMaterial(String tissueMaterial) {
      this.tissueMaterial = tissueMaterial;
      return this;
    }

    public Builder timepoint(String timepoint) {
      this.timepoint = timepoint;
      return this;
    }

    public Builder secondaryId(String secondaryId) {
      this.secondaryId = secondaryId;
      return this;
    }

    public Builder groupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public Builder project(String project) {
      this.project = project;
      return this;
    }

    public Builder nucleicAcidType(String nucleicAcidType) {
      this.nucleicAcidType = nucleicAcidType;
      return this;
    }

    public Builder librarySize(Integer librarySize) {
      this.librarySize = librarySize;
      return this;
    }

    public Builder libraryDesignCode(String libraryDesignCode) {
      this.libraryDesignCode = libraryDesignCode;
      return this;
    }

    public Builder targetedSequencing(String targetedSequencing) {
      this.targetedSequencing = targetedSequencing;
      return this;
    }

    public Builder createdDate(LocalDate createdDate) {
      this.createdDate = createdDate;
      return this;
    }

    public Builder volume(BigDecimal volume) {
      this.volume = volume;
      return this;
    }

    public Builder concentration(BigDecimal concentration) {
      this.concentration = concentration;
      return this;
    }

    public Builder concentrationUnits(String concentrationUnits) {
      this.concentrationUnits = concentrationUnits;
      return this;
    }

    public Builder run(Run run) {
      this.run = run;
      return this;
    }

    public Builder donor(Donor donor) {
      this.donor = donor;
      return this;
    }

    public Builder meanInsertSize(BigDecimal meanInsertSize) {
      this.meanInsertSize = meanInsertSize;
      return this;
    }

    public Builder clustersPerSample(Integer clustersPerSample) {
      this.clustersPerSample = clustersPerSample;
      return this;
    }

    public Builder duplicationRate(BigDecimal duplicationRate) {
      this.duplicationRate = duplicationRate;
      return this;
    }

    public Builder meanCoverageDeduplicated(BigDecimal meanCoverageDeduplicated) {
      this.meanCoverageDeduplicated = meanCoverageDeduplicated;
      return this;
    }

    public Builder rRnaContamination(BigDecimal rRnaContamination) {
      this.rRnaContamination = rRnaContamination;
      return this;
    }

    public Builder mappedToCoding(BigDecimal mappedToCoding) {
      this.mappedToCoding = mappedToCoding;
      return this;
    }

    public Builder rawCoverage(BigDecimal rawCoverage) {
      this.rawCoverage = rawCoverage;
      return this;
    }

    public Builder onTargetReads(BigDecimal onTargetReads) {
      this.onTargetReads = onTargetReads;
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

    public Builder sequencingLane(String sequencingLane) {
      this.sequencingLane = sequencingLane;
      return this;
    }

    public Sample build() {
      return new Sample(this);
    }

  }
}
