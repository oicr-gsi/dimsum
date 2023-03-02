package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.util.Collections;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
public class ProjectSummary {
  private final String name;
  private final Map<String, Integer> counts;

  public ProjectSummary(Builder builder) {
    this.name = requireNonNull(builder.name);
    this.counts = Collections.unmodifiableMap(builder.counts);
  }

  public String getName() {
    return name;
  }

  public Map<String, Integer> getCounts() {
    return counts;
  }

  static public Map<String, Integer> initCounts() {
    Map<String, Integer> counts = Collections.emptyMap();
    counts.put("totalTests", 0);

    counts.put("receiptPendingQC", 0);
    counts.put("receiptCompleted", 0);

    counts.put("extractionPendingWork", 0);
    counts.put("extractionPendingQC", 0);
    counts.put("extractionCompleted", 0);

    counts.put("LibraryPrepPendingWork", 0);
    counts.put("LibraryPrepPendingQC", 0);
    counts.put("LibraryPrepCompleted", 0);

    counts.put("LibraryQualPendingWork", 0);
    counts.put("LibraryQualPendingQC", 0);
    counts.put("LibraryQualCompleted", 0);

    counts.put("fullDepthPendingWork", 0);
    counts.put("fullDepthPendingQC", 0);
    counts.put("fullDepthCompleted", 0);

    counts.put("informaticsPendingWork", 0);
    counts.put("informaticsCompleted", 0);

    counts.put("draftReportPendingWork", 0);
    counts.put("draftReportCompleted", 0);

    counts.put("finalReportPendingWork", 0);
    counts.put("finalReportCompleted", 0);

    return counts;

  }

  public void addCounts(Map<String, Integer> toAdd) {
    for (Map.Entry<String, Integer> entry : toAdd.entrySet()) {
      String statusDescription = entry.getKey();
      int count = entry.getValue();
      if (this.counts.containsKey(statusDescription)) {
        this.counts.put(statusDescription, this.counts.get(statusDescription) + count);
      }
    }
  }

  public static class Builder {

    private String name;
    private Map<String, Integer> counts;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder counts(Map<String, Integer> counts) {
      this.counts = counts;
      return this;
    }

    public ProjectSummary build() {
      return new ProjectSummary(this);
    }
  }
}
