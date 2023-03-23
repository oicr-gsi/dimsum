package ca.on.oicr.gsi.dimsum.service;

import java.util.Arrays;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ca.on.oicr.gsi.dimsum.CaseLoader;
import ca.on.oicr.gsi.dimsum.FrontEndConfig;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.CaseData;
import ca.on.oicr.gsi.dimsum.data.MetricCategory;
import ca.on.oicr.gsi.dimsum.data.OmittedSample;
import ca.on.oicr.gsi.dimsum.data.Project;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;
import ca.on.oicr.gsi.dimsum.data.ProjectSummaryField;
import ca.on.oicr.gsi.dimsum.data.ProjectSummaryRow;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;
import ca.on.oicr.gsi.dimsum.data.TestTableView;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedSampleFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedSampleFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedSampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummaryFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummaryFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummarySort;
import ca.on.oicr.gsi.dimsum.service.filtering.RequisitionSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TestTableViewSort;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.service.filtering.RunFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.RunFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.RunSort;

@Service
public class CaseService {

  private static final Logger log = LoggerFactory.getLogger(CaseService.class);

  @Autowired
  private CaseLoader dataLoader;

  @Autowired
  private FrontEndConfig frontEndConfig;

  @Autowired
  private NotificationManager notificationManager;

  private CaseData caseData;

  private int refreshFailures = 0;

  public CaseService(@Autowired MeterRegistry meterRegistry) {
    if (meterRegistry != null) {
      Gauge.builder("case_data_refresh_failures", this::getRefreshFailures)
          .description("Number of consecutive failures to refresh the case data")
          .register(meterRegistry);
      Gauge.builder("case_data_age_seconds", () -> this.getDataAge().toSeconds())
          .description("Time since case data was refreshed").register(meterRegistry);
    }
  }

  protected void setCaseData(CaseData caseData) {
    this.caseData = caseData;
  }

  private int getRefreshFailures() {
    return refreshFailures;
  }

  public Duration getDataAge() {
    CaseData currentData = caseData;
    if (currentData == null) {
      return Duration.ZERO;
    }
    return Duration.between(currentData.getTimestamp(), ZonedDateTime.now());
  }

  public List<Case> getCases(CaseFilter baseFilter) {
    CaseData currentData = caseData;
    if (currentData == null) {
      throw new IllegalStateException("Cases have not been loaded yet");
    }
    if (baseFilter != null) {
      return currentData.getCases().stream().filter(baseFilter.casePredicate()).toList();
    } else {
      return currentData.getCases();
    }
  }

  public TableData<Case> getCases(int pageSize, int pageNumber, CaseSort sort, boolean descending,
      CaseFilter baseFilter, Collection<CaseFilter> filters) {
    List<Case> baseCases = getCases(baseFilter);
    Stream<Case> stream = filterCases(baseCases, filters);

    if (sort == null) {
      sort = CaseSort.LAST_ACTIVITY;
      descending = true;
    }
    stream = stream.sorted(descending ? sort.comparator().reversed() : sort.comparator());

    List<Case> filteredCases =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());

