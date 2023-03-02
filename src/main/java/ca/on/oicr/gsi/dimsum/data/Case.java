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
import com.fasterxml.jackson.annotation.JsonIgnore;

@Immutable
public class Case {

  private final String id;
  private final Donor donor;
  private final Set<Project> projects;
  private final Assay assay;
  private final long assayId;
  private final String tissueOrigin;
  private final String tissueType;
  private final String timepoint;
  private final boolean stopped;
  private final List<Sample> receipts;
  private final LocalDate startDate;
  private final List<Test> tests;
  private final Requisition requisition;
  private final LocalDate latestActivityDate;

  private Case(Builder builder) {
    this.id = requireNonNull(builder.id);
    this.donor = requireNonNull(builder.donor);
    this.projects = unmodifiableSet(builder.projects);
    this.assay = requireNonNull(builder.assay);
    this.assayId = this.assay.getId();
    this.tissueOrigin = requireNonNull(builder.tissueOrigin);
    this.tissueType = requireNonNull(builder.tissueType);
    this.timepoint = builder.timepoint;
    this.stopped = builder.stopped;
    this.receipts = unmodifiableList(builder.receipts);
    this.tests = unmodifiableList(builder.tests);
    this.requisition = builder.requisition;
    this.startDate = builder.receipts.stream()
        .filter(sample -> !"R".equals(sample.getTissueType()))
        .map(Sample::getCreatedDate)
        .min(LocalDate::compareTo).orElse(null);
    this.latestActivityDate = Stream
        .of(receipts.stream().map(Sample::getLatestActivityDate),
            tests.stream().map(Test::getLatestActivityDate),
            Stream.of(requisition.getLatestActivityDate()))
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

  @JsonIgnore
  public Assay getAssay() {
    return assay;
  }

  public long getAssayId() {
    return assayId;
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

  public Requisition getRequisition() {
    return requisition;
  }

  public LocalDate getLatestActivityDate() {
    return latestActivityDate;
  }

  public static class Builder {

    private String id;
    private Donor donor;
    private Set<Project> projects;
    private Assay assay;
    private String tissueOrigin;
    private String tissueType;
    private String timepoint;
    private boolean stopped;
    private List<Sample> receipts;
    private List<Test> tests;
    private Requisition requisition;

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

    public Builder assay(Assay assay) {
      this.assay = assay;
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

    public Builder requisition(Requisition requisition) {
      this.requisition = requisition;
      return this;
    }

    public Case build() {
      return new Case(this);
    }
  }
}
