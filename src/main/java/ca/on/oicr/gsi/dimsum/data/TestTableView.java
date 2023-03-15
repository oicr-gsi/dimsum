package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

@Immutable
public class TestTableView {
  private final Test test;
  private final String caseId;
  private final Requisition requisition;
  private final Donor donor;
  private final Set<Project> projects;
  private final Assay assay;
  private final String tissueOrigin;
  private final String tissueType;
  private final String timepoint;
  private final List<Sample> receipts;
  private final LocalDate latestActivityDate;

  public TestTableView(Case kase, Test test) {
    this.test = requireNonNull(test);
    this.caseId = requireNonNull(kase.getId());
    this.requisition = kase.getRequisition();
    this.donor = requireNonNull(kase.getDonor());
    this.projects = kase.getProjects();
    this.assay = requireNonNull(kase.getAssay());
    this.tissueOrigin = requireNonNull(kase.getTissueOrigin());
    this.tissueType = requireNonNull(kase.getTissueType());
    this.timepoint = kase.getTimepoint();
    this.receipts = kase.getReceipts();
    this.latestActivityDate = kase.getLatestActivityDate();
  }

  public Test getTest() {
    return test;
  }

  public String getCaseId() {
    return caseId;
  }

  public Requisition getRequisition() {
    return requisition;
  }

  public Donor getDonor() {
    return donor;
  }

  public Set<Project> getProjects() {
    return projects;
  }

  public Assay getAssay() {
    return assay;
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

  public List<Sample> getReceipts() {
    return receipts;
  }

  public LocalDate getLatestActivityDate() {
    return latestActivityDate;
  }

}
