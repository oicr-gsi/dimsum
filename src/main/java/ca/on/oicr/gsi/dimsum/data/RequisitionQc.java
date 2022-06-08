package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import javax.annotation.concurrent.Immutable;

@Immutable
public class RequisitionQc {

  private final boolean qcPassed;
  private final String qcUser;
  private final LocalDate qcDate;

  private RequisitionQc(Builder builder) {
    this.qcPassed = requireNonNull(builder.qcPassed);
    this.qcUser = requireNonNull(builder.qcUser);
    this.qcDate = requireNonNull(builder.qcDate);
  }

  public boolean isQcPassed() {
    return qcPassed;
  }

  public String getQcUser() {
    return qcUser;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

  public static class Builder {
    
    private boolean qcPassed;
    private String qcUser;
    private LocalDate qcDate;

    public Builder qcPassed(boolean qcPassed) {
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

    public RequisitionQc build() {
      return new RequisitionQc(this);
    }
  }
}
