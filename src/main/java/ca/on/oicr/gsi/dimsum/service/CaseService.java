package ca.on.oicr.gsi.dimsum.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.OmittedRunSample;
import ca.on.oicr.gsi.cardea.data.OmittedSample;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.CaseLoader;
import ca.on.oicr.gsi.dimsum.FrontEndConfig;
import ca.on.oicr.gsi.dimsum.data.CacheUpdatedCase;
import ca.on.oicr.gsi.dimsum.data.CaseData;
import ca.on.oicr.gsi.dimsum.data.NabuSavedSignoff;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;
import ca.on.oicr.gsi.dimsum.data.ProjectSummaryField;
import ca.on.oicr.gsi.dimsum.data.ProjectSummaryRow;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.TestTableView;
import ca.on.oicr.gsi.dimsum.data.external.ExternalCase;
import ca.on.oicr.gsi.dimsum.security.DimsumPrincipal;
import ca.on.oicr.gsi.dimsum.security.SecurityManager;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.CompletedGate;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedRunSampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedSampleFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedSampleFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedSampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.PendingState;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummaryFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummaryFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummarySort;
import ca.on.oicr.gsi.dimsum.service.filtering.RunFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.RunFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.RunSort;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;
import ca.on.oicr.gsi.dimsum.service.filtering.TestTableViewSort;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class CaseService {

  // overlap to maintain signoffs that may have been completed during data refresh
  private static final int CACHE_OVERLAP_MINUTES = 10;

  private static final Logger log = LoggerFactory.getLogger(CaseService.class);

  @Autowired
  private CaseLoader dataLoader;

  @Autowired
  private FrontEndConfig frontEndConfig;

  @Autowired
  private NotificationManager notificationManager;

  @Autowired
  private SecurityManager securityManager;

  private CaseData caseData;

  // Note: Any access of cachedSignoffsByCaseId should synchronize on cachedSignoffsByCaseId to
  // ensure updates are never missed before/during/after refresh
  private Map<String, List<NabuSavedSignoff>> cachedSignoffsByCaseId = new HashMap<>();
  private List<Case> cacheUpdatedCases;

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
    refreshCacheUpdatedCases();
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

  public Case getCase(String caseId) {
    return cacheUpdatedCases.stream()
        .filter(new CaseFilter(CaseFilterKey.CASE_ID, caseId).casePredicate())
        .findFirst()
        .orElse(null);
  }

  public List<Case> getCases(CaseFilter baseFilter) {
    if (cacheUpdatedCases == null) {
      throw new IllegalStateException("Cases have not been loaded yet");
    }
    if (baseFilter != null) {
      return cacheUpdatedCases.stream().filter(baseFilter.casePredicate()).toList();
    } else {
      return cacheUpdatedCases;
    }
  }

  public Stream<Case> getCaseStream(Collection<CaseFilter> filters) {
    return filterCases(cacheUpdatedCases, filters);
  }

  public List<Case> getCasesByIds(Set<String> caseIds) {
    return cacheUpdatedCases.stream()
        .filter(kase -> caseIds.contains(kase.getId()))
        .collect(Collectors.toList());
  }

  public Map<Long, Assay> getAssaysById() {
    return caseData.getAssaysById();
  }

  public TableData<Case> getCases(int pageSize, int pageNumber, CaseSort sort, boolean descending,
      CaseFilter baseFilter, Collection<CaseFilter> filters) {
    List<Case> baseCases = getCases(baseFilter);
    Stream<Case> stream = filterCases(baseCases, filters);

    if (sort == null) {
      sort = CaseSort.LAST_ACTIVITY;
      descending = true;
    }
    Comparator<Case> comparator = sort.comparator(getAssaysById());
    stream = stream.sorted(descending ? comparator.reversed() : comparator);

    List<Case> filteredCases =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());

    TableData<Case> data = new TableData<>();
    data.setTotalCount(baseCases.size());
    data.setFilteredCount(filterCases(baseCases, filters).count());
    data.setItems(filteredCases);

    return data;
  }

  public TableData<ExternalCase> getExternalCases(int pageSize, int pageNumber, CaseSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    Set<String> userProjects = securityManager.getPrincipal().getProjects();
    List<Case> baseCases = getCases(baseFilter).stream()
        .filter(kase -> kase.getProjects().stream()
            .anyMatch(project -> userProjects.contains(project.getName())))
        .toList();
    Stream<Case> stream = filterCases(baseCases, filters);

    if (sort == null) {
      sort = CaseSort.LAST_ACTIVITY;
      descending = true;
    }
    Comparator<Case> comparator = sort.comparator(getAssaysById());
    stream = stream.sorted(descending ? comparator.reversed() : comparator);

    List<ExternalCase> filteredCases = stream.skip(pageSize * (pageNumber - 1))
        .limit(pageSize)
        .map(ExternalCase::new)
        .toList();

    TableData<ExternalCase> data = new TableData<>();
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
    Stream<String> stream = null;
    if (securityManager.getPrincipal().isInternal()) {
      stream = caseData.getRequisitionNames().stream();
    } else {
      stream = streamAuthorizedCases()
          .map(Case::getRequisition)
          .map(Requisition::getName);
    }
    return stream.filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
        .collect(Collectors.toSet());
  }

  private Stream<Case> streamAuthorizedCases() {
    Stream<Case> stream = caseData.getCases().stream();
    DimsumPrincipal principal = securityManager.getPrincipal();
    if (!principal.isInternal()) {
      Set<String> userProjects = principal.getProjects();
      stream = stream.filter(kase -> kase.getProjects().stream()
          .map(Project::getName)
          .anyMatch(userProjects::contains));
    }
    return stream;
  }

  public Set<String> getMatchingProjectNames(String prefix) {
    Stream<String> stream = null;
    DimsumPrincipal principal = securityManager.getPrincipal();
    if (principal.isInternal()) {
      stream = caseData.getProjectNames().stream();
    } else {
      stream = principal.getProjects().stream();
    }

    return stream
        .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
        .collect(Collectors.toSet());
  }

  public Set<String> getMatchingDonorNames(String prefix) {
    Stream<String> stream = null;
    if (securityManager.getPrincipal().isInternal()) {
      stream = caseData.getDonorNames().stream();
    } else {
      stream = streamAuthorizedCases().map(Case::getDonor).map(Donor::getName);
    }
    return stream
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

  public List<Sample> getLibraryQualifications(CaseFilter baseFilter,
      Collection<CaseFilter> filters) {
    return filterSamples(getCases(baseFilter), filters, MetricCategory.LIBRARY_QUALIFICATION)
        .distinct()
        .toList();
  }

  public TableData<Sample> getLibraryQualifications(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        MetricCategory.LIBRARY_QUALIFICATION);
  }

  public List<Sample> getFullDepthSequencings(CaseFilter baseFilter,
      Collection<CaseFilter> filters) {
    return filterSamples(getCases(baseFilter), filters, MetricCategory.FULL_DEPTH_SEQUENCING)
        .distinct()
        .toList();
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

  public TableData<ProjectSummaryRow> getProjectSummaryRows(String projectName,
      Collection<CaseFilter> filters, LocalDate afterDate, LocalDate beforeDate) {
    ProjectSummary projectSummary;
    TableData<ProjectSummaryRow> data = new TableData<>();
    if (filters == null && afterDate == null && beforeDate == null) {
      projectSummary = caseData.getProjectSummariesByName().get(projectName);
    } else if (filters == null) {
      // only when date filters are applied
      Map<String, ProjectSummary> projectSummariesByName =
          CaseLoader.calculateProjectSummaries(cacheUpdatedCases, afterDate, beforeDate);
      projectSummary = projectSummariesByName.get(projectName);
    } else {
      // when both date filter and case filters applied
      Map<Case, List<Test>> testsByCase = getFilteredCaseAndTest(cacheUpdatedCases, filters);
      Map<String, ProjectSummary> projectSummariesByName =
          CaseLoader.calculateFilteredProjectSummaries(testsByCase, afterDate, beforeDate);
      projectSummary = projectSummariesByName.get(projectName);
    }

    if (projectSummary == null) {
      return data;
    }

    ProjectSummaryRow completed = getCompletedProjectSummaryRow(projectSummary);
    if (afterDate == null && beforeDate == null) {
      ProjectSummaryRow pendingWork = getPendingProjectSummaryRow(projectSummary);
      ProjectSummaryRow pendingQc = getPendingQcProjectSummaryRow(projectSummary);
      data.setItems(Arrays.asList(pendingWork, pendingQc, completed));
    } else {
      data.setItems(Arrays.asList(completed));
    }

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
        .flatMap(kase -> kase.getTests().stream()
            .map(test -> new TestTableView(kase, test)));
    if (filters != null && !filters.isEmpty()) {
      Map<CaseFilterKey, Predicate<TestTableView>> filterMap =
          buildFilterMap(filters, CaseFilter::testTableViewPredicate);
      for (Predicate<TestTableView> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
  }

  private Map<Case, List<Test>> getFilteredCaseAndTest(List<Case> cases,
      Collection<CaseFilter> filters) {
    if (filters == null) {
      throw new NullPointerException("Filters cannot be null");
    } else if (filters.isEmpty()) {
      throw new IllegalStateException("Filters cannot be empty");
    }
    Map<Case, List<Test>> testsByCase = new HashMap<>();
    List<Case> filteredCases = filterCases(cases, filters).toList();
    Map<CaseFilterKey, Predicate<Test>> filterMap =
        buildFilterMap(filters, CaseFilter::testPredicate);
    for (Predicate<Test> predicate : filterMap.values()) {
      for (Case kase : filteredCases) {
        List<Test> tests = testsByCase.getOrDefault(kase, kase.getTests());
        testsByCase.put(kase, tests.stream().filter(predicate).toList());
      }
    }
    return testsByCase;
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

  public List<Sample> getLibraryQualificationsForRun(String runName,
      Collection<CaseFilter> filters) {
    Set<Sample> samples = getRunLibraries(runName, RunAndLibraries::getLibraryQualifications);
    return filterRunLibraries(samples, filters, MetricCategory.LIBRARY_QUALIFICATION);
  }

  public TableData<Sample> getLibraryQualificationsForRun(String runName, int pageSize,
      int pageNumber, SampleSort sort, boolean descending, Collection<CaseFilter> filters) {
    return getRunLibraries(runName, pageSize, pageNumber, sort, descending, filters,
        RunAndLibraries::getLibraryQualifications, MetricCategory.LIBRARY_QUALIFICATION);
  }

  public List<Sample> getFullDepthSequencingsForRun(String runName,
      Collection<CaseFilter> filters) {
    Set<Sample> samples = getRunLibraries(runName, RunAndLibraries::getFullDepthSequencings);
    return filterRunLibraries(samples, filters, MetricCategory.FULL_DEPTH_SEQUENCING);
  }

  public TableData<Sample> getFullDepthSequencingsForRun(String runName, int pageSize,
      int pageNumber, SampleSort sort, boolean descending, Collection<CaseFilter> filters) {
    return getRunLibraries(runName, pageSize, pageNumber, sort, descending, filters,
        RunAndLibraries::getFullDepthSequencings, MetricCategory.FULL_DEPTH_SEQUENCING);
  }

  public TableData<OmittedRunSample> getOmittedRunSamplesForRun(String runName, int pageSize,
      int pageNumber, OmittedRunSampleSort sort, boolean descending) {
    Set<OmittedRunSample> samples = caseData.getOmittedRunSamples().stream()
        .filter(x -> Objects.equals(x.getRunName(), runName))
        .collect(Collectors.toSet());

    return filterOmittedRunSamples(samples, pageSize, pageNumber, sort, descending);
  }

  public TableData<OmittedRunSample> getOmittedRunSamplesForProject(String projectName,
      MetricCategory sequencingType, int pageSize, int pageNumber, OmittedRunSampleSort sort,
      boolean descending) {
    Set<OmittedRunSample> samples = caseData.getOmittedRunSamples().stream()
        .filter(x -> Objects.equals(x.getProject(), projectName)
            && x.getSequencingType() == sequencingType)
        .collect(Collectors.toSet());

    return filterOmittedRunSamples(samples, pageSize, pageNumber, sort, descending);
  }

  private TableData<OmittedRunSample> filterOmittedRunSamples(Set<OmittedRunSample> samples,
      int pageSize, int pageNumber, OmittedRunSampleSort sort, boolean descending) {
    TableData<OmittedRunSample> data = new TableData<>();
    data.setTotalCount(samples.size());
    data.setFilteredCount(samples.size());
    data.setItems(samples.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1))
        .limit(pageSize)
        .toList());
    return data;
  }

  private TableData<Sample> getRunLibraries(String runName, int pageSize,
      int pageNumber, SampleSort sort, boolean descending, Collection<CaseFilter> filters,
      Function<RunAndLibraries, Set<Sample>> getSamples, MetricCategory requestCategory) {
    Set<Sample> samples = getRunLibraries(runName, getSamples);
    List<Sample> filteredSamples = filterRunLibraries(samples, filters, requestCategory);

    TableData<Sample> data = new TableData<>();
    data.setTotalCount(samples.size());
    data.setFilteredCount(filteredSamples.size());
    data.setItems(filteredSamples.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1))
        .limit(pageSize)
        .toList());
    return data;
  }

  private Set<Sample> getRunLibraries(String runName,
      Function<RunAndLibraries, Set<Sample>> getSamples) {
    RunAndLibraries runAndLibraries = caseData.getRunAndLibraries(runName);
    Set<Sample> samples = runAndLibraries == null ? Collections.emptySet()
        : getSamples.apply(runAndLibraries);
    return samples;
  }

  private List<Sample> filterRunLibraries(Collection<Sample> samples,
      Collection<CaseFilter> filters, MetricCategory requestCategory) {
    Stream<Sample> stream = samples.stream();

    if (filters != null && !filters.isEmpty()) {
      Map<CaseFilterKey, Predicate<Sample>> filterMap =
          buildFilterMap(filters, filter -> filter.samplePredicate(requestCategory));
      for (Predicate<Sample> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream.toList();
  }

  private ProjectSummaryRow getCompletedProjectSummaryRow(ProjectSummary projectSummary) {
    return new ProjectSummaryRow.Builder()
        .title("Completed")
        .receipt(
            new ProjectSummaryField(projectSummary.getReceiptCompletedCount(),
                CaseFilterKey.COMPLETED.name(),
                CompletedGate.RECEIPT.getLabel()))
        .extraction(
            new ProjectSummaryField(projectSummary.getExtractionCompletedCount(),
                CaseFilterKey.COMPLETED.name(),
                CompletedGate.EXTRACTION.getLabel()))
        .libraryPreparation(
            new ProjectSummaryField(projectSummary.getLibraryPrepCompletedCount(),
                CaseFilterKey.COMPLETED.name(), CompletedGate.LIBRARY_PREPARATION.getLabel()))
        .libraryQualification(
            new ProjectSummaryField(projectSummary.getLibraryQualCompletedCount(),
                CaseFilterKey.COMPLETED.name(), CompletedGate.LIBRARY_QUALIFICATION.getLabel()))
        .fullDepthSequencing(
            new ProjectSummaryField(projectSummary.getFullDepthSeqCompletedCount(),
                CaseFilterKey.COMPLETED.name(), CompletedGate.FULL_DEPTH_SEQUENCING.getLabel()))
        .analysisReview(
            new ProjectSummaryField(projectSummary.getAnalysisReviewCompletedCount(),
                CaseFilterKey.COMPLETED.name(), CompletedGate.ANALYSIS_REVIEW.getLabel()))
        .releaseApproval(
            new ProjectSummaryField(projectSummary.getReleaseApprovalCompletedCount(),
                CaseFilterKey.COMPLETED.name(), CompletedGate.RELEASE_APPROVAL.getLabel()))
        .release(
            new ProjectSummaryField(projectSummary.getReleaseCompletedCount(),
                CaseFilterKey.COMPLETED.name(), CompletedGate.RELEASE.getLabel()))
        .build();
  }

  private ProjectSummaryRow getPendingProjectSummaryRow(ProjectSummary projectSummary) {
    return new ProjectSummaryRow.Builder()
        .title("Pending Work")
        .extraction(
            new ProjectSummaryField(projectSummary.getExtractionPendingCount(),
                CaseFilterKey.PENDING.name(), PendingState.EXTRACTION.getLabel()))
        .libraryPreparation(
            new ProjectSummaryField(projectSummary.getLibraryPrepPendingCount(),
                CaseFilterKey.PENDING.name(), PendingState.LIBRARY_PREPARATION.getLabel()))
        .libraryQualification(
            new ProjectSummaryField(projectSummary.getLibraryQualPendingCount(),
                CaseFilterKey.PENDING.name(), PendingState.LIBRARY_QUALIFICATION.getLabel()))
        .fullDepthSequencing(
            new ProjectSummaryField(projectSummary.getFullDepthSeqPendingCount(),
                CaseFilterKey.PENDING.name(), PendingState.FULL_DEPTH_SEQUENCING.getLabel()))
        .analysisReview(
            new ProjectSummaryField(projectSummary.getAnalysisReviewPendingCount(),
                CaseFilterKey.PENDING.name(), PendingState.ANALYSIS_REVIEW.getLabel()))
        .releaseApproval(
            new ProjectSummaryField(projectSummary.getReleaseApprovalPendingCount(),
                CaseFilterKey.PENDING.name(), PendingState.RELEASE_APPROVAL.getLabel()))
        .release(
            new ProjectSummaryField(projectSummary.getReleasePendingCount(),
                CaseFilterKey.PENDING.name(), PendingState.RELEASE.getLabel()))
        .build();
  }

  private ProjectSummaryRow getPendingQcProjectSummaryRow(ProjectSummary projectSummary) {
    return new ProjectSummaryRow.Builder()
        .title("Pending QC")
        .receipt(
            new ProjectSummaryField(projectSummary.getReceiptPendingQcCount(),
                CaseFilterKey.PENDING.name(), PendingState.RECEIPT_QC.getLabel()))
        .extraction(
            new ProjectSummaryField(projectSummary.getExtractionPendingQcCount(),
                CaseFilterKey.PENDING.name(), PendingState.EXTRACTION_QC.getLabel()))
        .libraryPreparation(
            new ProjectSummaryField(projectSummary.getLibraryPrepPendingQcCount(),
                CaseFilterKey.PENDING.name(), PendingState.LIBRARY_QC.getLabel()))
        .libraryQualification(
            new ProjectSummaryField(projectSummary.getLibraryQualPendingQcCount(),
                CaseFilterKey.PENDING.name(), PendingState.LIBRARY_QUALIFICATION_QC.getLabel()))
        .fullDepthSequencing(
            new ProjectSummaryField(projectSummary.getFullDepthSeqPendingQcCount(),
                CaseFilterKey.PENDING.name(), PendingState.FULL_DEPTH_QC.getLabel()))
        .build();
  }

  @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
  private void refreshData() {
    try {
      ZonedDateTime previousTimestamp = caseData == null ? null : caseData.getTimestamp();
      CaseData newData = dataLoader.load(previousTimestamp);
      refreshFailures = 0;
      if (newData != null) {
        setCaseData(newData);
        updateFrontEndConfig();
        notificationManager.update(newData.getRunsAndLibrariesByName(), newData.getAssaysById());
      }
    } catch (Exception e) {
      refreshFailures++;
      log.error("Failed to refresh case data", e);
    }
  }

  private void updateFrontEndConfig() {
    frontEndConfig.setPipelines(cacheUpdatedCases.stream()
        .flatMap(kase -> kase.getProjects().stream())
        .map(Project::getPipeline)
        .collect(Collectors.toSet()));
    frontEndConfig.setAssaysById(caseData.getAssaysById());
    // Library preparation must always match the test design code
    // Library qualification must match the library qualificiation design code if set, else above
    // Full-depth must match one of the above items
    frontEndConfig.setLibraryDesigns(cacheUpdatedCases.stream()
        .flatMap(kase -> kase.getTests().stream())
        .flatMap(test -> Stream.concat(Stream.of(test.getLibraryDesignCode()),
            test.getLibraryQualifications().stream().map(Sample::getLibraryDesignCode)))
        .collect(Collectors.toSet()));
    frontEndConfig.setDeliverableCategories(caseData.getCases().stream()
        .flatMap(kase -> kase.getDeliverables().stream())
        .map(CaseDeliverable::getDeliverableCategory)
        .collect(Collectors.toSet()));
    frontEndConfig.setDeliverables(caseData.getCases().stream()
        .flatMap(kase -> kase.getDeliverables().stream())
        .flatMap(deliverable -> deliverable.getReleases().stream())
        .map(CaseRelease::getDeliverable)
        .collect(Collectors.toSet()));
  }

  public void cacheSignoffs(Collection<NabuSavedSignoff> signoffs) {
    synchronized (cachedSignoffsByCaseId) {
      for (NabuSavedSignoff signoff : signoffs) {
        List<NabuSavedSignoff> cachedSignoffs =
            cachedSignoffsByCaseId.get(signoff.getCaseIdentifier());
        if (cachedSignoffs == null) {
          cachedSignoffs = new ArrayList<>();
        }
        cachedSignoffs.add(signoff);
        cachedSignoffsByCaseId.put(signoff.getCaseIdentifier(), cachedSignoffs);
      }
      refreshCacheUpdatedCases();
    }
  }

  private void refreshCacheUpdatedCases() {
    synchronized (cachedSignoffsByCaseId) {
      removeExpiredCachedSignoffs();

      cacheUpdatedCases = caseData.getCases().stream()
          .map(kase -> cachedSignoffsByCaseId.keySet().contains(kase.getId())
              ? makeCacheUpdatedCase(kase, cachedSignoffsByCaseId.get(kase.getId()))
              : kase)
          .collect(
              Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
  }

  private void removeExpiredCachedSignoffs() {
    ZonedDateTime cutoff = caseData.getTimestamp().minus(CACHE_OVERLAP_MINUTES, ChronoUnit.MINUTES);
    Iterator<String> iterator = cachedSignoffsByCaseId.keySet().iterator();
    while (iterator.hasNext()) {
      String caseId = iterator.next();
      List<NabuSavedSignoff> signoffs = cachedSignoffsByCaseId.get(caseId);
      signoffs.removeIf(signoff -> signoff.getCreated().isBefore(cutoff));
      if (signoffs.isEmpty()) {
        iterator.remove();
      }
    }
  }

  private Case makeCacheUpdatedCase(Case kase, Collection<NabuSavedSignoff> signoffs) {
    Case result = kase;
    for (NabuSavedSignoff signoff : signoffs) {
      result = new CacheUpdatedCase(result, signoff);
    }
    return result;
  }

}
