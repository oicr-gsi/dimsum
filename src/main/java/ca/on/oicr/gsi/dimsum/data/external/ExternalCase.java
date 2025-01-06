package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;

public class ExternalCase {

  private final String id;
  private final Donor donor; // Internal model has no sensitive fields
  private final Set<Project> projects; // Internal model has no sensitive fields
  private final String tissueType;
  private final String tissueOrigin;
  private final String timepoint;
  private final Requisition requisition; // Internal model has no sensitive fields
  private final long assayId;
  private final String assayName;
  private final String assayDescription;
  private final List<ExternalSample> receipts;
  private final List<ExternalTest> tests;
  private final List<ExternalCaseDeliverable> deliverables;
  private final LocalDate startDate;
  private final LocalDate latestActivityDate;

  public ExternalCase(Case from) {
    id = from.getId();
    donor = from.getDonor();
    projects = from.getProjects();
    tissueType = from.getTissueType();
    tissueOrigin = from.getTissueOrigin();
    timepoint = from.getTimepoint();
    requisition = from.getRequisition();
    assayId = from.getAssayId();
    assayName = from.getAssayName();
    assayDescription = from.getAssayDescription();
    receipts = from.getReceipts().stream().map(ExternalSample::new)
        .collect(Collectors.toUnmodifiableList());
    tests =
        from.getTests().stream().map(ExternalTest::new).collect(Collectors.toUnmodifiableList());
    deliverables = from.getDeliverables().stream().map(ExternalCaseDeliverable::new)
        .collect(Collectors.toUnmodifiableList());
    startDate = from.getStartDate();
    latestActivityDate = from.getLatestActivityDate();
  }

  public String getId() {
    return id;
  }

  public Donor getDonor() {
    return donor;
  }

  public Set<Project> getProjects() {
    return projects;
  }

  public String getTissueType() {
    return tissueType;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public String getTimepoint() {
    return timepoint;
  }

  public Requisition getRequisition() {
    return requisition;
  }

  public long getAssayId() {
    return assayId;
  }

  public String getAssayName() {
    return assayName;
  }

  public String getAssayDescription() {
    return assayDescription;
  }

  public List<ExternalSample> getReceipts() {
    return receipts;
  }

  public List<ExternalTest> getTests() {
    return tests;
  }

  public List<ExternalCaseDeliverable> getDeliverables() {
    return deliverables;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getLatestActivityDate() {
    return latestActivityDate;
  }

}
