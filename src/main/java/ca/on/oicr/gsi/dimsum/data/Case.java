package ca.on.oicr.gsi.dimsum.data;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Case {

  private final String id;
  private final Donor donor;
  private final Set<Project> projects;
  private final String assayName;
  private final String assayDescription;
  private final String tissueOrigin;
  private final String tissueType;
  private final String timepoint;
  private final boolean stopped;
  private final List<Sample> receipts;
  private final LocalDate startDate;
  private final List<Test> tests;
  private final List<Requisition> requisitions;
  private final LocalDate latestActivityDate;

  private Case(Builder builder) {
    this.id = requireNonNull(builder.id);
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
    this.startDate = builder.receipts.stream()
        .filter(sample -> !"R".equals(sample.getTissueType()))
        .map(Sample::getCreatedDate)
        .min(LocalDate::compareTo).orElse(null);
    this.latestActivityDate = Stream
        .of(receipts.stream().map(Sample::getLatestActivityDate),
            tests.stream().map(Test::getLatestActivityDate),
            requisitions.stream().map(Requisition::getLatestActivityDate))
        .flatMap(Function.identity()).filter(Objects::nonNull).max(LocalDate::compareTo)
        .orElse(null);
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

  public LocalDate getStartDate() {
    return startDate;
  }

  public List<Test> getTests() {
    return tests;
  }

  public List<Requisition> getRequisitions() {
    return requisitions;
  }

  public LocalDate getLatestActivityDate() {
    return latestActivityDate;
  }

  public static class Builder {

    private String id;
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

    public Builder id(String id) {
      this.id = id;
      return this;
    }

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
