package ca.on.oicr.gsi.dimsum.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.atlassian.jira.rest.client.api.domain.Issue;
import ca.on.oicr.gsi.dimsum.data.Assay;
import ca.on.oicr.gsi.dimsum.data.IssueState;
import ca.on.oicr.gsi.dimsum.data.Metric;
import ca.on.oicr.gsi.dimsum.data.MetricCategory;
import ca.on.oicr.gsi.dimsum.data.MetricSubcategory;
import ca.on.oicr.gsi.dimsum.data.Notification;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.RunQcCommentSummary;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.service.filtering.NotificationSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;
import ca.on.oicr.gsi.dimsum.util.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class NotificationManager {

  private static final Logger log = LoggerFactory.getLogger(NotificationManager.class);

  private static final String SUMMARY_SUFFIX_RUN_QC = " Run QC";
  private static final Pattern SUMMARY_PATTERN_RUN_QC =
      Pattern.compile("^(.+)" + SUMMARY_SUFFIX_RUN_QC + "$");

  @Autowired(required = false)
  private JiraService jiraService;

  @Value("${baseurl}")
  private String baseUrl;

  private List<Notification> notifications = new ArrayList<>();
  private int jiraErrors = 0;

  public NotificationManager(@Autowired MeterRegistry meterRegistry) {
    if (meterRegistry != null) {
      Gauge.builder("jira_errors", this::getJiraErrors)
          .description("Number of JIRA errors during last data refresh and notification sync")
          .register(meterRegistry);
    }
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setJiraService(JiraService jiraService) {
    this.jiraService = jiraService;
  }

  private int getJiraErrors() {
    return jiraErrors;
  }

  public void update(Map<String, RunAndLibraries> data, Map<Long, Assay> assaysById) {
    Counter jiraErrorCounter = new Counter();
    Set<String> handledRunNames = new HashSet<>();
    List<Notification> newNotifications =
        updateOpenIssues(data, assaysById, handledRunNames, jiraErrorCounter);
    newNotifications
        .addAll(createOrReopenIssues(data, assaysById, handledRunNames, jiraErrorCounter));
    notifications = newNotifications;
    jiraErrors = jiraErrorCounter.getCount();
  }

  private List<Notification> updateOpenIssues(Map<String, RunAndLibraries> data,
      Map<Long, Assay> assaysById, Set<String> handledRunNames, Counter jiraErrorCounter) {
    List<Notification> newNotifications = new ArrayList<>();
    if (jiraService == null) {
      return newNotifications;
    }
    Iterable<Issue> issues = null;
    try {
      issues = jiraService.getOpenIssues(SUMMARY_SUFFIX_RUN_QC);
    } catch (Exception e) {
      jiraErrorCounter.increment();
      log.error("Error fetching JIRA issues", e);
      return newNotifications;
    }
    for (Issue issue : issues) {
      String runName = parseRunNameFromSummary(issue);
      if (runName == null) {
        // Issue doesn't seem to match expected pattern; ignore
        continue;
      }
      RunAndLibraries runAndLibraries = data.get(runName);
      if (runAndLibraries == null) {
        log.debug("Orphaned notification - run not found: {}", runName);
        continue;
      }
      // track to avoid reprocessing later
      handledRunNames.add(runName);
      Notification notification =
          makeNotification(runAndLibraries, assaysById, true, issue.getKey());
      updateIssue(issue, notification, jiraErrorCounter);
      if (notification != null && notification.requiresAction()) {
        newNotifications.add(notification);
      }
    }
    return newNotifications;
  }

  private static String parseRunNameFromSummary(Issue issue) {
    Matcher m = SUMMARY_PATTERN_RUN_QC.matcher(issue.getSummary());
    if (m.matches()) {
      return m.group(1);
    } else {
      return null;
    }
  }

  private void updateIssue(Issue issue, Notification notification, Counter jiraErrorCounter) {
    try {
      if (notification == null) {
        jiraService.closeIssue(issue, "All sign-offs have been completed.");
        return;
      }
      IssueState issueState = jiraService.getIssueState(issue);
      IssueState notificationState = notification.getIssueState();
      if (issueState != notificationState) {
        switch (notificationState) {
          case OPEN:
            jiraService.reopenIssue(issue, notification.makeComment(baseUrl));
            return;
          case PAUSED:
            jiraService.pauseIssue(issue, notification.makeComment(baseUrl));
            return;
          case CLOSED:
            jiraService.closeIssue(issue, notification.makeComment(baseUrl));
            return;
          default:
            throw new IllegalStateException(
                String.format("Unexpected notification state: %s", notificationState));
        }
      }
      RunQcCommentSummary issueSummary = RunQcCommentSummary.findLatest(issue);
      if (issueSummary == null || issueSummary.needsUpdate(notification)) {
        jiraService.postComment(issue, notification.makeComment(baseUrl));
      }
    } catch (Exception e) {
      jiraErrorCounter.increment();
      log.error("Error updating JIRA issue %s".formatted(issue.getKey()), e);
    }
  }

  private List<Notification> createOrReopenIssues(Map<String, RunAndLibraries> data,
      Map<Long, Assay> assaysById, Set<String> handledRunNames, Counter jiraErrorCounter) {
    List<Notification> newNotifications = new ArrayList<>();
    data.values().stream()
        .filter(x -> !handledRunNames.contains(x.getRun().getName())
            && (readyForLibraryQualificationQc(x, assaysById)
                || readyForFullDepthQc(x, assaysById)))
        .map(x -> makeNotification(x, assaysById, false, null))
        .filter(Objects::nonNull)
        .forEach(x -> {
          if (jiraService == null) {
            newNotifications.add(x);
            return;
          }
          String issueSummary = x.getRun().getName() + SUMMARY_SUFFIX_RUN_QC;
          Issue issue = null;
          try {
            issue = jiraService.getIssue(issueSummary);
          } catch (Exception e) {
            jiraErrorCounter.increment();
            log.error("Error searching for JIRA issue", e);
            newNotifications.add(x);
            return;
          }
          if (issue == null) {
            try {
              String newIssueKey = jiraService.createIssue(issueSummary, x.makeComment(baseUrl));
              newNotifications.add(x.withIssueKey(newIssueKey));
            } catch (Exception e) {
              jiraErrorCounter.increment();
              log.error("Error creating issue", e);
              newNotifications.add(x);
            }
          } else {
            IssueState issueState = jiraService.getIssueState(issue);
            if (issueState != IssueState.OVERRIDDEN) {
              updateIssue(issue, x, jiraErrorCounter);
              newNotifications.add(x.withIssueKey(issue.getKey()));
            }
          }
        });
    return newNotifications;
  }

  protected boolean readyForLibraryQualificationQc(RunAndLibraries runAndLibraries,
      Map<Long, Assay> assaysById) {
    Set<Sample> libraries = runAndLibraries.getLibraryQualifications();
    if (libraries.isEmpty()) {
      return false;
    }
    Run run = runAndLibraries.getRun();
    // "ready" if metrics for ALL libraries are available and ANY library or run sign-off is needed
    return libraries.stream().allMatch(x -> metricsAvailable(x,
        run, assaysById, MetricCategory.LIBRARY_QUALIFICATION))
        && (libraries.stream().anyMatch(x -> x.getDataReviewDate() == null)
            || run.getDataReviewDate() == null);
  }

  protected boolean readyForFullDepthQc(RunAndLibraries runAndLibraries,
      Map<Long, Assay> assaysById) {
    Set<Sample> libraries = runAndLibraries.getFullDepthSequencings();
    if (libraries.isEmpty()) {
      return false;
    }
    Run run = runAndLibraries.getRun();
    final MetricCategory category = MetricCategory.FULL_DEPTH_SEQUENCING;
    // "ready" if ANY library has metrics available AND ANY such library or the run needs sign-off
    return libraries.stream().anyMatch(x -> metricsAvailable(x, run, assaysById, category)
        && (x.getDataReviewDate() == null || run.getDataReviewDate() == null));
  }

  private Notification makeNotification(RunAndLibraries runAndLibraries,
      Map<Long, Assay> assaysById, boolean returnIfPendingAnalysisOnly, String issueKey) {
    Run run = runAndLibraries.getRun();
    Set<Sample> pendingAnalysis = new HashSet<>();
    Set<Sample> pendingQc = new HashSet<>();
    Set<Sample> pendingDataReview = new HashSet<>();
    sortSamples(runAndLibraries.getLibraryQualifications(), run, assaysById,
        MetricCategory.LIBRARY_QUALIFICATION, pendingAnalysis, pendingQc, pendingDataReview);
    sortSamples(runAndLibraries.getFullDepthSequencings(), run, assaysById,
        MetricCategory.FULL_DEPTH_SEQUENCING, pendingAnalysis, pendingQc, pendingDataReview);

    if (pendingQc.isEmpty() && pendingDataReview.isEmpty()
        && run.getDataReviewDate() != null) {
      if (returnIfPendingAnalysisOnly) {
        if (pendingAnalysis.isEmpty()) {
          return null;
        }
      } else {
        return null;
      }
    }
    return new Notification(run, pendingAnalysis, pendingQc, pendingDataReview, issueKey);
  }

  private void sortSamples(Set<Sample> samples, Run run, Map<Long, Assay> assaysById,
      MetricCategory category, Set<Sample> pendingAnalysis, Set<Sample> pendingQc,
      Set<Sample> pendingDataReview) {
    for (Sample sample : samples) {
      if (sample.getDataReviewDate() != null) {
        continue;
      } else if (!metricsAvailable(sample, run, assaysById, category)) {
        pendingAnalysis.add(sample);
      } else if (sample.getQcDate() == null) {
        pendingQc.add(sample);
      } else {
        pendingDataReview.add(sample);
      }
    }
  }

  public TableData<Notification> getNotifications(int pageSize, int pageNumber,
      NotificationSort sort,
      boolean descending) {
    List<Notification> currentNotifications = notifications;
    List<Notification> includedNotifications = currentNotifications.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1))
        .limit(pageSize)
        .toList();
    TableData<Notification> data = new TableData<>();
    data.setTotalCount(currentNotifications.size());
    data.setFilteredCount(currentNotifications.size());
    data.setItems(includedNotifications);
    return data;
  }

  protected boolean metricsAvailable(Sample sample, Run run, Map<Long, Assay> assaysById,
      MetricCategory metricCategory) {
    if (metricCategory != MetricCategory.LIBRARY_QUALIFICATION
        && metricCategory != MetricCategory.FULL_DEPTH_SEQUENCING) {
      throw new IllegalArgumentException(
          String.format("Unexpected metric category: %s", metricCategory));
    }
    if (sample.getAssayId() == null) {
      return false;
    }
    Assay assay = assaysById.get(sample.getAssayId());
    List<MetricSubcategory> subcategories = assay.getMetricCategories().get(metricCategory);
    for (MetricSubcategory subcategory : subcategories) {
      if (subcategory.getLibraryDesignCode() != null
          && !subcategory.getLibraryDesignCode().equals(sample.getLibraryDesignCode())) {
        continue;
      }
      for (Metric metric : subcategory.getMetrics()) {
        if (filter(metric.getNucleicAcidType(), sample.getNucleicAcidType())
            || filter(metric.getTissueMaterial(), sample.getTissueMaterial())
            || filter(metric.getTissueOrigin(), sample.getTissueOrigin())
            || (!metric.isNegateTissueType()
                && filter(metric.getTissueType(), sample.getTissueType()))
            || (metric.isNegateTissueType() && metric.getTissueType() != null
                && metric.getTissueType().equals(sample.getTissueType()))
            || filter(metric.getContainerModel(), run.getContainerModel())
            || filter(metric.getReadLength(), run.getReadLength())
            || filter(metric.getReadLength2(), run.getReadLength2())) {
          continue;
        }
        if (metricValueMissing(sample, metric)) {
          return false;
        }
      }
    }
    return true;
  }

  private <T> boolean filter(T criteria, T value) {
    return criteria != null && !criteria.equals(value);
  }

  private boolean metricValueMissing(Sample sample, Metric metric) {
    // Only check metrics from GSI-QC-ETL here, as they should be the only ones we need to wait on
    switch (metric.getName()) {
      case "Mean Insert Size":
        return sample.getMeanInsertSize() == null;
      case "Clusters Per Sample":
      case "Pass Filter Clusters":
        return sample.getClustersPerSample() == null;
      case "Duplication Rate":
        return sample.getDuplicationRate() == null;
      case "Mapped to Coding":
        return sample.getMappedToCoding() == null;
      case "rRNA Contamination":
        return sample.getrRnaContamination() == null;
      case "Mean Coverage Deduplicated":
        return sample.getMeanCoverageDeduplicated() == null;
      case "Coverage (Raw)":
        return sample.getRawCoverage() == null;
      case "On Target Reads":
        return sample.getOnTargetReads() == null;
      default:
        return false;
    }
  }

}
