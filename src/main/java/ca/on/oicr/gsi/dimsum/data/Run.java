package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Run {

  private final long id;
  private final String name;
  private final String containerModel;
  private final boolean joinedLanes;
  private final String sequencingParameters;
  private final Integer readLength;
  private final Integer readLength2;
  private final LocalDate completionDate;
  private final BigDecimal percentOverQ30;
  private final Long clustersPf;
  private final List<Lane> lanes;
  private final Boolean qcPassed;
  private final String qcUser;
  private final LocalDate qcDate;
  private final Boolean dataReviewPassed;
  private final String dataReviewUser;
  private final LocalDate dataReviewDate;

  private Run(Builder builder) {
    if (builder.id < 0) {
      throw new IllegalArgumentException(String.format("Invalid run ID: %d", builder.id));
    }
    this.id = builder.id;
    this.name = requireNonNull(builder.name);
    this.containerModel = builder.containerModel;
    this.joinedLanes = builder.joinedLanes;
    this.sequencingParameters = builder.sequencingParameters;
    this.readLength = builder.readLength;
    this.readLength2 = builder.readLength2;
    this.completionDate = builder.completionDate;
    this.percentOverQ30 = builder.percentOverQ30;
    this.clustersPf = builder.clustersPf;
    this.lanes = Collections.unmodifiableList(requireNonNull(builder.lanes));
    this.qcPassed = builder.qcPassed;
    this.qcUser = builder.qcUser;
    this.qcDate = builder.qcDate;
    this.dataReviewPassed = builder.dataReviewPassed;
    this.dataReviewUser = builder.dataReviewUser;
    this.dataReviewDate = builder.dataReviewDate;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getContainerModel() {
    return containerModel;
  }

  public boolean hasJoinedLanes() {
    return joinedLanes;
  }

  public String getSequencingParameters() {
    return sequencingParameters;
  }

  public Integer getReadLength() {
    return readLength;
  }

  public Integer getReadLength2() {
    return readLength2;
  }

  public LocalDate getCompletionDate() {
    return completionDate;
  }

  public BigDecimal getPercentOverQ30() {
    return percentOverQ30;
  }

  public Long getClustersPf() {
    return clustersPf;
  }

  public List<Lane> getLanes() {
    return lanes;
  }

  public Boolean getQcPassed() {
    return qcPassed;
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
    Run other = (Run) obj;
    return Objects.equals(id, other.id);
  }

  public static class Builder {

    private long id;
    private String name;
    private String containerModel;
    private boolean joinedLanes;
    private String sequencingParameters;
    private Integer readLength;
    private Integer readLength2;
    private LocalDate completionDate;
    private BigDecimal percentOverQ30;
    private Long clustersPf;
    private List<Lane> lanes;
    private Boolean qcPassed;
    private String qcUser;
    private LocalDate qcDate;
    private Boolean dataReviewPassed;
    private String dataReviewUser;
    private LocalDate dataReviewDate;

    public Builder id(long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder containerModel(String containerModel) {
      this.containerModel = containerModel;
      return this;
    }

    public Builder joinedLanes(boolean joinedLanes) {
      this.joinedLanes = joinedLanes;
      return this;
    }

    public Builder sequencingParameters(String sequencingParameters) {
      this.sequencingParameters = sequencingParameters;
      return this;
    }

    public Builder readLength(Integer readLength) {
      this.readLength = readLength;
      return this;
    }

    public Builder readLength2(Integer readLength2) {
      this.readLength2 = readLength2;
      return this;
    }

    public Builder completionDate(LocalDate completionDate) {
      this.completionDate = completionDate;
      return this;
    }

    public Builder percentOverQ30(BigDecimal percentOverQ30) {
      this.percentOverQ30 = percentOverQ30;
      return this;
    }

    public Builder clustersPf(Long clustersPf) {
      this.clustersPf = clustersPf;
      return this;
    }

    public Builder lanes(List<Lane> lanes) {
      this.lanes = lanes;
      return this;
    }

    public Builder qcPassed(Boolean qcPassed) {
      this.qcPassed = qcPassed;
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

    public Run build() {
      return new Run(this);
    }

  }

}
