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
public class NotificationManager {

  private static final Logger log = LoggerFactory.getLogger(NotificationManager.class);

  private List<Run> notifications = new ArrayList<>();

  public void update(Map<String, RunAndLibraries> data) {
    List<Run> newNotifications = new ArrayList<>();
    for (Run notification : notifications) {
      RunAndLibraries runAndLibraries = data.get(notification.getName());
      if (runAndLibraries == null) {
        log.warn("Orphaned notification - run not found: {}", notification.getName());
        continue;
      }
      if (needsQc.test(runAndLibraries)) {
        log.debug("Maintaining notification for run {}", notification.getName());
        newNotifications.add(notification);
      } else {
        log.debug("Clearing notification for run {}", notification.getName());
      }
    }
    List<Long> processedRunIds = notifications.stream().map(x -> x.getId()).toList();
    data.values().stream()
        .filter(needsQc)
        .filter(x -> !processedRunIds.contains(x.getRun().getId()))
        .forEach(x -> {
          log.debug("Creating notification for run {}", x.getRun().getName());
          newNotifications.add(x.getRun());
        });
    notifications = newNotifications;
  }

  public TableData<Run> getNotifications(int pageSize, int pageNumber, RunSort sort,
      boolean descending) {
    List<Run> currentNotifications = notifications;
    List<Run> runs = currentNotifications.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1))
        .limit(pageSize)
        .toList();
    TableData<Run> data = new TableData<>();
    data.setTotalCount(currentNotifications.size());
    data.setFilteredCount(currentNotifications.size());
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
