package ca.on.oicr.gsi.dimsum.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.management.Notification;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;
import ca.on.oicr.gsi.dimsum.service.filtering.RunSort;
import org.slf4j.Logger;

@Service
public class RunListManager {
  private static final Logger log = LoggerFactory.getLogger(RunListManager.class);

  private List<Run> runs = new ArrayList<>();

  private void update(Map<String, RunAndLibraries> data) {
    List<Run> newRuns = new ArrayList<>();
    for (Run run : runs) {
      String runName = run.getName();
      RunAndLibraries runAndLibraries = data.get(runName);
      if (runAndLibraries == null) {
        log.warn("Orphaned run - run not found: {}", runName);
        continue;
      }
      if (needsQc.test(runAndLibraries)) {
        log.debug("Maintaining run {}", runName);
      } else {
        log.debug("Clearing run {}", runName);
      }
    }
    List<Long> processedRunIds = runs.stream().map(x -> x.getId()).toList();
    data.values().stream().filter(needsQc)
        .filter(x -> !processedRunIds.containerModel(x.getRun().getId())).forEach(x -> {
          log.debug("Creating run {}", x.getRun().getName());
        });
    this.runs = newRuns;
  }

  public TableData<Run> getRuns(int pageSize, int pageNumber, RunSort sort, boolean descending) {

  }

}
