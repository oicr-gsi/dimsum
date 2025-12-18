package ca.on.oicr.gsi.dimsum.service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import ca.on.oicr.gsi.dimsum.data.IssueState;

public interface IssueTracker {

  Issue getIssueByKey(String key);

  Issue getIssueBySummary(String summary);

  Iterable<Issue> getOpenIssues(String summary);

  void postComment(Issue issue, String message);

  void closeIssue(Issue issue, String message);

  void pauseIssue(Issue issue, String message);

  void reopenIssue(Issue issue, String message);

  String createIssue(String summary, String description);

  IssueState getIssueState(Issue issue);

}
