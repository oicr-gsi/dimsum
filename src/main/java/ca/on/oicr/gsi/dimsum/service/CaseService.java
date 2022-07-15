package ca.on.oicr.gsi.dimsum.service;

import ca.on.oicr.gsi.dimsum.CaseLoader;
import ca.on.oicr.gsi.dimsum.FrontEndConfig;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.CaseData;
import ca.on.oicr.gsi.dimsum.data.Project;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.RequisitionSort;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CaseService {

  private static final Logger log = LoggerFactory.getLogger(CaseService.class);

  @Autowired
  private CaseLoader dataLoader;

  @Autowired
  private FrontEndConfig frontEndConfig;

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
    if (caseData == null) {
      return Duration.ZERO;
    }
    return Duration.between(caseData.getTimestamp(), ZonedDateTime.now());
  }

  public List<Case> getCases() {
    if (caseData == null) {
      throw new IllegalStateException("Cases have not been loaded yet");
    }
    return caseData.getCases();
  }

  public TableData<Case> getCases(int pageSize, int pageNumber, CaseSort sort, boolean descending,
      Collection<CaseFilter> filters) {
    List<Case> allCases = getCases();
    Stream<Case> stream = applyFilters(allCases, filters);

    if (sort == null) {
      sort = CaseSort.LAST_ACTIVITY;
      descending = true;
    }
    stream = stream.sorted(descending ? sort.comparator().reversed() : sort.comparator());

    List<Case> filteredCases =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());

    TableData<Case> data = new TableData<>();
    data.setTotalCount(allCases.size());
    data.setFilteredCount(applyFilters(allCases, filters).count());
    data.setItems(filteredCases);
    return data;
  }

  public TableData<Sample> getReceipts(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, filters,
        (kase) -> kase.getReceipts().stream());
  }

  public TableData<Sample> getExtractions(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, filters,
        getTestSampleStream(Test::getExtractions));
  }

  public TableData<Sample> getLibraryPreparations(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, filters,
        getTestSampleStream(Test::getLibraryPreparations));
  }

  public TableData<Sample> getLibraryQualifications(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, filters,
        getTestSampleStream(Test::getLibraryQualifications));
  }

  public TableData<Sample> getFullDepthSequencings(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, Collection<CaseFilter> filters) {
    return getSamples(pageSize, pageNumber, sort, descending, filters,
        getTestSampleStream(Test::getFullDepthSequencings));
  }

  private Function<Case, Stream<Sample>> getTestSampleStream(
      Function<Test, List<Sample>> getTestSamples) {
    return kase -> kase.getTests().stream().flatMap(test -> getTestSamples.apply(test).stream());
  }

  private TableData<Sample> getSamples(int pageSize, int pageNumber, SampleSort sort,
      boolean descending, Collection<CaseFilter> filters,
      Function<Case, Stream<Sample>> getSampleStream) {
    long sampleCount =
        applyFilters(getCases(), filters).flatMap(getSampleStream).distinct().count();
    List<Sample> filteredSamples = applyFilters(getCases(), filters).flatMap(getSampleStream)
        .distinct().sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1)).limit(pageSize).toList();

    TableData<Sample> data = new TableData<>();
    data.setTotalCount(sampleCount);
    data.setFilteredCount(sampleCount);
    data.setItems(filteredSamples);
    return data;
  }

  public TableData<Requisition> getRequisitions(int pageSize, int pageNumber, RequisitionSort sort,
      boolean descending, Collection<CaseFilter> filters) {
    long requisitionCount = applyFilters(getCases(), filters)
        .flatMap(kase -> kase.getRequisitions().stream()).distinct().count();
    List<Requisition> filteredRequisitions =
        applyFilters(getCases(), filters).flatMap(kase -> kase.getRequisitions().stream())
            .distinct().sorted(descending ? sort.comparator().reversed() : sort.comparator())
            .skip(pageSize * (pageNumber - 1)).limit(pageSize).toList();

    TableData<Requisition> data = new TableData<>();
    data.setTotalCount(requisitionCount);
    data.setFilteredCount(requisitionCount);
    data.setItems(filteredRequisitions);
    return data;
  }

  private Stream<Case> applyFilters(List<Case> cases, Collection<CaseFilter> filters) {
    Stream<Case> stream = cases.stream();
    if (filters != null) {
      for (CaseFilter filter : filters) {
        stream = stream.filter(filter.predicate());
      }
    }
    return stream;
  }

  @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
  private void refreshData() {
    try {
      ZonedDateTime previousTimestamp = caseData == null ? null : caseData.getTimestamp();
      CaseData newData = dataLoader.load(previousTimestamp);
      refreshFailures = 0;
      if (newData != null) {
        caseData = newData;
        frontEndConfig
            .setPipelines(newData.getCases().stream().flatMap(kase -> kase.getProjects().stream())
                .map(Project::getPipeline).collect(Collectors.toSet()));
      }
    } catch (Exception e) {
      refreshFailures++;
      log.error("Failed to refresh case data", e);
    }
  }

}
