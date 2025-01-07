package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;

// Note: internal models for donor, project, and requisition contain no sensitive fields
public record ExternalCase(String id, Donor donor, Set<Project> projects, String tissueType,
    String tissueOrigin, String timepoint, Requisition requisition, long assayId, String assayName,
    String assayDescription, List<ExternalSample> receipts, List<ExternalTest> tests,
    List<ExternalCaseDeliverable> deliverables, LocalDate startDate, LocalDate latestActivityDate) {

  public ExternalCase(Case from) {
    this(from.getId(),
        from.getDonor(),
        from.getProjects(),
        from.getTissueType(),
        from.getTissueOrigin(),
        from.getTimepoint(),
        from.getRequisition(),
        from.getAssayId(),
        from.getAssayName(),
        from.getAssayDescription(),
        from.getReceipts().stream().map(ExternalSample::new)
            .collect(Collectors.toUnmodifiableList()),
        from.getTests().stream().map(ExternalTest::new).collect(Collectors.toUnmodifiableList()),
        from.getDeliverables().stream().map(ExternalCaseDeliverable::new)
            .collect(Collectors.toUnmodifiableList()),
        from.getStartDate(),
        from.getLatestActivityDate());
  }

}
