package ca.on.oicr.gsi.dimsum.data;

import java.util.List;
import java.util.Set;

public class Case {

  private Donor donor;
  private Set<Project> projects;
  private String assayName;
  private String assayDescription;
  private String tissueOrigin;
  private String tissueType;
  private String timepoint;
  private List<Sample> receipts;
  private List<Test> tests;
  private List<Requisition> requisitions;

  public Donor getDonor() {
    return donor;
  }

  public void setDonor(Donor donor) {
    this.donor = donor;
  }

  public Set<Project> getProjects() {
    return projects;
  }

  public void setProjects(Set<Project> projects) {
    this.projects = projects;
  }

  public String getAssayName() {
    return assayName;
  }

  public void setAssayName(String assayName) {
    this.assayName = assayName;
  }

  public String getAssayDescription() {
    return assayDescription;
  }

  public void setAssayDescription(String assayDescription) {
    this.assayDescription = assayDescription;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public void setTissueOrigin(String tissueOrigin) {
    this.tissueOrigin = tissueOrigin;
  }

  public String getTissueType() {
    return tissueType;
  }

  public void setTissueType(String tissueType) {
    this.tissueType = tissueType;
  }

  public String getTimepoint() {
    return timepoint;
  }

  public void setTimepoint(String timepoint) {
    this.timepoint = timepoint;
  }

  public List<Sample> getReceipts() {
    return receipts;
  }

  public void setReceipts(List<Sample> receipts) {
    this.receipts = receipts;
  }

  public List<Test> getTests() {
    return tests;
  }

  public void setTests(List<Test> tests) {
    this.tests = tests;
  }

  public List<Requisition> getRequisitions() {
    return requisitions;
  }

  public void setRequisitions(List<Requisition> requisitions) {
    this.requisitions = requisitions;
  }
}
