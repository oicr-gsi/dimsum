package ca.on.oicr.gsi.dimsum.data;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Requisition {

  private final long id;
  private final String name;
  private final Long assayId;
  private final boolean stopped;
  private final String stopReason;
  private final List<RequisitionQcGroup> qcGroups;
  private final List<RequisitionQc> informaticsReviews;
  private final List<RequisitionQc> draftReports;
  private final List<RequisitionQc> finalReports;
  private final LocalDate latestActivityDate;

  private Requisition(Builder builder) {
    this.id = requireNonNull(builder.id);
    this.name = requireNonNull(builder.name);
    this.assayId = builder.assayId;
    this.stopped = builder.stopped;
    this.stopReason = builder.stopReason;
    this.qcGroups = builder.qcGroups == null ? emptyList() : unmodifiableList(builder.qcGroups);
    this.informaticsReviews = builder.informaticsReviews == null ? emptyList()
        : unmodifiableList(builder.informaticsReviews);
    this.draftReports =
        builder.draftReports == null ? emptyList() : unmodifiableList(builder.draftReports);
    this.finalReports =
        builder.finalReports == null ? emptyList() : unmodifiableList(builder.finalReports);
    this.latestActivityDate =
        Stream.of(informaticsReviews.stream(), draftReports.stream(), finalReports.stream())
            .flatMap(Function.identity()).map(RequisitionQc::getQcDate).max(LocalDate::compareTo)
            .orElse(null);
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getAssayId() {
    return assayId;
  }

  public boolean isStopped() {
    return stopped;
  }

  public String getStopReason() {
    return stopReason;
  }

  public List<RequisitionQcGroup> getQcGroups() {
    return qcGroups;
  }

  public List<RequisitionQc> getInformaticsReviews() {
    return informaticsReviews;
  }

  public List<RequisitionQc> getDraftReports() {
    return draftReports;
  }

  public List<RequisitionQc> getFinalReports() {
    return finalReports;
  }

  public LocalDate getLatestActivityDate() {
    return latestActivityDate;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Requisition other = (Requisition) obj;
    return Objects.equals(id, other.id);
  }

  public static class Builder {

    private long id;
    private String name;
    private boolean stopped;
    private String stopReason;
    private Long assayId;
    private List<RequisitionQcGroup> qcGroups;
    private List<RequisitionQc> informaticsReviews;
    private List<RequisitionQc> draftReports;
    private List<RequisitionQc> finalReports;

    public Builder id(long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder assayId(Long assayId) {
      this.assayId = assayId;
      return this;
    }

    public Builder stopped(boolean stopped) {
      this.stopped = stopped;
      return this;
    }

    public Builder stopReason(String stopReason) {
      this.stopReason = stopReason;
      return this;
    }

    public Builder qcGroups(List<RequisitionQcGroup> qcGroups) {
      this.qcGroups = qcGroups;
      return this;
    }

    public Builder informaticsReviews(List<RequisitionQc> informaticsReviews) {
      this.informaticsReviews = informaticsReviews;
      return this;
    }

    public Builder draftReports(List<RequisitionQc> draftReports) {
      this.draftReports = draftReports;
      return this;
    }

    public Builder finalReports(List<RequisitionQc> finalReports) {
      this.finalReports = finalReports;
      return this;
    }

    public Requisition build() {
      return new Requisition(this);
    }
  }
}
