package ca.on.oicr.gsi.dimsum.data;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Case {

  private final Donor donor;
  private final Set<Project> projects;
  private final String assayName;
  private final String assayDescription;
  private final String tissueOrigin;
  private final String tissueType;
  private final String timepoint;
  private final boolean stopped;
  private final List<Sample> receipts;
  private final List<Test> tests;
  private final List<Requisition> requisitions;

  private Case(Builder builder) {
    this.donor = requireNonNull(builder.donor);
    this.projects = unmodifiableSet(builder.projects);
    this.assayName = requireNonNull(builder.assayName);
    this.assayDescription = requireNonNull(builder.assayDescription);
    this.tissueOrigin = requireNonNull(builder.tissueOrigin);
    this.tissueType = requireNonNull(builder.tissueType);
    this.timepoint = builder.timepoint;
    this.stopped = builder.stopped;
    this.receipts = unmodifiableList(builder.receipts);
    this.tests = unmodifiableList(builder.tests);
    this.requisitions = unmodifiableList(builder.requisitions);
  }

  public Donor getDonor() {
    return donor;
  }

  public Set<Project> getProjects() {
    return projects;
  }

  public String getAssayName() {
    return assayName;
  }

  public String getAssayDescription() {
    return assayDescription;
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

  public boolean isStopped() {
    return stopped;
  }

  public List<Sample> getReceipts() {
    return receipts;
  }

  public List<Test> getTests() {
    return tests;
  }

  public List<Requisition> getRequisitions() {
    return requisitions;
  }

  public static class Builder {

    private Donor donor;
    private Set<Project> projects;
    private String assayName;
    private String assayDescription;
    private String tissueOrigin;
    private String tissueType;
    private String timepoint;
    private boolean stopped;
    private List<Sample> receipts;
    private List<Test> tests;
    private List<Requisition> requisitions;

    public Builder donor(Donor donor) {
      this.donor = donor;
      return this;
    }

    public Builder projects(Set<Project> projects) {
      this.projects = projects;
      return this;
    }

    public Builder assayName(String assayName) {
      this.assayName = assayName;
      return this;
    }

    public Builder assayDescription(String assayDescription) {
      this.assayDescription = assayDescription;
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

    public Builder stopped(boolean stopped) {
      this.stopped = stopped;
      return this;
    }

    public Builder receipts(List<Sample> receipts) {
      this.receipts = receipts;
      return this;
    }

    public Builder tests(List<Test> tests) {
      this.tests = tests;
      return this;
    }

    public Builder requisitions(List<Requisition> requisitions) {
      this.requisitions = requisitions;
      return this;
    }

    public Case build() {
      return new Case(this);
    }
  }
}
