package ca.on.oicr.gsi.dimsum.data;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.List;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Requisition {

  private final long id;
  private final String name;
  private final boolean stopped;
  private final List<RequisitionQc> informaticsReviews;
  private final List<RequisitionQc> draftReports;
  private final List<RequisitionQc> finalReports;

  private Requisition(Builder builder) {
    this.id = requireNonNull(builder.id);
    this.name = requireNonNull(builder.name);
    this.stopped = builder.stopped;
    this.informaticsReviews = builder.informaticsReviews == null ? emptyList()
        : unmodifiableList(builder.informaticsReviews);
    this.draftReports = builder.draftReports == null ? emptyList()
        : unmodifiableList(builder.draftReports);
    this.finalReports = builder.finalReports == null ? emptyList()
        : unmodifiableList(builder.finalReports);
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public boolean isStopped() {
    return stopped;
  }

  public List<RequisitionQc> getInformationReviews() {
    return informaticsReviews;
  }

  public List<RequisitionQc> getDraftReports() {
    return draftReports;
  }

  public List<RequisitionQc> getFinalReports() {
    return finalReports;
  }

  public static class Builder {

    private long id;
    private String name;
    private boolean stopped;
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

    public Builder stopped(boolean stopped) {
      this.stopped = stopped;
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
