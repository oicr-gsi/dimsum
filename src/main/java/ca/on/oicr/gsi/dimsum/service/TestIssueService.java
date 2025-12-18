package ca.on.oicr.gsi.dimsum.service;

import java.util.ArrayList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import com.atlassian.jira.rest.client.api.domain.Issue;
import ca.on.oicr.gsi.dimsum.data.IssueState;

/**
 * This service is intended for testing only, does not interact with any real service, and does not
 * maintain any issues. Most methods do nothing and return nothing.
 */
@Service
@ConditionalOnExpression("${testissues.enabled}")
public class TestIssueService implements IssueTracker {

  @Override
  public Issue getIssueByKey(String key) {
    return null;
  }

  @Override
  public Issue getIssueBySummary(String summary) {
    return null;
  }

  @Override
  public Iterable<Issue> getOpenIssues(String summary) {
    return new ArrayList<>();
  }

  @Override
  public void postComment(Issue issue, String message) {
    // do nothing
  }

  @Override
  public void closeIssue(Issue issue, String message) {
    // do nothing
  }

  @Override
  public void pauseIssue(Issue issue, String message) {
    // do nothing
  }

  @Override
  public void reopenIssue(Issue issue, String message) {
    // do nothing
  }

  @Override
  public String createIssue(String summary, String description) {
    return "TICKET";
  }

  @Override
  public IssueState getIssueState(Issue issue) {
    return IssueState.OPEN;
  }

}
