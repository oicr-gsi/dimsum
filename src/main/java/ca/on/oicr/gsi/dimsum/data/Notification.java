package ca.on.oicr.gsi.dimsum.data;

import java.util.Collections;
import java.util.Set;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
public class Notification {

  private final Run run;
  private final MetricCategory metricCategory;
  private final Set<Sample> pendingAnalysisSamples;
  private final Set<Sample> pendingQcSamples;
  private final Set<Sample> pendingDataReviewSamples;

  public Notification(Run run, MetricCategory metricCategory, Set<Sample> pendingAnalysisSamples,
      Set<Sample> pendingQcSamples, Set<Sample> pendingDataReviewSamples) {
    this.run = run;
    this.metricCategory = metricCategory;
    this.pendingAnalysisSamples = Collections.unmodifiableSet(pendingAnalysisSamples);
    this.pendingQcSamples = Collections.unmodifiableSet(pendingQcSamples);
    this.pendingDataReviewSamples = Collections.unmodifiableSet(pendingDataReviewSamples);
  }

  public Run getRun() {
    return run;
  }

  public MetricCategory getMetricCategory() {
    return metricCategory;
  }

  @JsonIgnore
  public Set<Sample> getPendingAnalysisSamples() {
    return pendingAnalysisSamples;
  }

  @JsonIgnore
  public Set<Sample> getPendingQcSamples() {
    return pendingQcSamples;
  }

  @JsonIgnore
  public Set<Sample> getPendingDataReviewSamples() {
    return pendingDataReviewSamples;
  }

  @JsonProperty("pendingAnalysisCount")
  public int getPendingAnalysisCount() {
    return count(pendingAnalysisSamples);
  }

  @JsonProperty("pendingQcCount")
  public int getPendingQcCount() {
    return count(pendingQcSamples);
  }

  @JsonProperty("pendingDataReviewCount")
  public int getPendingDataReviewCount() {
    return count(pendingDataReviewSamples);
  }

  private static int count(Set<Sample> samples) {
    return samples == null ? 0 : samples.size();
  }

}
