package ca.on.oicr.gsi.dimsum.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.service.filtering.RunSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

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
        .filter(x -> !processedRunIds.contains(x.getRun().getId())).forEach(x -> {
          log.debug("Creating run {}", x.getRun().getName());
        });
    this.runs = newRuns;
  }

  public TableData<Run> getRuns(int pageSize, int pageNumber, RunSort sort, boolean descending) {
    List<Run> currentRuns = runs;
    List<Run> runs =
        currentRuns.stream()
            .sorted(descending ? sort.comparator().reversed() : sort.comparator())
            .skip(pageSize * (pageNumber - 1))
            .limit(pageSize)
            .toList();
    TableData<Run> data = new TableData<>();
    data.setTotalCount(currentRuns.size());
    data.setFilteredCount(currentRuns.size());
    data.setItems(runs);
    return data;
  }

  private Predicate<RunAndLibraries> needsQc = x -> {
    return x.getRun().getDataReviewDate() == null || anyNeedQc(x.getLibraryQualifications())
        || anyNeedQc(x.getFullDepthSequencings());
  };

  private static boolean anyNeedQc(Collection<Sample> libraries) {
    return libraries.stream().anyMatch(x -> x.getDataReviewDate() == null);
  }
}
