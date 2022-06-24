package ca.on.oicr.gsi.dimsum.service;

import ca.on.oicr.gsi.dimsum.CaseLoader;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.CaseData;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CaseService {

  private static final Logger log = LoggerFactory.getLogger(CaseService.class);

  @Autowired
  private CaseLoader dataLoader;

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
    Stream<Case> stream = allCases.stream();

    if (filters != null) {
      for (CaseFilter filter : filters) {
        stream = stream.filter(filter.predicate());
      }
    }
    if (sort == null) {
      sort = CaseSort.LAST_ACTIVITY;
    }
    stream = stream.sorted(descending ? sort.comparator().reversed() : sort.comparator());

    List<Case> filteredCases =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());

    TableData<Case> data = new TableData<>();
    data.setTotalCount(allCases.size());
    data.setFilteredCount(filteredCases.size());
    data.setItems(filteredCases);
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
      }
    } catch (Exception e) {
      refreshFailures++;
      log.error("Failed to refresh case data", e);
    }
  }

}
