package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;

public record ExternalTestTableView(ExternalTest test, String caseId, Requisition requisition,
    Donor donor, Set<Project> projects, long assayId, String assayName, String tissueOrigin,
    String tissueType, String timepoint, List<ExternalSample> receipts,
    LocalDate latestActivityDate) {

  public ExternalTestTableView(Case kase, Test test) {
    this(new ExternalTest(test),
        kase.getId(),
        kase.getRequisition(),
        kase.getDonor(),
        kase.getProjects(),
        kase.getAssayId(),
        kase.getAssayName(),
        kase.getTissueOrigin(),
        kase.getTissueType(),
        kase.getTimepoint(),
        kase.getReceipts().stream()
            .map(ExternalSample::new)
            .collect(Collectors.toUnmodifiableList()),
        findLatestActivity(test, kase));
  }

  private static LocalDate findLatestActivity(Test test, Case kase) {
    if (test.getLatestActivityDate() != null) {
      return test.getLatestActivityDate();
    } else {
      return kase.getReceipts().stream()
          .map(Sample::getLatestActivityDate)
          .filter(Objects::nonNull)
          .max(LocalDate::compareTo)
          .orElse(null);
    }
  }

}
