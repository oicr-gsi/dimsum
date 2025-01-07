package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import java.util.Set;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Sample;

public record ExternalSample(String id, String name, String project, Donor donor,
    String secondaryId, String groupId, String timepoint, String tissueType, String tissueOrigin,
    String tissueMaterial, String nucleicAcidType, String libraryDesignCode, Long requisitionId,
    String requisitionName, Set<Long> assayIds, LocalDate createdDate, LocalDate qcDate,
    Boolean qcPassed, String qcReason, LocalDate dataReviewDate, Boolean dataReviewPassed,
    ExternalRun run, String sequencingLane, LocalDate latestActivityDate) {

  public ExternalSample(Sample from) {
    this(from.getId(),
        from.getName(),
        from.getProject(),
        from.getDonor(),
        from.getSecondaryId(),
        from.getGroupId(),
        from.getTimepoint(),
        from.getTissueType(),
        from.getTissueOrigin(),
        from.getTissueMaterial(),
        from.getNucleicAcidType(),
        from.getLibraryDesignCode(),
        from.getRequisitionId(),
        from.getRequisitionName(),
        from.getAssayIds(),
        from.getCreatedDate(),
        from.getQcDate(),
        from.getQcPassed(),
        from.getQcReason(),
        from.getDataReviewDate(),
        from.getDataReviewPassed(),
        new ExternalRun(from.getRun()),
        from.getSequencingLane(),
        from.getLatestActivityDate());
  }

}
