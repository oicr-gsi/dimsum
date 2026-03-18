package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.SampleMetric;

public class SampleAndRelated implements Sample {

  private final Sample sample;
  private final Set<RelatedSample> relatedSamples;

  private SampleAndRelated(Builder builder) {
    this.sample = requireNonNull(builder.sample);
    this.relatedSamples =
        Collections.unmodifiableSet(new HashSet<>(builder.relatedSamples.values()));
  }

  @Override
  public Boolean getAnalysisSkipped() {
    return sample.getAnalysisSkipped();
  }

  @Override
  public Set<Long> getAssayIds() {
    return sample.getAssayIds();
  }

  @Override
  public Integer getClustersPerSample() {
    return sample.getClustersPerSample();
  }

  @Override
  public BigDecimal getCollapsedCoverage() {
    return sample.getCollapsedCoverage();
  }

  @Override
  public BigDecimal getConcentration() {
    return sample.getConcentration();
  }

  @Override
  public String getConcentrationUnits() {
    return sample.getConcentrationUnits();
  }

  @Override
  public LocalDate getCreatedDate() {
    return sample.getCreatedDate();
  }

  @Override
  public LocalDate getDataReviewDate() {
    return sample.getDataReviewDate();
  }

  @Override
  public Boolean getDataReviewPassed() {
    return sample.getDataReviewPassed();
  }

  @Override
  public String getDataReviewUser() {
    return sample.getDataReviewUser();
  }

  @Override
  public Donor getDonor() {
    return sample.getDonor();
  }

  @Override
  public BigDecimal getDuplicationRate() {
    return sample.getDuplicationRate();
  }

  @Override
  public BigDecimal getDv200() {
    return sample.getDv200();
  }

  @Override
  public String getGroupId() {
    return sample.getGroupId();
  }

  @Override
  public String getId() {
    return sample.getId();
  }

  @Override
  public Integer getLambdaClusters() {
    return sample.getLambdaClusters();
  }

  @Override
  public BigDecimal getLambdaMethylation() {
    return sample.getLambdaMethylation();
  }

  @Override
  public LocalDate getLatestActivityDate() {
    return sample.getLatestActivityDate();
  }

  @Override
  public String getLibraryDesignCode() {
    return sample.getLibraryDesignCode();
  }

  @Override
  public Integer getLibrarySize() {
    return sample.getLibrarySize();
  }

  @Override
  public BigDecimal getMappedToCoding() {
    return sample.getMappedToCoding();
  }

  @Override
  public BigDecimal getMeanCoverageDeduplicated() {
    return sample.getMeanCoverageDeduplicated();
  }

  @Override
  public BigDecimal getMeanInsertSize() {
    return sample.getMeanInsertSize();
  }

  @Override
  public BigDecimal getMedianInsertSize() {
    return sample.getMedianInsertSize();
  }

  @Override
  public BigDecimal getMethylationBeta() {
    return sample.getMethylationBeta();
  }

  @Override
  public List<SampleMetric> getMetrics() {
    return sample.getMetrics();
  }

  @Override
  public String getName() {
    return sample.getName();
  }

  @Override
  public String getNucleicAcidType() {
    return sample.getNucleicAcidType();
  }

  @Override
  public BigDecimal getOnTargetReads() {
    return sample.getOnTargetReads();
  }

  @Override
  public Integer getPeReads() {
    return sample.getPeReads();
  }

  @Override
  public Integer getPreliminaryClustersPerSample() {
    return sample.getPreliminaryClustersPerSample();
  }

  @Override
  public BigDecimal getPreliminaryMeanCoverageDeduplicated() {
    return sample.getPreliminaryMeanCoverageDeduplicated();
  }

  @Override
  public String getProject() {
    return sample.getProject();
  }

  @Override
  public Integer getPuc19Clusters() {
    return sample.getPuc19Clusters();
  }

  @Override
  public BigDecimal getPuc19Methylation() {
    return sample.getPuc19Methylation();
  }

  @Override
  public LocalDate getQcDate() {
    return sample.getQcDate();
  }

  @Override
  public String getQcNote() {
    return sample.getQcNote();
  }

  @Override
  public Boolean getQcPassed() {
    return sample.getQcPassed();
  }

  @Override
  public String getQcReason() {
    return sample.getQcReason();
  }

  @Override
  public String getQcUser() {
    return sample.getQcUser();
  }

  @Override
  public BigDecimal getRawCoverage() {
    return sample.getRawCoverage();
  }

  @Override
  public BigDecimal getRelativeCpgInRegions() {
    return sample.getRelativeCpgInRegions();
  }

  @Override
  public Long getRequisitionId() {
    return sample.getRequisitionId();
  }

  @Override
  public String getRequisitionName() {
    return sample.getRequisitionName();
  }

  @Override
  public Run getRun() {
    return sample.getRun();
  }

  @Override
  public String getSecondaryId() {
    return sample.getSecondaryId();
  }

  @Override
  public String getSequencingLane() {
    return sample.getSequencingLane();
  }

  @Override
  public String getTargetedSequencing() {
    return sample.getTargetedSequencing();
  }

  @Override
  public String getTimepoint() {
    return sample.getTimepoint();
  }

  @Override
  public String getTissueMaterial() {
    return sample.getTissueMaterial();
  }

  @Override
  public String getTissueOrigin() {
    return sample.getTissueOrigin();
  }

  @Override
  public String getTissueType() {
    return sample.getTissueType();
  }

  @Override
  public LocalDate getTransferDate() {
    return sample.getTransferDate();
  }

  @Override
  public BigDecimal getVolume() {
    return sample.getVolume();
  }

  @Override
  public BigDecimal getrRnaContamination() {
    return sample.getrRnaContamination();
  }

  public Set<RelatedSample> getRelatedSamples() {
    return relatedSamples;
  }

  public static final class Builder {

    private final Sample sample;
    private final Map<String, RelatedSample> relatedSamples;

    public Builder(Sample sample, Collection<Sample> relatedSamples) {
      this.sample = sample;
      this.relatedSamples = relatedSamples.stream()
          .filter(related -> !Objects.equals(related.getId(), sample.getId()))
          .map(RelatedSample::new)
          .collect(Collectors.toMap(RelatedSample::getId, Function.identity()));
    }

    public Builder addRelatedSamples(Collection<Sample> relatedSamples) {
      for (Sample relatedSample : relatedSamples) {
        if (Objects.equals(relatedSample.getId(), sample.getId())) {
          continue;
        }
        this.relatedSamples.putIfAbsent(relatedSample.getId(),
            new RelatedSample(relatedSample));
      }
      return this;
    }

    public SampleAndRelated build() {
      return new SampleAndRelated(this);
    }

  }

}
