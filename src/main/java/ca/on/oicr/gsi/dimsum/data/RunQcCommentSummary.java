package ca.on.oicr.gsi.dimsum.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;
import org.joda.time.DateTime;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;

import ca.on.oicr.gsi.cardea.data.Run;

@Immutable
public class RunQcCommentSummary {

  private static final Pattern codePattern =
      Pattern.compile(".*<R([1-3])A(\\d+)Q(\\d+)D(\\d+)>$", Pattern.DOTALL);

  // Run states: 1=pending QC, 2=pending data review, 3=sign-offs complete
  private final int runState;
  private final int pendingAnalysisCount;
  private final int pendingQcCount;
  private final int pendingDataReviewCount;

  public static RunQcCommentSummary findLatest(Issue issue) {
    // look for latest comment with state code
    DateTime timestamp = null;
    RunQcCommentSummary result = null;
    if (issue.getComments() != null) {
      for (Comment comment : issue.getComments()) {
        if (timestamp == null || comment.getCreationDate().isAfter(timestamp)) {
          RunQcCommentSummary commentResult = parseCode(comment.getBody());
          if (commentResult != null) {
            result = commentResult;
            timestamp = comment.getCreationDate();
          }
        }
      }
    }
    if (result != null) {
      return result;
    }
    // return values from description if state code is present; otherwise null
    return parseCode(issue.getDescription());
  }

  private static RunQcCommentSummary parseCode(String text) {
    Matcher m = codePattern.matcher(text);
    if (!m.matches()) {
      return null;
    }
    return new RunQcCommentSummary(parseInt(m, 1), parseInt(m, 2), parseInt(m, 3), parseInt(m, 4));
  }

  private static final int parseInt(Matcher m, int group) {
    return Integer.parseInt(m.group(group));
  }

  protected RunQcCommentSummary(int runState, int pendingAnalysisCount, int pendingQcCount,
      int pendingDataReviewCount) {
    this.runState = runState;
    this.pendingAnalysisCount = pendingAnalysisCount;
    this.pendingQcCount = pendingQcCount;
    this.pendingDataReviewCount = pendingDataReviewCount;
  }

  public boolean isPendingRunQc() {
    return runState == 1;
  }

  public boolean isPendingDataReview() {
    return runState == 2;
  }

  public int getPendingAnalysisCount() {
    return pendingAnalysisCount;
  }

  public int getPendingQcCount() {
    return pendingQcCount;
  }

  public int getPendingDataReviewCount() {
    return pendingDataReviewCount;
  }

  public boolean needsUpdate(Notification newState) {
    return newState.getPendingAnalysisCount() != pendingAnalysisCount
        || newState.getPendingQcCount() != pendingQcCount
        || newState.getPendingDataReviewCount() != pendingDataReviewCount
        || getRunState(newState.getRun()) != runState;
  }

  public String getCode() {
    return String.format("R%dA%dQ%dD%d", runState, pendingAnalysisCount, pendingQcCount,
        pendingDataReviewCount);
  }

  private static int getRunState(Run run) {
    if (run.getQcDate() == null) {
      return 1;
    } else if (run.getDataReviewDate() == null) {
      return 2;
    } else {
      return 3;
    }
  }

}
