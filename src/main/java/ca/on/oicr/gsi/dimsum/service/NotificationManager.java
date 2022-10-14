package ca.on.oicr.gsi.dimsum.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ca.on.oicr.gsi.dimsum.data.Assay;
import ca.on.oicr.gsi.dimsum.data.Metric;
import ca.on.oicr.gsi.dimsum.data.MetricCategory;
import ca.on.oicr.gsi.dimsum.data.MetricSubcategory;
import ca.on.oicr.gsi.dimsum.data.Notification;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.service.filtering.NotificationSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@Service
public class NotificationManager {

  private static final Logger log = LoggerFactory.getLogger(NotificationManager.class);

  private List<Notification> notifications = new ArrayList<>();

  public void update(Map<String, RunAndLibraries> data, Map<Long, Assay> assaysById) {
    List<Notification> newNotifications = updateExistingNotifications(data, assaysById);
    newNotifications.addAll(createNewNotifications(data, assaysById));
    notifications = newNotifications;
  }

  private List<Notification> updateExistingNotifications(Map<String, RunAndLibraries> data,
      Map<Long, Assay> assaysById) {
    List<Notification> newNotifications = new ArrayList<>();
    for (Notification notification : notifications) {
      String runName = notification.getRun().getName();
      RunAndLibraries runAndLibraries = data.get(runName);
      if (runAndLibraries == null) {
        log.warn("Orphaned notification - run not found: {}", runName);
        continue;
      }
      Notification newNotification =
          makeNotification(runAndLibraries, notification.getMetricCategory(), assaysById);
      if (newNotification == null) {
        // QC is complete
        log.debug("Clearing notification for run {}", runName);
      } else {
        newNotifications.add(newNotification);
        if (newNotification.getPendingQcCount() > 0
            || newNotification.getPendingDataReviewCount() > 0) {
          // still pending QC
          log.debug("Updating/maintaining notification for run {}", runName);
        } else if (newNotification.getPendingAnalysisCount() > 0) {
          // no pending QC, but still has samples pending analysis
          log.debug("Pausing/maintaining notification for run {}", runName);
        } else {
          // all run-library QC is complete, but run is missing QC
          log.debug("Updating/maintaining notification for run {} - Run QC needed", runName);
        }
      }
    }
    return newNotifications;
  }

  private List<Notification> createNewNotifications(Map<String, RunAndLibraries> data,
      Map<Long, Assay> assaysById) {
    List<Notification> newNotifications = new ArrayList<>();

    // Library Qualification: create notification if
    // 1. run has library qualifications
    // 2. there is not already a lib.qual. notification for this run
    // 3. ALL library qualifications have completed analysis
    // 4. any library qualification or the run itself are pending QC or data review
    data.values().stream()
        .filter(x -> !x.getLibraryQualifications().isEmpty()
            && notifications.stream()
                .noneMatch(existing -> existing.getRun().getId() == x.getRun().getId()
                    && existing.getMetricCategory() == MetricCategory.LIBRARY_QUALIFICATION)
            && x.getLibraryQualifications().stream()
                .noneMatch(sample -> !metricsAvailable(sample, x.getRun(), assaysById,
                    MetricCategory.LIBRARY_QUALIFICATION)))
        .map(x -> makeNotification(x, MetricCategory.LIBRARY_QUALIFICATION, assaysById))
        .filter(Objects::nonNull)
        .forEach(x -> {
          log.debug("Creating notification for run {} (library qualification)",
              x.getRun().getName());
          newNotifications.add(x);
        });
    // Full Depth: create notification if
    // 1. run has full depth sequencings
    // 2. there is not already a full depth notification for this run
    // 3. any full depth library has completed analysis
    // 4. any full depth library or the run itself are pending QC or data review
    data.values().stream()
        .filter(x -> !x.getFullDepthSequencings().isEmpty()
            && notifications.stream()
                .noneMatch(existing -> existing.getRun().getId() == x.getRun().getId()
                    && existing.getMetricCategory() == MetricCategory.FULL_DEPTH_SEQUENCING)
            && x.getFullDepthSequencings().stream()
                .anyMatch(sample -> metricsAvailable(sample, x.getRun(), assaysById,
                    MetricCategory.FULL_DEPTH_SEQUENCING)))
        .map(x -> makeNotification(x, MetricCategory.FULL_DEPTH_SEQUENCING, assaysById))
        .filter(x -> Objects.nonNull(x)
            && (x.getPendingQcCount() > 0 || x.getPendingDataReviewCount() > 0
                || x.getRun().getDataReviewDate() == null))
        .forEach(x -> {
          log.debug("Creating notification for run {} (full depth)", x.getRun().getName());
          newNotifications.add(x);
        });

    return newNotifications;
  }

