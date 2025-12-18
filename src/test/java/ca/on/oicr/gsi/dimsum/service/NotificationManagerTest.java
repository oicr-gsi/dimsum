package ca.on.oicr.gsi.dimsum.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.Metric;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.MetricSubcategory;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.dimsum.data.IssueState;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;

public class NotificationManagerTest {

  private static final long ASSAY_ID = 1L;
  private static final String RUN_NAME = "RUN1";
  private static final String SUMMARY_SUFFIX = " Dimsum Run QC";
  private static final String SUMMARY = "RUN1 Dimsum Run QC";
  private static final String ISSUE_KEY = "JIRA-123";

  @Mock
  private JiraService jiraService;

  private NotificationManager sut;

  private final LocalDate arbitraryTimestamp = LocalDate.now();
  private final Run pendingQcRun = makeRun(false);
  private final Run qcCompleteRun = makeRun(true);
  private final Map<Long, Assay> assaysById = makeAssays();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    sut = new NotificationManager(null);
    sut.setBaseUrl("https://example.com");
    sut.setIssueTracker(jiraService);
  }

  @Test
  public void testLibraryQualificationMetricsAvailable() {
    assertTrue(sut.metricsAvailable(makeSample(true, false), pendingQcRun, assaysById,
        MetricCategory.LIBRARY_QUALIFICATION));
    assertTrue(sut.metricsAvailable(makeSample(true, true), pendingQcRun, assaysById,
        MetricCategory.LIBRARY_QUALIFICATION));
  }

  @Test
  public void testLibraryQualificationMetricsNotAvailable() {
    assertFalse(sut.metricsAvailable(makeSample(false, false), pendingQcRun, assaysById,
        MetricCategory.LIBRARY_QUALIFICATION));
    assertFalse(sut.metricsAvailable(makeSample(false, true), pendingQcRun, assaysById,
        MetricCategory.LIBRARY_QUALIFICATION));
  }

  @Test
  public void testFullDepthMetricsAvailable() {
    assertTrue(sut.metricsAvailable(makeSample(true, false), pendingQcRun, assaysById,
        MetricCategory.FULL_DEPTH_SEQUENCING));
    assertTrue(sut.metricsAvailable(makeSample(true, true), pendingQcRun, assaysById,
        MetricCategory.FULL_DEPTH_SEQUENCING));
  }

  @Test
  public void testFullDepthMetricsNotAvailable() {
    assertFalse(sut.metricsAvailable(makeSample(false, false), pendingQcRun, assaysById,
        MetricCategory.FULL_DEPTH_SEQUENCING));
    assertFalse(sut.metricsAvailable(makeSample(false, true), pendingQcRun, assaysById,
        MetricCategory.FULL_DEPTH_SEQUENCING));
  }

  @Test
  public void testReadyForLibraryQualificationQcAll() {
    RunAndLibraries runAndLibraries = makeRunAndLibraries(false);
    runAndLibraries.getLibraryQualifications().add(makeSample(true, false));
    runAndLibraries.getLibraryQualifications().add(makeSample(true, false));
    assertTrue(sut.readyForLibraryQualificationQc(runAndLibraries, assaysById));
  }

  @Test
  public void testReadyForLibraryQualificationQcPartial() {
    // Still considered ready if ANY library is missing QC
    RunAndLibraries runAndLibraries = makeRunAndLibraries(true);
    runAndLibraries.getLibraryQualifications().add(makeSample(true, true));
    runAndLibraries.getLibraryQualifications().add(makeSample(true, false));
    assertTrue(sut.readyForLibraryQualificationQc(runAndLibraries, assaysById));
  }

  @Test
  public void testReadyForLibraryQualificationQcRun() {
    // Still considered ready if ONLY the run is missing QC
    RunAndLibraries runAndLibraries = makeRunAndLibraries(false);
    runAndLibraries.getLibraryQualifications().add(makeSample(true, true));
    runAndLibraries.getLibraryQualifications().add(makeSample(true, true));
    assertTrue(sut.readyForLibraryQualificationQc(runAndLibraries, assaysById));
  }

  @Test
  public void testNotReadyForLibraryQualificationQcPartial() {
    // Only consider ready if ALL libraries have metrics available
    RunAndLibraries runAndLibraries = makeRunAndLibraries(false);
    runAndLibraries.getLibraryQualifications().add(makeSample(true, false));
    runAndLibraries.getLibraryQualifications().add(makeSample(false, false));
    assertFalse(sut.readyForLibraryQualificationQc(runAndLibraries, assaysById));
  }

  @Test
  public void testNotReadyForLibraryQualificationQcDone() {
    // Only consider ready if a signoff is missing
    RunAndLibraries runAndLibraries = makeRunAndLibraries(true);
    runAndLibraries.getLibraryQualifications().add(makeSample(true, true));
    runAndLibraries.getLibraryQualifications().add(makeSample(true, true));
    assertFalse(sut.readyForLibraryQualificationQc(runAndLibraries, assaysById));
  }

  @Test
  public void testReadyForFullDepthQcAll() {
    RunAndLibraries runAndLibraries = makeRunAndLibraries(false);
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, false));
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, false));
    assertTrue(sut.readyForFullDepthQc(runAndLibraries, assaysById));
  }

  @Test
  public void testReadyForFullDepthQcPartial() {
    // Still considered ready if ANY library is missing QC
    RunAndLibraries runAndLibraries = makeRunAndLibraries(true);
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, true));
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, false));
    assertTrue(sut.readyForFullDepthQc(runAndLibraries, assaysById));
  }

  @Test
  public void testReadyForFullDepthQcRun() {
    // Still considered ready if ONLY the run is missing QC
    RunAndLibraries runAndLibraries = makeRunAndLibraries(false);
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, true));
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, true));
    assertTrue(sut.readyForFullDepthQc(runAndLibraries, assaysById));
  }

  @Test
  public void testReadyForFullDepthQcPartialAvailable() {
    // Consider ready if ANY libraries has metrics available and requires QC
    RunAndLibraries runAndLibraries = makeRunAndLibraries(false);
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, false));
    runAndLibraries.getFullDepthSequencings().add(makeSample(false, false));
    assertTrue(sut.readyForFullDepthQc(runAndLibraries, assaysById));
  }

  @Test
  public void testReadyForFullDepthQcRunPartialAvailable() {
    // Still considered ready if ANY library has metrics available and ONLY the run is missing QC
    RunAndLibraries runAndLibraries = makeRunAndLibraries(false);
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, true));
    runAndLibraries.getFullDepthSequencings().add(makeSample(false, false));
    assertTrue(sut.readyForFullDepthQc(runAndLibraries, assaysById));
  }

  @Test
  public void testNotReadyForFullDepthQcDone() {
    // Only consider ready if a signoff is missing
    RunAndLibraries runAndLibraries = makeRunAndLibraries(true);
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, true));
    runAndLibraries.getFullDepthSequencings().add(makeSample(true, true));
    assertFalse(sut.readyForFullDepthQc(runAndLibraries, assaysById));
  }

  @Test
  public void testNotReadyForFullDepthQcDoneRun() {
    // Only consider ready if a library has metrics available
    RunAndLibraries runAndLibraries = makeRunAndLibraries(false);
    runAndLibraries.getFullDepthSequencings().add(makeSample(false, false));
    runAndLibraries.getFullDepthSequencings().add(makeSample(false, false));
    assertFalse(sut.readyForFullDepthQc(runAndLibraries, assaysById));
  }

  @Test
  public void testNewFullDepthIssue() {
    Map<String, RunAndLibraries> data = makeData(true, 1, 1, 0, 0);
    when(jiraService.getOpenIssues(anyString())).thenReturn(Collections.emptySet());
    when(jiraService.getIssueBySummary(SUMMARY)).thenReturn(null);
    sut.update(data, assaysById);
    verify(jiraService).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService).getIssueBySummary(SUMMARY);
    verify(jiraService).createIssue(Mockito.eq(SUMMARY), anyString());
    verifyNoMoreInteractions(jiraService);
  }

  @Test
  public void testUnchangedSincePostFullDepthIssue() {
    Map<String, RunAndLibraries> data = makeData(true, 1, 1, 0, 0);
    Issue issue = makeIssue("R1A1Q1D0");
    when(jiraService.getOpenIssues(anyString())).thenReturn(Collections.singleton(issue));
    when(jiraService.getIssueByKey(issue.getKey())).thenReturn(issue);
    when(jiraService.getIssueState(issue)).thenReturn(IssueState.OPEN);
    sut.update(data, assaysById);
    verify(jiraService).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService).getIssueByKey(issue.getKey());
    verify(jiraService).getIssueState(issue);
    verifyNoMoreInteractions(jiraService);
  }

  @Test
  public void testUnchangedSinceUpdateFullDepthIssue() {
    Map<String, RunAndLibraries> data = makeData(true, 0, 0, 2, 0);
    Issue issue = makeIssue("R1A0Q2D0", "R1A0Q0D2");
    when(jiraService.getOpenIssues(anyString())).thenReturn(Collections.singleton(issue));
    when(jiraService.getIssueByKey(issue.getKey())).thenReturn(issue);
    when(jiraService.getIssueState(issue)).thenReturn(IssueState.OPEN);
    sut.update(data, assaysById);
    verify(jiraService).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService).getIssueByKey(issue.getKey());
    verify(jiraService).getIssueState(issue);
    verifyNoMoreInteractions(jiraService);
  }

  @Test
  public void testUpdateFullDepthIssue() {
    Map<String, RunAndLibraries> data = makeData(true, 1, 0, 1, 0);
    Issue issue = makeIssue("R1A1Q1D0");
    when(jiraService.getOpenIssues(anyString())).thenReturn(Collections.singleton(issue));
    when(jiraService.getIssueByKey(issue.getKey())).thenReturn(issue);
    when(jiraService.getIssueState(issue)).thenReturn(IssueState.OPEN);
    sut.update(data, assaysById);
    verify(jiraService).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService).getIssueState(issue);
    verify(jiraService).getIssueByKey(issue.getKey());
    verify(jiraService).postComment(any(), anyString());
    verifyNoMoreInteractions(jiraService);
  }

  @Test
  public void testPauseFullDepthIssue() {
    Map<String, RunAndLibraries> data = makeData(true, 1, 0, 0, 1);
    Issue issue = makeIssue("R3A1Q0D1");
    when(jiraService.getOpenIssues(anyString())).thenReturn(Collections.singleton(issue));
    when(jiraService.getIssueState(issue)).thenReturn(IssueState.OPEN);
    sut.update(data, assaysById);
    verify(jiraService).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService).getIssueState(issue);
    verify(jiraService).pauseIssue(any(), anyString());
    verifyNoMoreInteractions(jiraService);
  }

  @Test
  public void testReopenFullDepthIssue() {
    Map<String, RunAndLibraries> data = makeData(true, 0, 1, 0, 1);
    Issue issue = makeIssue("R3A1Q0D0");
    when(jiraService.getOpenIssues(anyString())).thenReturn(Collections.emptySet());
    when(jiraService.getIssueBySummary(SUMMARY)).thenReturn(issue);
    when(jiraService.getIssueState(issue)).thenReturn(IssueState.PAUSED);
    sut.update(data, assaysById);
    verify(jiraService).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService).getIssueBySummary(SUMMARY);
    verify(jiraService, times(2)).getIssueState(issue);
    verify(jiraService).reopenIssue(any(), anyString());
    verifyNoMoreInteractions(jiraService);
  }

  @Test
  public void testCloseFullDepthIssue() {
    Map<String, RunAndLibraries> data = makeData(true, 0, 0, 0, 2);
    Issue issue = makeIssue("R3A0Q1D0");
    when(jiraService.getOpenIssues(anyString())).thenReturn(Collections.singleton(issue));
    sut.update(data, assaysById);
    verify(jiraService).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService).closeIssue(any(), anyString());
    verifyNoMoreInteractions(jiraService);
  }

  @Test
  public void testOverriddenFullDepthIssue() {
    Map<String, RunAndLibraries> data = makeData(true, 0, 1, 0, 1);
    Issue issue = makeIssue("R3A1Q0D0");
    when(jiraService.getOpenIssues(anyString())).thenReturn(Collections.emptySet());
    when(jiraService.getIssueBySummary(SUMMARY)).thenReturn(issue);
    when(jiraService.getIssueState(issue)).thenReturn(IssueState.PAUSED);
    // Verify that it would normally be reopened
    sut.update(data, assaysById);
    verify(jiraService).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService).getIssueBySummary(SUMMARY);
    verify(jiraService, times(2)).getIssueState(issue);
    verify(jiraService).reopenIssue(any(), anyString());
    verifyNoMoreInteractions(jiraService);

    // Verify that it is not reopened if overridden
    when(jiraService.getIssueState(issue)).thenReturn(IssueState.OVERRIDDEN);
    sut.update(data, assaysById);
    // Note: these verifications count the previously verified invocations too
    verify(jiraService, times(2)).getOpenIssues(SUMMARY_SUFFIX);
    verify(jiraService, times(2)).getIssueBySummary(SUMMARY);
    verify(jiraService, times(3)).getIssueState(issue);
    verifyNoMoreInteractions(jiraService);
  }

  private Map<Long, Assay> makeAssays() {
    Map<Long, Assay> map = new HashMap<>();
    Assay assay = mock(Assay.class);
    when(assay.getId()).thenReturn(ASSAY_ID);
    Map<MetricCategory, List<MetricSubcategory>> metricCategories = new HashMap<>();
    List<Metric> metrics = new ArrayList<>();
    Metric metric = mock(Metric.class);
    when(metric.getName()).thenReturn("Mean Insert Size");
    metrics.add(metric);
    MetricSubcategory subcat = new MetricSubcategory.Builder().metrics(metrics).build();
    metricCategories.put(MetricCategory.LIBRARY_QUALIFICATION, Collections.singletonList(subcat));
    metricCategories.put(MetricCategory.FULL_DEPTH_SEQUENCING, Collections.singletonList(subcat));
    when(assay.getMetricCategories()).thenReturn(metricCategories);
    map.put(ASSAY_ID, assay);
    return map;
  }

  private Run makeRun(boolean signoffsDone) {
    Run run = mock(Run.class);
    when(run.getName()).thenReturn(RUN_NAME);
    if (signoffsDone) {
      when(run.getDataReviewDate()).thenReturn(arbitraryTimestamp);
    }
    return run;
  }

  private Map<String, RunAndLibraries> makeData(boolean runSignoffsDone, int pendingAnalysisCount,
      int pendingQcCount, int pendingDataReviewCount, int doneCount) {
    Map<String, RunAndLibraries> data = new HashMap<>();
    RunAndLibraries runAndLibraries = makeRunAndLibraries(runSignoffsDone);
    for (int i = 0; i < pendingAnalysisCount; i++) {
      runAndLibraries.getFullDepthSequencings().add(makeSample(false, false, false));
    }
    for (int i = 0; i < pendingQcCount; i++) {
      runAndLibraries.getFullDepthSequencings().add(makeSample(true, false, false));
    }
    for (int i = 0; i < pendingDataReviewCount; i++) {
      runAndLibraries.getFullDepthSequencings().add(makeSample(true, true, false));
    }
    for (int i = 0; i < doneCount; i++) {
      runAndLibraries.getFullDepthSequencings().add(makeSample(true, true, true));
    }
    data.put(runAndLibraries.getRun().getName(), runAndLibraries);
    return data;
  }

  private RunAndLibraries makeRunAndLibraries(boolean runSignoffsDone) {
    RunAndLibraries runAndLibraries = mock(RunAndLibraries.class);
    when(runAndLibraries.getRun()).thenReturn(runSignoffsDone ? qcCompleteRun : pendingQcRun);
    when(runAndLibraries.getLibraryQualifications()).thenReturn(new HashSet<>());
    when(runAndLibraries.getFullDepthSequencings()).thenReturn(new HashSet<>());
    return runAndLibraries;
  }

  private Sample makeSample(boolean metricAvailable, boolean signoffsDone) {
    return makeSample(metricAvailable, signoffsDone, signoffsDone);
  }

  private Sample makeSample(boolean metricAvailable, boolean qcDone, boolean dataReviewDone) {
    Sample sample = mock(Sample.class);
    when(sample.getAssayIds()).thenReturn(Collections.singleton(ASSAY_ID));
    when(sample.getMeanInsertSize()).thenReturn(metricAvailable ? BigDecimal.TEN : null);
    when(sample.getQcDate()).thenReturn(qcDone ? arbitraryTimestamp : null);
    when(sample.getDataReviewDate()).thenReturn(dataReviewDone ? arbitraryTimestamp : null);
    return sample;
  }

  private Issue makeIssue(String code) {
    return makeIssue(code, null);
  }

  private Issue makeIssue(String descriptionCode, String commentCode) {
    Issue issue = mock(Issue.class);
    when(issue.getKey()).thenReturn(ISSUE_KEY);
    when(issue.getSummary()).thenReturn(SUMMARY);
    when(issue.getDescription()).thenReturn(makeComment(descriptionCode));
    List<Comment> comments = new ArrayList<>();
    if (commentCode != null) {
      Comment comment = mock(Comment.class);
      when(comment.getCreationDate()).thenReturn(DateTime.now());
      when(comment.getBody()).thenReturn(makeComment(commentCode));
      comments.add(comment);
    }
    when(issue.getComments()).thenReturn(comments);
    return issue;
  }

  private static String makeComment(String code) {
    return """
        (Human readable stuff here)

        Internal use: <%s>""".formatted(code);
  }

}
