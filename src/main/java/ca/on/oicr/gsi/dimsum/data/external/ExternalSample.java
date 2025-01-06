package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import java.util.Set;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Sample;

public class ExternalSample {

  private final String id;
  private final String name;
  private final String project;
  private final Donor donor; // Internal model has no sensitive fields
  private final String secondaryId;
  private final String groupId;
  private final String timepoint;
  private final String tissueType;
  private final String tissueOrigin;
  private final String tissueMaterial;
  private final String libraryDesignCode;
  private final Long requisitionId;
  private final String requisitionName;
  private final Set<Long> assayIds;
  private final LocalDate createdDate;
  private final LocalDate qcDate;
  private final Boolean qcPassed;
  private final String qcReason;
  private final LocalDate dataReviewDate;
  private final Boolean dataReviewPassed;
  private final ExternalRun run;
  private final String sequencingLane;

  public ExternalSample(Sample from) {
    id = from.getId();
    name = from.getName();
    project = from.getProject();
    donor = from.getDonor();
    secondaryId = from.getSecondaryId();
    groupId = from.getGroupId();
    timepoint = from.getTimepoint();
    tissueType = from.getTissueType();
    tissueOrigin = from.getTissueOrigin();
    tissueMaterial = from.getTissueMaterial();
    libraryDesignCode = from.getLibraryDesignCode();
    requisitionId = from.getRequisitionId();
    requisitionName = from.getRequisitionName();
    assayIds = from.getAssayIds();
    createdDate = from.getCreatedDate();
    qcDate = from.getQcDate();
    qcPassed = from.getQcPassed();
    qcReason = from.getQcReason();
    dataReviewDate = from.getDataReviewDate();
    dataReviewPassed = from.getDataReviewPassed();
    run = from.getRun() == null ? null : new ExternalRun(from.getRun());
    sequencingLane = from.getSequencingLane();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getProject() {
    return project;
  }

  public Donor getDonor() {
    return donor;
  }

  public String getSecondaryId() {
    return secondaryId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getTimepoint() {
    return timepoint;
  }

  public String getTissueType() {
    return tissueType;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public String getTissueMaterial() {
    return tissueMaterial;
  }

  public String getLibraryDesignCode() {
    return libraryDesignCode;
  }

  public Long getRequisitionId() {
    return requisitionId;
  }

  public String getRequisitionName() {
    return requisitionName;
  }

  public Set<Long> getAssayIds() {
    return assayIds;
  }

  public LocalDate getCreatedDate() {
    return createdDate;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

  public Boolean getQcPassed() {
    return qcPassed;
  }

  public String getQcReason() {
    return qcReason;
  }

  public LocalDate getDataReviewDate() {
    return dataReviewDate;
  }

  public Boolean getDataReviewPassed() {
    return dataReviewPassed;
  }

  public ExternalRun getRun() {
    return run;
  }

  public String getSequencingLane() {
    return sequencingLane;
  }

}