  private Notification makeNotification(RunAndLibraries runAndLibraries, MetricCategory category,
      Map<Long, Assay> assaysById) {
    Set<Sample> samples = getSamples(runAndLibraries, category).stream()
        .filter(sample -> sample.getDataReviewDate() == null)
        .collect(Collectors.toSet());
    Run run = runAndLibraries.getRun();
    Set<Sample> pendingAnalysis = new HashSet<>();
    Set<Sample> pendingQc = new HashSet<>();
    Set<Sample> pendingDataReview = new HashSet<>();
    for (Sample sample : samples) {
      if (sample.getDataReviewDate() != null) {
        continue;
      } else if (!metricsAvailable(sample, run, assaysById, category)) {
        pendingAnalysis.add(sample);
      } else if (sample.getQcDate() == null) {
        pendingQc.add(sample);
      } else {
        pendingDataReview.add(sample);
      }
    }
    if (pendingAnalysis.isEmpty() && pendingQc.isEmpty() && pendingDataReview.isEmpty()
        && run.getDataReviewDate() != null) {
      return null;
    }
    return new Notification(run, category, pendingAnalysis, pendingQc, pendingDataReview);
  }

  private Set<Sample> getSamples(RunAndLibraries runAndLibraries, MetricCategory category) {
    switch (category) {
      case LIBRARY_QUALIFICATION:
        return runAndLibraries.getLibraryQualifications();
      case FULL_DEPTH_SEQUENCING:
        return runAndLibraries.getFullDepthSequencings();
      default:
        throw new IllegalStateException(
            String.format("Invalid notification category: %s", category));
    }
  }

  public TableData<Notification> getNotifications(int pageSize, int pageNumber,
      NotificationSort sort,
      boolean descending) {
    List<Notification> currentNotifications = notifications;
    List<Notification> includedNotifications = currentNotifications.stream()
        .sorted(descending ? sort.comparator().reversed() : sort.comparator())
        .skip(pageSize * (pageNumber - 1))
        .limit(pageSize)
        .toList();
    TableData<Notification> data = new TableData<>();
    data.setTotalCount(currentNotifications.size());
    data.setFilteredCount(currentNotifications.size());
    data.setItems(includedNotifications);
    return data;
  }

  private boolean metricsAvailable(Sample sample, Run run, Map<Long, Assay> assaysById,
      MetricCategory metricCategory) {
    if (metricCategory != MetricCategory.LIBRARY_QUALIFICATION
        && metricCategory != MetricCategory.FULL_DEPTH_SEQUENCING) {
      throw new IllegalArgumentException(
          String.format("Unexpected metric category: %s", metricCategory));
    }
    if (sample.getAssayId() == null) {
      return false;
    }
    Assay assay = assaysById.get(sample.getAssayId());
    List<MetricSubcategory> subcategories = assay.getMetricCategories().get(metricCategory);
    for (MetricSubcategory subcategory : subcategories) {
      if (subcategory.getLibraryDesignCode() != null
          && !subcategory.getLibraryDesignCode().equals(sample.getLibraryDesignCode())) {
        continue;
      }
      for (Metric metric : subcategory.getMetrics()) {
        if (filter(metric.getNucleicAcidType(), sample.getNucleicAcidType())
            || filter(metric.getTissueMaterial(), sample.getTissueMaterial())
            || filter(metric.getTissueOrigin(), sample.getTissueOrigin())
            || (!metric.isNegateTissueType()
                && filter(metric.getTissueType(), sample.getTissueType()))
            || (metric.isNegateTissueType() && metric.getTissueType() != null
                && metric.getTissueType().equals(sample.getTissueType()))
            || filter(metric.getContainerModel(), run.getContainerModel())
            || filter(metric.getReadLength(), run.getReadLength())
            || filter(metric.getReadLength2(), run.getReadLength2())) {
          continue;
        }
        if (metricValueMissing(sample, metric)) {
          return false;
        }
      }
    }
    return true;
  }

  private <T> boolean filter(T criteria, T value) {
    return criteria != null && !criteria.equals(value);
  }

  private boolean metricValueMissing(Sample sample, Metric metric) {
    // Only check metrics from GSI-QC-ETL here, as they should be the only ones we need to wait on
    switch (metric.getName()) {
      case "Mean Insert Size":
        return sample.getMeanInsertSize() == null;
      case "Clusters Per Sample":
      case "Pass Filter Clusters":
        return sample.getClustersPerSample() == null;
      case "Duplication Rate":
        return sample.getDuplicationRate() == null;
      case "Mapped to Coding":
        return sample.getMappedToCoding() == null;
      case "rRNA Contamination":
        return sample.getrRnaContamination() == null;
      case "Mean Coverage Deduplicated":
        return sample.getMeanCoverageDeduplicated() == null;
      case "Coverage (Raw)":
        return sample.getRawCoverage() == null;
      case "On Target Reads":
        return sample.getOnTargetReads() == null;
      default:
        return false;
    }
  }

}
