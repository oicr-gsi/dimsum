package ca.on.oicr.gsi.dimsum.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import ca.on.oicr.gsi.dimsum.data.IssueState;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
@ConditionalOnProperty(name = "jira.baseurl")
public class JiraService {

  @Value("${jira.transitions.close}")
  private String transitionClose;
  @Value("${jira.transitions.reopen}")
  private String transitionReopen;
  // Note: issue resolution is null when unresolved
  @Value("${jira.resolutions.paused}")
  private String resolutionPaused;
  @Value("${jira.resolutions.done}")
  private String resolutionDone;
  @Value("${jira.resolutions.override}")
  private String resolutionOverride;
  @Value("${jira.issuetypes.task}")
  private String issueTypeTask;
  @Value("${jira.labels.notification}")
  private String labelNotification;
  @Value("${jira.projects.lab}")
  private String projectNotification;

  private Counter requestCounter;

  private JiraRestClient rest;

  public JiraService(@Value("${jira.baseurl}") String baseUrl,
      @Value("${jira.username}") String username,
      @Value("${jira.password}") String password,
      @Autowired MeterRegistry meterRegistry) {
    if (meterRegistry != null) {
      requestCounter = Counter.builder("jira_requests")
          .description("Number of JIRA requests since application startup")
          .register(meterRegistry);
    }
    rest = new AsynchronousJiraRestClientFactory()
        .createWithBasicHttpAuthentication(URI.create(baseUrl), username, password);
  }

  private void countRequest() {
    if (requestCounter != null) {
      requestCounter.increment();
    }
  }

  public Issue getIssueByKey(String key) {
    return rest.getIssueClient().getIssue(key).claim();
  }

  public Issue getIssueBySummary(String summary) {
    countRequest();
    Iterable<Issue> issues = rest.getSearchClient().searchJql(
        String.format("project = %s AND labels = %s AND summary ~ \"%s\"", projectNotification,
            labelNotification, summary))
        .claim().getIssues();
    for (Issue issue : issues) {
      if (issue.getSummary().equals(summary)) {
        return issue;
      }
    }
    return null;
  }

  public Iterable<Issue> getOpenIssues(String summary) {
    final int pageSize = 50;
    List<Issue> issues = new ArrayList<>();
    String jql = "project = %s AND labels = %s AND summary ~ \"%s\" AND resolution = Unresolved"
        .formatted(projectNotification, labelNotification, summary);
    int startAt = 0;
    while (true) {
      int previousLength = issues.size();
      countRequest();
      Iterable<Issue> newIssues = rest.getSearchClient()
          .searchJql(jql, pageSize, startAt, null)
          .claim().getIssues();
      for (Issue newIssue : newIssues) {
        issues.add(newIssue);
      }
      if (issues.size() < previousLength + pageSize) {
        break;
      }
      startAt += pageSize;
    }
    return issues;
  }

  public void postComment(Issue issue, String message) {
    countRequest();
    rest.getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(message)).claim();
  }

  public void closeIssue(Issue issue, String message) {
    closeIssue(issue, message, resolutionDone);
  }

  public void pauseIssue(Issue issue, String message) {
    closeIssue(issue, message, resolutionPaused);
  }

  private void closeIssue(Issue issue, String message, String resolution) {
    Resolution currentResolution = issue.getResolution();
    if (currentResolution != null) {
      // Issue already closed
      if (Objects.equals(currentResolution.getName(), resolution)) {
        // Desired resolution already set
        postComment(issue, message);
      } else {
        // Wrong resolution set - reopen and close with correct resolution
        Integer transitionId = getTransitionId(issue, transitionReopen);
        TransitionInput input = new TransitionInput(transitionId);
        countRequest();
        rest.getIssueClient().transition(issue, input).claim();
        doClose(issue, message, resolution);
      }
    } else {
      doClose(issue, message, resolution);
    }
  }

  private void doClose(Issue issue, String message, String resolution) {
    Integer transitionId = getTransitionId(issue, transitionClose);
    Collection<FieldInput> fields = Arrays.asList(
        new FieldInput("resolution", ComplexIssueInputFieldValue.with("name", resolution)));
    TransitionInput input = new TransitionInput(transitionId, fields, Comment.valueOf(message));
    countRequest();
    rest.getIssueClient().transition(issue.getTransitionsUri(), input).claim();
  }

  public void reopenIssue(Issue issue, String message) {
    Resolution currentResolution = issue.getResolution();
    if (currentResolution == null) {
      // Already open
      postComment(issue, message);
    } else {
      Integer transitionId = getTransitionId(issue, transitionReopen);
      TransitionInput input = new TransitionInput(transitionId, Comment.valueOf(message));
      countRequest();
      rest.getIssueClient().transition(issue, input).claim();
    }
  }

  private Integer getTransitionId(Issue issue, String transitionName) {
    countRequest();
    Iterable<Transition> transitions = rest.getIssueClient().getTransitions(issue).claim();
    for (Transition transition : transitions) {
      if (Objects.equals(transitionName, transition.getName())) {
        return transition.getId();
      }
    }
    throw new IllegalStateException(
        "Transition '%s' not found for issue %s".formatted(transitionName, issue.getKey()));
  }

  public String createIssue(String summary, String description) {
    countRequest();
    Project project = rest.getProjectClient().getProject(projectNotification).claim();
    IssueInput input = new IssueInputBuilder()
        .setProject(project)
        .setIssueType(getIssueType(project, issueTypeTask))
        .setSummary(summary)
        .setDescription(description)
        .setFieldInput(
            new FieldInput(IssueFieldId.LABELS_FIELD, Collections.singleton(labelNotification)))
        .build();
    countRequest();
    BasicIssue issue = rest.getIssueClient().createIssue(input).claim();
    return issue.getKey();
  }

  private IssueType getIssueType(Project project, String issueTypeName) {
    for (IssueType issueType : project.getIssueTypes()) {
      if (Objects.equals(issueType.getName(), issueTypeName)) {
        return issueType;
      }
    }
    throw new IllegalArgumentException(String.format("Issue type '%s' not found in project '%s'",
        issueTypeName, project.getKey()));
  }

  public IssueState getIssueState(Issue issue) {
    Resolution resolution = issue.getResolution();
    if (resolution == null) {
      return IssueState.OPEN;
    } else if (Objects.equals(resolution.getName(), resolutionPaused)) {
      return IssueState.PAUSED;
    } else if (Objects.equals(resolution.getName(), resolutionOverride)) {
      return IssueState.OVERRIDDEN;
    } else {
      return IssueState.CLOSED;
    }
  }

}
