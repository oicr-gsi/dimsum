package ca.on.oicr.gsi.dimsum.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.service.filtering.RunFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.RunFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.RunSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@Service
public class RunListManager {
  private static final Logger log = LoggerFactory.getLogger(RunListManager.class);

  private List<Run> runs = new ArrayList<>();

  public void update(Map<String, RunAndLibraries> data) {
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
    data.values().stream()
        .filter(needsQc)
        .filter(x -> !processedRunIds.contains(x.getRun().getId()))
        .forEach(x -> {
          log.debug("Creating run {}", x.getRun().getName());
          newRuns.add(x.getRun());
        });
    this.runs = newRuns;
  }

  public TableData<Run> getRuns(int pageSize, int pageNumber, RunSort sort, boolean descending,
      RunFilter baseFilter, Collection<RunFilter> filters) {
    List<Run> baseRuns = runs;
    Stream<Run> stream = applyFilters(baseRuns, filters);
    if (sort == null) {
      sort = RunSort.COMPLETION_DATE;
      descending = true;
    }
    stream = stream.sorted(descending ? sort.comparator().reversed() : sort.comparator());
    List<Run> filteredRuns =
        stream.skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toList());
    TableData<Run> data = new TableData<>();
    data.setTotalCount(baseRuns.size());
    data.setFilteredCount(applyFilters(baseRuns, filters).count());
    data.setItems(filteredRuns);
    return data;
  }

  private Stream<Run> applyFilters(List<Run> runs, Collection<RunFilter> filters) {
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

  private Predicate<RunAndLibraries> needsQc = x -> {
    return x.getRun().getDataReviewDate() == null || anyNeedQc(x.getLibraryQualifications())
        || anyNeedQc(x.getFullDepthSequencings());
  };

  private static boolean anyNeedQc(Collection<Sample> libraries) {
    return libraries.stream().anyMatch(x -> x.getDataReviewDate() == null);
  }
}