    TableData<Case> data = new TableData<>();
    data.setTotalCount(baseCases.size());
    data.setFilteredCount(filterCases(baseCases, filters).count());
    data.setItems(filteredCases);
    return data;
  }

  public Set<String> getMatchingAssayNames(String prefix) {
    return caseData.getAssayNames().stream()
        .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
        .collect(Collectors.toSet());
  }

  public Set<String> getMatchingRequisitionNames(String prefix) {
    return caseData.getRequisitionNames().stream()
        .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
        .collect(Collectors.toSet());
  }

  public Set<String> getMatchingProjectNames(String prefix) {
    return caseData.getProjectNames().stream()
        .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
        .collect(Collectors.toSet());
  }

  public Set<String> getMatchingDonorNames(String prefix) {
    return caseData.getDonorNames().stream()
        .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
        .collect(Collectors.toSet());
  }

  public Set<String> getMatchingRunNames(String prefix) {
    return caseData.getRunNames().stream()
        .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
        .collect(Collectors.toSet());
  }

  public Set<String> getMatchingTestNames(String prefix) {
    return caseData.getTestNames().stream()
        .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
        .collect(Collectors.toSet());
  }

  public TableData<Sample> getReceipts(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        MetricCategory.RECEIPT);
  }

  public TableData<Sample> getExtractions(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        MetricCategory.EXTRACTION);
  }

  public TableData<Sample> getLibraryPreparations(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        MetricCategory.LIBRARY_PREP);
  }

  public TableData<Sample> getLibraryQualifications(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        MetricCategory.LIBRARY_QUALIFICATION);
  }

  public TableData<Sample> getFullDepthSequencings(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        MetricCategory.FULL_DEPTH_SEQUENCING);
  }

  private TableData<Sample> getSamples(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters,
      MetricCategory requestCategory) {
    List<Case> cases = getCases(baseFilter);
    TableData<Sample> data = new TableData<>();
    data.setTotalCount(
        cases.stream().flatMap(getAllGateSamples(requestCategory)).distinct().count());
    List<Sample> samples = filterSamples(cases, filters, requestCategory).distinct().toList();
    data.setFilteredCount(samples.size());
    data.setItems(samples.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1)).limit(pageSize).toList());
    return data;
  }

  private static Function<Case, Stream<Sample>> getAllGateSamples(MetricCategory category) {
    switch (category) {
      case RECEIPT:
        return kase -> kase.getReceipts().stream();
      case EXTRACTION:
      case LIBRARY_PREP:
      case LIBRARY_QUALIFICATION:
      case FULL_DEPTH_SEQUENCING:
        return getTestGateSamples(category);
      default:
        throw new IllegalArgumentException("Gate does not contain samples");
    }
  }

  private static Function<Case, Stream<Sample>> getTestGateSamples(MetricCategory category) {
    return kase -> kase.getTests().stream().flatMap(getAllTestGateSamples(category));
  }

  private static Function<Test, Stream<Sample>> getAllTestGateSamples(MetricCategory category) {
    switch (category) {
      case EXTRACTION:
        return test -> test.getExtractions().stream();
      case LIBRARY_PREP:
        return test -> test.getLibraryPreparations().stream();
      case LIBRARY_QUALIFICATION:
        return test -> test.getLibraryQualifications().stream();
      case FULL_DEPTH_SEQUENCING:
        return test -> test.getFullDepthSequencings().stream();
      default:
        throw new IllegalArgumentException("Not a test gate");
    }
  }

  public TableData<Requisition> getRequisitions(int pageSize, int pageNumber, RequisitionSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    List<Case> cases = getCases(baseFilter);
    TableData<Requisition> data = new TableData<>();
    data.setTotalCount(
        cases.stream().map(Case::getRequisition).distinct().count());
    List<Requisition> requisitions = filterRequisitions(cases, filters).distinct().toList();
    data.setFilteredCount(requisitions.size());
    data.setItems(requisitions.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1)).limit(pageSize).toList());
    return data;
  }

  public TableData<Run> getRuns(int pageSize, int pageNumber, RunSort sort, boolean descending,
      Collection<RunFilter> filters) {
    List<Run> baseRuns =
        caseData.getRunsAndLibraries().stream().map(RunAndLibraries::getRun).toList();
    Stream<Run> stream = filterRuns(baseRuns, filters);
    if (sort == null) {
      sort = RunSort.COMPLETION_DATE;
      descending = true;
    }
    stream = stream.sorted(descending ? sort.comparator().reversed() : sort.comparator());
    List<Run> filteredRuns =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());
    TableData<Run> data = new TableData<>();
    data.setTotalCount(baseRuns.size());
    data.setFilteredCount(filterRuns(baseRuns, filters).count());
    data.setItems(filteredRuns);
    return data;
  }

  public TableData<OmittedSample> getOmittedSamples(int pageSize, int pageNumber,
      OmittedSampleSort sort, boolean descending, Collection<OmittedSampleFilter> filters) {
    List<OmittedSample> baseSamples = caseData.getOmittedSamples();
    Stream<OmittedSample> stream = filterOmittedSamples(baseSamples, filters);
    if (sort == null) {
      sort = OmittedSampleSort.CREATED;
      descending = true;
    }
    stream = stream.sorted(descending ? sort.comparator().reversed() : sort.comparator());
    List<OmittedSample> filteredSamples =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());
    TableData<OmittedSample> data = new TableData<>();
    data.setTotalCount(baseSamples.size());
    data.setFilteredCount(filterOmittedSamples(baseSamples, filters).count());
    data.setItems(filteredSamples);
    return data;
  }

  public TableData<ProjectSummary> getProjects(int pageSize, int pageNumber,
      ProjectSummarySort sort,
      boolean descending, Collection<ProjectSummaryFilter> filters) {
    List<ProjectSummary> baseProjectSummaries = caseData.getProjectSummaries().stream().toList();
    Stream<ProjectSummary> stream = filterProjectSummaries(baseProjectSummaries, filters);

    if (sort == null) {
      sort = ProjectSummarySort.NAME;
      descending = true;
    }
    stream = stream.sorted(descending ? sort.comparator().reversed() : sort.comparator());

    List<ProjectSummary> filteredProjectSummaries =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());

    TableData<ProjectSummary> data = new TableData<>();
    data.setTotalCount(baseProjectSummaries.size());
    data.setFilteredCount(filterProjectSummaries(baseProjectSummaries, filters).count());
    data.setItems(filteredProjectSummaries);
    return data;
  }

  public TableData<ProjectSummaryRow> getProjectSummaryRows(String name) {
    ProjectSummary projectSummary = caseData.getProjectSummariesByName().get(name);

    ProjectSummaryRow pendingWork = new ProjectSummaryRow.Builder()
        .title("Pending Work")
        .extraction(
            new ProjectSummaryField(projectSummary.getExtractionPendingCount(),
                "PENDING", "Extraction"))
        .libraryPreparation(
            new ProjectSummaryField(projectSummary.getLibraryPrepPendingCount(),
                "PENDING", "Library Preparation"))
        .libraryQualification(
            new ProjectSummaryField(projectSummary.getLibraryQualPendingCount(),
                "PENDING", "Library Qualification"))
        .fullDepthSequencing(
            new ProjectSummaryField(projectSummary.getFullDepthSeqPendingCount(),
                "PENDING", "Full-Depth Sequencing"))
        .informaticsReview(
            new ProjectSummaryField(projectSummary.getInformaticsPendingCount(),
                "PENDING", "Informatics Review"))
        .draftReport(
            new ProjectSummaryField(projectSummary.getDraftReportPendingCount(),
                "PENDING", "Draft Report"))
        .finalReport(
            new ProjectSummaryField(projectSummary.getFinalReportPendingCount(),
                "PENDING", "Final Report"))
        .build();

    ProjectSummaryRow pendingQc = new ProjectSummaryRow.Builder()
        .title("Pending QC")
        .receipt(
            new ProjectSummaryField(projectSummary.getReceiptPendingQcCount(),
                "PENDING", "Receipt QC"))
        .extraction(
            new ProjectSummaryField(projectSummary.getExtractionPendingQcCount(),
                "PENDING", "Extraction QC Sign-Off"))
        .libraryPreparation(
            new ProjectSummaryField(projectSummary.getLibraryPrepPendingQcCount(),
                "PENDING", "Library QC Sign-Off"))
        .libraryQualification(
            new ProjectSummaryField(projectSummary.getLibraryQualPendingQcCount(),
                "PENDING", "LLibrary Qualification QC Sign-Off"))
        .fullDepthSequencing(
            new ProjectSummaryField(projectSummary.getFullDepthSeqPendingQcCount(),
                "PENDING", "Full-Depth Sequencing QC Sign-Off"))
        .build();

    ProjectSummaryRow completed = new ProjectSummaryRow.Builder()
        .title("Completed")
        .receipt(
            new ProjectSummaryField(projectSummary.getReceiptCompletedCount(), "COMPLETED",
                "Receipt"))
        .extraction(
            new ProjectSummaryField(projectSummary.getExtractionCompletedCount(), "COMPLETED",
                "Extraction"))
        .libraryPreparation(
            new ProjectSummaryField(projectSummary.getLibraryPrepCompletedCount(),
                "COMPLETED", "Library Preparation"))
        .libraryQualification(
            new ProjectSummaryField(projectSummary.getLibraryQualCompletedCount(),
                "COMPLETED", "LLibrary Qualification"))
        .fullDepthSequencing(
            new ProjectSummaryField(projectSummary.getFullDepthSeqCompletedCount(),
                "COMPLETED", "Full-Depth Sequencing"))
        .informaticsReview(
            new ProjectSummaryField(projectSummary.getInformaticsCompletedCount(),
                "COMPLETED", "Informatics Review"))
        .draftReport(
            new ProjectSummaryField(projectSummary.getDraftReportCompletedCount(),
                "COMPLETED", "Draft Report"))
        .finalReport(
            new ProjectSummaryField(projectSummary.getFinalReportCompletedCount(),
                "COMPLETED", "Final Report"))
        .build();

    TableData<ProjectSummaryRow> data = new TableData<>();
    data.setItems(Arrays.asList(pendingWork, pendingQc, completed));
    data.setFilteredCount(data.getItems().size());
    data.setTotalCount(data.getItems().size());
    return data;
  }

  public TableData<TestTableView> getTestTableViews(int pageSize, int pageNumber,
      TestTableViewSort sort, boolean descending,
      CaseFilter baseFilter, Collection<CaseFilter> filters) {
    List<Case> cases = getCases(baseFilter);
    TableData<TestTableView> data = new TableData<>();
    data.setTotalCount(
        cases.stream().flatMap(kase -> kase.getTests().stream()).count());
    List<TestTableView> testTableViews = filterTestTableViews(cases, filters).toList();
    data.setFilteredCount(testTableViews.size());
    data.setItems(testTableViews.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1)).limit(pageSize).toList());
    return data;
  }

  private Stream<Case> filterCases(List<Case> cases, Collection<CaseFilter> filters) {
    Stream<Case> stream = cases.stream();
    if (filters != null && !filters.isEmpty()) {
      Map<CaseFilterKey, Predicate<Case>> filterMap =
          buildFilterMap(filters, CaseFilter::casePredicate);
      for (Predicate<Case> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
  }

  private Stream<Run> filterRuns(List<Run> runs, Collection<RunFilter> filters) {
    Stream<Run> stream = runs.stream();
    if (filters != null && !filters.isEmpty()) {
      Map<RunFilterKey, Predicate<Run>> filterMap = new HashMap<>();
      for (RunFilter filter : filters) {
        RunFilterKey key = filter.getKey();
        if (filterMap.containsKey(key)) {
          filterMap.put(key, filterMap.get(key).or(filter.predicate()));
        } else {
          filterMap.put(key, filter.predicate());
        }
      }
      for (Predicate<Run> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
  }

  private Stream<OmittedSample> filterOmittedSamples(List<OmittedSample> samples,
      Collection<OmittedSampleFilter> filters) {
    Stream<OmittedSample> stream = samples.stream();
    if (filters != null && !filters.isEmpty()) {
      Map<OmittedSampleFilterKey, Predicate<OmittedSample>> filterMap = new HashMap<>();
      for (OmittedSampleFilter filter : filters) {
        OmittedSampleFilterKey key = filter.getKey();
        if (filterMap.containsKey(key)) {
          filterMap.put(key, filterMap.get(key).or(filter.predicate()));
        } else {
          filterMap.put(key, filter.predicate());
        }
      }
      for (Predicate<OmittedSample> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
  }

  private Stream<Test> filterTests(List<Case> cases, Collection<CaseFilter> filters) {
    Stream<Test> stream = filterCases(cases, filters)
        .flatMap(kase -> kase.getTests().stream());
    if (filters != null && !filters.isEmpty()) {
      Map<CaseFilterKey, Predicate<Test>> filterMap =
          buildFilterMap(filters, CaseFilter::testPredicate);
      for (Predicate<Test> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
  }

  private Stream<Sample> filterSamples(List<Case> cases, Collection<CaseFilter> filters,
      MetricCategory requestCategory) {
    Stream<Sample> stream = null;
    if (requestCategory == MetricCategory.RECEIPT) {
      stream = filterCases(cases, filters).flatMap(kase -> kase.getReceipts().stream());
    } else {
      stream = filterTests(cases, filters)
          .flatMap(getAllTestGateSamples(requestCategory));
    }
    if (filters != null && !filters.isEmpty()) {
      Map<CaseFilterKey, Predicate<Sample>> filterMap =
          buildFilterMap(filters, filter -> filter.samplePredicate(requestCategory));
      for (Predicate<Sample> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
  }

  private Stream<Requisition> filterRequisitions(List<Case> cases, Collection<CaseFilter> filters) {
    Stream<Requisition> stream = filterCases(cases, filters)
        .map(Case::getRequisition);
    if (filters != null && !filters.isEmpty()) {
      Map<CaseFilterKey, Predicate<Requisition>> filterMap =
          buildFilterMap(filters, CaseFilter::requisitionPredicate);
      for (Predicate<Requisition> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;

  }

  private Stream<ProjectSummary> filterProjectSummaries(List<ProjectSummary> projectSummaries,
      Collection<ProjectSummaryFilter> filters) {
    Stream<ProjectSummary> stream = projectSummaries.stream();
    if (filters != null && !filters.isEmpty()) {
      Map<ProjectSummaryFilterKey, Predicate<ProjectSummary>> filterMap = new HashMap<>();
      for (ProjectSummaryFilter filter : filters) {
        ProjectSummaryFilterKey key = filter.getKey();
        if (filterMap.containsKey(key)) {
          filterMap.put(key, filterMap.get(key).or(filter.predicate()));
        } else {
          filterMap.put(key, filter.predicate());
        }
      }
      for (Predicate<ProjectSummary> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
  }

  private Stream<TestTableView> filterTestTableViews(List<Case> cases,
      Collection<CaseFilter> filters) {
    Stream<TestTableView> stream = filterCases(cases, filters)
        .flatMap(kase -> kase.getTests().stream().map(test -> new TestTableView(kase, test)));
    if (filters != null && !filters.isEmpty()) {
      Map<CaseFilterKey, Predicate<TestTableView>> filterMap =
          buildFilterMap(filters, CaseFilter::testTableViewPredicate);
      for (Predicate<TestTableView> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
  }

  private <T> Map<CaseFilterKey, Predicate<T>> buildFilterMap(Collection<CaseFilter> filters,
      Function<CaseFilter, Predicate<T>> getPredicate) {
    Map<CaseFilterKey, Predicate<T>> filterMap = new HashMap<>();
    for (CaseFilter filter : filters) {
      CaseFilterKey key = filter.getKey();
      if (filterMap.containsKey(key)) {
        filterMap.put(key, filterMap.get(key).or(getPredicate.apply(filter)));
      } else {
        filterMap.put(key, getPredicate.apply(filter));
      }
    }
    return filterMap;
  }

  public RunAndLibraries getRunAndLibraries(String name) {
    return caseData.getRunAndLibraries(name);
  }

  public TableData<Sample> getLibraryQualificationsForRun(String runName, int pageSize,
      int pageNumber, SampleSort sort, boolean descending) {
    return getRunLibraries(runName, pageSize, pageNumber, sort, descending,
        RunAndLibraries::getLibraryQualifications);
  }

  public TableData<Sample> getFullDepthSequencingsForRun(String runName, int pageSize,
      int pageNumber, SampleSort sort, boolean descending) {
    return getRunLibraries(runName, pageSize, pageNumber, sort, descending,
        RunAndLibraries::getFullDepthSequencings);
  }

  private TableData<Sample> getRunLibraries(String runName, int pageSize,
      int pageNumber, SampleSort sort, boolean descending,
      Function<RunAndLibraries, Set<Sample>> getSamples) {
    RunAndLibraries runAndLibraries = caseData.getRunAndLibraries(runName);
    Set<Sample> samples = runAndLibraries == null ? Collections.emptySet()
        : getSamples.apply(runAndLibraries);
    TableData<Sample> data = new TableData<>();
    data.setTotalCount(samples.size());
    data.setFilteredCount(samples.size());
    data.setItems(samples.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1))
        .limit(pageSize)
        .toList());
    return data;
  }

  @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
  private void refreshData() {
    try {
      ZonedDateTime previousTimestamp = caseData == null ? null : caseData.getTimestamp();
      CaseData newData = dataLoader.load(previousTimestamp);
      refreshFailures = 0;
      if (newData != null) {
        caseData = newData;
        updateFrontEndConfig();
        notificationManager.update(newData.getRunsAndLibrariesByName(), newData.getAssaysById());
      }
    } catch (Exception e) {
      refreshFailures++;
      log.error("Failed to refresh case data", e);
    }
  }

  private void updateFrontEndConfig() {
    frontEndConfig.setPipelines(caseData.getCases().stream()
        .flatMap(kase -> kase.getProjects().stream())
        .map(Project::getPipeline)
        .collect(Collectors.toSet()));
    frontEndConfig.setAssaysById(caseData.getAssaysById());
  }

}
