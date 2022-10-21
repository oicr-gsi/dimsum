package ca.on.oicr.gsi.dimsum.service;

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
import ca.on.oicr.gsi.dimsum.data.Project;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.RequisitionSort;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class CaseService {

  private static final Logger log = LoggerFactory.getLogger(CaseService.class);

  @Autowired
  private CaseLoader dataLoader;

  @Autowired
  private FrontEndConfig frontEndConfig;

  @Autowired
  private NotificationManager notificationManager;

  @Autowired
  private RunListManager runListManager;

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
      return currentData.getCases().stream().filter(baseFilter.predicate()).toList();
    } else {
      return currentData.getCases();
    }
  }

  public TableData<Case> getCases(int pageSize, int pageNumber, CaseSort sort, boolean descending,
      CaseFilter baseFilter, Collection<CaseFilter> filters) {
    List<Case> baseCases = getCases(baseFilter);
    Stream<Case> stream = applyFilters(baseCases, filters);

    if (sort == null) {
      sort = CaseSort.LAST_ACTIVITY;
      descending = true;
    }
    stream = stream.sorted(descending ? sort.comparator().reversed() : sort.comparator());

    List<Case> filteredCases =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());

    TableData<Case> data = new TableData<>();
    data.setTotalCount(baseCases.size());
    data.setFilteredCount(applyFilters(baseCases, filters).count());
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

  public TableData<Sample> getReceipts(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        (kase) -> kase.getReceipts().stream());
  }

  public TableData<Sample> getExtractions(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        getTestSampleStream(Test::getExtractions));
  }

  public TableData<Sample> getLibraryPreparations(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        getTestSampleStream(Test::getLibraryPreparations));
  }

  public TableData<Sample> getLibraryQualifications(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        getTestSampleStream(Test::getLibraryQualifications));
  }

  public TableData<Sample> getFullDepthSequencings(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, baseFilter, filters,
        getTestSampleStream(Test::getFullDepthSequencings));
  }

  private Function<Case, Stream<Sample>> getTestSampleStream(
      Function<Test, List<Sample>> getTestSamples) {
    return kase -> kase.getTests().stream().flatMap(test -> getTestSamples.apply(test).stream());
  }

  private TableData<Sample> getSamples(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters,
      Function<Case, Stream<Sample>> getSampleStream) {
    List<Case> cases = getCases(baseFilter);
    TableData<Sample> data = new TableData<>();
    data.setTotalCount(cases.stream().flatMap(getSampleStream).distinct().count());
    data.setFilteredCount(applyFilters(cases, filters).flatMap(getSampleStream).distinct().count());
    data.setItems(applyFilters(cases, filters).flatMap(getSampleStream).distinct()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1)).limit(pageSize).toList());
    return data;
  }

  public TableData<Requisition> getRequisitions(int pageSize, int pageNumber, RequisitionSort sort,
      boolean descending, CaseFilter baseFilter, Collection<CaseFilter> filters) {
    List<Case> cases = getCases(baseFilter);
    TableData<Requisition> data = new TableData<>();
    data.setTotalCount(
        cases.stream().flatMap(kase -> kase.getRequisitions().stream()).distinct().count());
    data.setFilteredCount(applyFilters(cases, filters)
        .flatMap(kase -> kase.getRequisitions().stream()).distinct().count());
    data.setItems(applyFilters(cases, filters).flatMap(kase -> kase.getRequisitions().stream())
        .distinct().sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1)).limit(pageSize).toList());
    return data;
  }

  private Stream<Case> applyFilters(List<Case> cases, Collection<CaseFilter> filters) {
    Stream<Case> stream = cases.stream();
    if (filters != null && !filters.isEmpty()) {
      Map<CaseFilterKey, Predicate<Case>> filterMap = new HashMap<>();
      for (CaseFilter filter : filters) {
        CaseFilterKey key = filter.getKey();
        if (filterMap.containsKey(key)) {
          filterMap.put(key, filterMap.get(key).or(filter.predicate()));
        } else {
          filterMap.put(key, filter.predicate());
        }
      }
      for (Predicate<Case> predicate : filterMap.values()) {
        stream = stream.filter(predicate);
      }
    }
    return stream;
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
        runListManager.update(newData.getRunsAndLibrariesByName());
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
