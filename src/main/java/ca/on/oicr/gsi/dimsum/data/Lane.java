package ca.on.oicr.gsi.dimsum.data;

import java.math.BigDecimal;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Lane {

  private final Integer laneNumber;
  private final Integer percentOverQ30Read1;
  private final Integer percentOverQ30Read2;
  private final Long clustersPf;
  private final BigDecimal percentPfixRead1;
  private final BigDecimal percentPfixRead2;

  private Lane(Builder builder) {
    this.laneNumber = Objects.requireNonNull(builder.laneNumber);
    this.percentOverQ30Read1 = builder.percentOverQ30Read1;
    this.percentOverQ30Read2 = builder.percentOverQ30Read2;
    this.clustersPf = builder.clustersPf;
    this.percentPfixRead1 = builder.percentPfixRead1;
    this.percentPfixRead2 = builder.percentPfixRead2;
  }

  public Integer getLaneNumber() {
    return laneNumber;
  }

  public Integer getPercentOverQ30Read1() {
    return percentOverQ30Read1;
  }

  public Integer getPercentOverQ30Read2() {
    return percentOverQ30Read2;
  }

  public Long getClustersPf() {
    return clustersPf;
  }

  public BigDecimal getPercentPfixRead1() {
    return percentPfixRead1;
  }

  public BigDecimal getPercentPfixRead2() {
    return percentPfixRead2;
  }

  public static class Builder {

    private Integer laneNumber;
    private Integer percentOverQ30Read1;
    private Integer percentOverQ30Read2;
    private Long clustersPf;
    private BigDecimal percentPfixRead1;
    private BigDecimal percentPfixRead2;

    public Builder laneNumber(Integer laneNumber) {
      this.laneNumber = laneNumber;
      return this;
    }

    public Builder percentOverQ30Read1(Integer percentOverQ30Read1) {
      this.percentOverQ30Read1 = percentOverQ30Read1;
      return this;
    }

    public Builder percentOverQ30Read2(Integer percentOverQ30Read2) {
      this.percentOverQ30Read2 = percentOverQ30Read2;
      return this;
    }

    public Builder clustersPf(Long clustersPf) {
      this.clustersPf = clustersPf;
      return this;
    }

    public Builder percentPfixRead1(BigDecimal percentPfixRead1) {
      this.percentPfixRead1 = percentPfixRead1;
      return this;
    }

    public Builder percentPfixRead2(BigDecimal percentPfixRead2) {
      this.percentPfixRead2 = percentPfixRead2;
      return this;
    }

    public Lane build() {
      return new Lane(this);
    }

  }

}
