package ca.on.oicr.gsi.dimsum.service;

import ca.on.oicr.gsi.dimsum.CaseLoader;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.CaseData;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CaseService {

  private static Logger log = LoggerFactory.getLogger(CaseService.class);

  @Autowired
  private CaseLoader dataLoader;

  private CaseData caseData;

  private Timer refreshTimer;
  private int refreshFailures = 0;

  public CaseService(@Autowired MeterRegistry meterRegistry) {
    if (meterRegistry != null) {
      Gauge.builder("case_data_refresh_failures", this::getRefreshFailures)
          .description("Number of consecutive failures to refresh the case data")
          .register(meterRegistry);
      Gauge.builder("case_data_age_seconds", () -> this.getDataAge().toSeconds())
          .description("Time since case data was refreshed")
          .register(meterRegistry);
      refreshTimer = Timer.builder("case_data_refresh_time")
          .description("Time taken to refresh the case data")
          .register(meterRegistry);
    }
  }

  private int getRefreshFailures() {
    return refreshFailures;
  }

  public Duration getDataAge() {
    if (caseData == null) {
      return Duration.ZERO;
    }
    return Duration.between(caseData.getTimestamp(), LocalDateTime.now());
  }

  public List<Case> getCases() {
    if (caseData == null) {
      throw new IllegalStateException("Cases have not been loaded yet");
    }
    return caseData.getCases();
  }

  @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.MINUTES)
  private void refreshData() {
    log.debug("Checking for new data...");
    try {
      LocalDateTime fileTimestamp = dataLoader.getDataTimestamp();
      if (caseData != null && !fileTimestamp.isAfter(caseData.getTimestamp())) {
        log.debug("No new data - aborting refresh");
        return;
      }
      log.debug("New data found. Refreshing...");
      long startTimeMillis = System.currentTimeMillis();
      caseData = dataLoader.load();
      refreshTimer.record(System.currentTimeMillis() - startTimeMillis, TimeUnit.MILLISECONDS);
      refreshFailures = 0;
      log.debug(String.format("Data refreshed. %d cases loaded.", caseData.getCases().size()));
    } catch (Exception e) {
      refreshFailures++;
      log.error("Failed to refresh case data", e);
    }
  }

}
