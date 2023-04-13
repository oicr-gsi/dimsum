package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import javax.annotation.concurrent.Immutable;

@Immutable
public class RequisitionQcGroup {

  private final Donor donor;
  private final String tissueOrigin;
  private final String tissueType;
  private final String libraryDesignCode;
  private final String groupId;
  private final BigDecimal purity;
  private final BigDecimal collapsedCoverage;
  private final BigDecimal callability;

  private RequisitionQcGroup(Builder builder) {
    this.donor = requireNonNull(builder.donor);
    this.tissueOrigin = requireNonNull(builder.tissueOrigin);
    this.tissueType = requireNonNull(builder.tissueType);
    this.libraryDesignCode = requireNonNull(builder.libraryDesignCode);
    this.groupId = builder.groupId;
    this.purity = builder.purity;
    this.collapsedCoverage = builder.collapsedCoverage;
    this.callability = builder.callability;
  }

  public Donor getDonor() {
    return donor;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public String getTissueType() {
    return tissueType;
  }

  public String getLibraryDesignCode() {
    return libraryDesignCode;
  }

  public String getGroupId() {
    return groupId;
  }

  public BigDecimal getPurity() {
    return purity;
  }

  public BigDecimal getCollapsedCoverage() {
    return collapsedCoverage;
  }

  public BigDecimal getCallability() {
    return callability;
  }

  public static class Builder {

    private Donor donor;
    private String tissueOrigin;
    private String tissueType;
    private String libraryDesignCode;
    private String groupId;
    private BigDecimal purity;
    private BigDecimal collapsedCoverage;
    private BigDecimal callability;

    public Builder donor(Donor donor) {
      this.donor = donor;
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

    public Builder libraryDesignCode(String libraryDesignCode) {
      this.libraryDesignCode = libraryDesignCode;
      return this;
    }

    public Builder groupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public Builder purity(BigDecimal purity) {
      this.purity = purity;
      return this;
    }

    public Builder collapsedCoverage(BigDecimal collapsedCoverage) {
      this.collapsedCoverage = collapsedCoverage;
      return this;
    }

    public Builder callability(BigDecimal callability) {
      this.callability = callability;
      return this;
    }

    public RequisitionQcGroup build() {
      return new RequisitionQcGroup(this);
    }

  }

}
