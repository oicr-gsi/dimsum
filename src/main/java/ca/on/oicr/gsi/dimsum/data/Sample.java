package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Sample {

  private final String id;
  private final String name;
  private final String tissueOrigin;
  private final String tissueType;
  private final String timepoint;
  private final String groupId;
  private final String targetedSequencing;
  private final LocalDate createdDate;
  private final BigDecimal volume;
  private final BigDecimal concentration;
  private final Run run;
  private final Boolean qcPassed;
  private final String qcReason;
  private final String qcUser;
  private final LocalDate qcDate;
  private final Boolean dataReviewPassed;
  private final String dataReviewUser;
  private final LocalDate dataReviewDate;
  private final LocalDate latestActivityDate;

  private Sample(Builder builder) {
    this.id = requireNonNull(builder.id);
    this.name = requireNonNull(builder.name);
    this.tissueOrigin = requireNonNull(builder.tissueOrigin);
    this.tissueType = requireNonNull(builder.tissueType);
    this.timepoint = builder.timepoint;
    this.groupId = builder.groupId;
    this.targetedSequencing = builder.targetedSequencing;
    this.createdDate = requireNonNull(builder.createdDate);
    this.volume = builder.volume;
    this.concentration = builder.concentration;
    this.run = builder.run;
    this.qcPassed = builder.qcPassed;
    this.qcReason = builder.qcReason;
    this.qcUser = builder.qcUser;
    this.qcDate = builder.qcDate;
    this.dataReviewPassed = builder.dataReviewPassed;
    this.dataReviewUser = builder.dataReviewUser;
    this.dataReviewDate = builder.dataReviewDate;
    this.latestActivityDate = Stream.of(createdDate, qcDate, dataReviewDate)
        .filter(Objects::nonNull).max(LocalDate::compareTo).orElseThrow();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
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

  public String getGroupId() {
    return groupId;
  }

  public String getTargetedSequencing() {
    return targetedSequencing;
  }

  public LocalDate getCreatedDate() {
    return createdDate;
  }

  public BigDecimal getVolume() {
    return volume;
  }

  public BigDecimal getConcentration() {
    return concentration;
  }

  public Run getRun() {
    return run;
  }

  public Boolean getQcPassed() {
    return qcPassed;
  }

  public String getQcReason() {
    return qcReason;
  }

  public String getQcUser() {
    return qcUser;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

  public Boolean getDataReviewPassed() {
    return dataReviewPassed;
  }

  public String getDataReviewUser() {
    return dataReviewUser;
  }

  public LocalDate getDataReviewDate() {
    return dataReviewDate;
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
    Sample other = (Sample) obj;
    return Objects.equals(id, other.id);
  }

  public static class Builder {

    private String id;
    private String name;
    private String tissueOrigin;
    private String tissueType;
    private String timepoint;
    private String groupId;
    private String targetedSequencing;
    private LocalDate createdDate;
    private BigDecimal volume;
    private BigDecimal concentration;
    private Run run;
    private Boolean qcPassed;
    private String qcReason;
    private String qcUser;
    private LocalDate qcDate;
    private Boolean dataReviewPassed;
    private String dataReviewUser;
    private LocalDate dataReviewDate;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
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

    public Builder groupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public Builder targetedSequencing(String targetedSequencing) {
      this.targetedSequencing = targetedSequencing;
      return this;
    }

    public Builder createdDate(LocalDate createdDate) {
      this.createdDate = createdDate;
      return this;
    }

    public Builder volume(BigDecimal volume) {
      this.volume = volume;
      return this;
    }

    public Builder concentration(BigDecimal concentration) {
      this.concentration = concentration;
      return this;
    }

    public Builder run(Run run) {
      this.run = run;
      return this;
    }

    public Builder qcPassed(Boolean qcPassed) {
      this.qcPassed = qcPassed;
      return this;
    }

    public Builder qcReason(String qcReason) {
      this.qcReason = qcReason;
      return this;
    }

    public Builder qcUser(String qcUser) {
      this.qcUser = qcUser;
      return this;
    }

    public Builder qcDate(LocalDate qcDate) {
      this.qcDate = qcDate;
      return this;
    }

    public Builder dataReviewPassed(Boolean dataReviewPassed) {
      this.dataReviewPassed = dataReviewPassed;
      return this;
    }

    public Builder dataReviewUser(String dataReviewUser) {
      this.dataReviewUser = dataReviewUser;
      return this;
    }

    public Builder dataReviewDate(LocalDate dataReviewDate) {
      this.dataReviewDate = dataReviewDate;
      return this;
    }

    public Sample build() {
      return new Sample(this);
    }

  }
}
