package ca.on.oicr.gsi.dimsum.data;

import java.util.Collections;
import java.util.Set;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Immutable
public class Notification {

  private static final String PERIOD = ".";
  private static final String COLON_AND_LINE_BREAK = ":\n";

  private final Run run;
  private final Set<Sample> pendingAnalysisSamples;
  private final Set<Sample> pendingQcSamples;
  private final Set<Sample> pendingDataReviewSamples;
  private final String issueKey;

  public Notification(Run run, Set<Sample> pendingAnalysisSamples, Set<Sample> pendingQcSamples,
      Set<Sample> pendingDataReviewSamples, String issueKey) {
    this.run = run;
    this.pendingAnalysisSamples = Collections.unmodifiableSet(pendingAnalysisSamples);
    this.pendingQcSamples = Collections.unmodifiableSet(pendingQcSamples);
    this.pendingDataReviewSamples = Collections.unmodifiableSet(pendingDataReviewSamples);
    this.issueKey = issueKey;
  }

  public Notification withIssueKey(String newIssueKey) {
    return new Notification(run, pendingAnalysisSamples, pendingQcSamples, pendingDataReviewSamples,
        newIssueKey);
  }

  public Run getRun() {
    return run;
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
    return pendingAnalysisSamples.size();
  }

  @JsonProperty("pendingQcCount")
  public int getPendingQcCount() {
    return pendingQcSamples.size();
  }

  @JsonProperty("pendingDataReviewCount")
  public int getPendingDataReviewCount() {
    return pendingDataReviewSamples.size();
  }

  public String getIssueKey() {
    return issueKey;
  }

  public boolean requiresAction() {
    return !pendingQcSamples.isEmpty() || !pendingDataReviewSamples.isEmpty()
        || run.getDataReviewDate() == null;
  }

  public String makeComment(String baseUrl) {
    return """
        %s

        %d libraries are pending QC%s

        %d libraries are pending data review%s

        %d other libraries are still pending analysis.

        [See metrics in Dimsum|%s/runs/%s]

        Internal use: <%s>""".formatted(makeRunMessage(),
        getPendingQcCount(), makePunctuationAndList(pendingQcSamples),
        getPendingDataReviewCount(), makePunctuationAndList(pendingDataReviewSamples),
        getPendingAnalysisCount(),
        baseUrl, run.getName(),
        makeCommentCode());
  }

  private String makeRunMessage() {
    if (run.getQcDate() == null) {
      return "Run is pending QC.";
    } else if (run.getDataReviewDate() == null) {
      return "Run is pending data review.";
    } else {
      return "Run-level QC completed.";
    }
  }

  private String makePunctuationAndList(Set<Sample> samples) {
    if (samples.isEmpty()) {
      return PERIOD;
    } else {
      StringBuilder sb = new StringBuilder(COLON_AND_LINE_BREAK);
      for (Sample sample : samples) {
        sb.append("\n* %s (L%s)".formatted(sample.getName(), sample.getSequencingLane()));
      }
      return sb.toString();
    }
  }

  private String makeCommentCode() {
    return "R%dA%dQ%dD%d".formatted(getRunState(), getPendingAnalysisCount(), getPendingQcCount(),
        getPendingDataReviewCount());
  }

  private int getRunState() {
    if (run.getQcDate() == null) {
      return 1;
    } else if (run.getDataReviewDate() == null) {
      return 2;
    } else {
      return 3;
    }
  }

  public IssueState getIssueState() {
    if (!pendingQcSamples.isEmpty()
        || !pendingDataReviewSamples.isEmpty()) {
      return IssueState.OPEN;
    } else if (!pendingAnalysisSamples.isEmpty()) {
      return IssueState.PAUSED;
    } else if (run.getDataReviewDate() == null) {
      return IssueState.OPEN;
    } else {
      return IssueState.CLOSED;
    }
  }

}
