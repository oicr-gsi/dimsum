package ca.on.oicr.gsi.dimsum;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.OmittedSample;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.RequisitionQc;
import ca.on.oicr.gsi.cardea.data.RunAndLibraries;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.data.CaseData;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;
import ca.on.oicr.gsi.dimsum.service.filtering.CompletedGate;
import ca.on.oicr.gsi.dimsum.service.filtering.DateFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.PendingState;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class CaseLoader {

  private static final Logger log = LoggerFactory.getLogger(CaseLoader.class);

  private Timer refreshTimer = null;

  @Value("${cardea.url}")
  private String cardeaUrl; // to store Cardea url that is passed in CaseLoader constructor
  private final int LIMIT_FOR_DATA_LOAD = 1073741824; // 1GB to load the data from Cardea



  public CaseLoader(@Autowired MeterRegistry meterRegistry) {
    if (meterRegistry != null) {
      refreshTimer = Timer.builder("case_data_refresh_time")
          .description("Time taken to refresh the case data").register(meterRegistry);
    }
  }

  /**
   *
   * @param previousTimestamp timestamp of previous successful load
   * @return case data if it is available and newer than the previous Timestamp; null otherwise
   */
  public CaseData load(ZonedDateTime previousTimestamp) throws IOException {
    log.debug("Loading case data...");

    WebClient.Builder builder = WebClient.builder();
    ZonedDateTime currentTimeStamp = builder.build().get().uri(cardeaUrl + "/timestamp").retrieve()
        .bodyToMono(ZonedDateTime.class).block();

    if (previousTimestamp != null && !currentTimeStamp.isAfter(previousTimestamp)) {
      log.debug("Current case data is up to date with Cardea; aborting reload.");
      return null;
    }

    ca.on.oicr.gsi.cardea.data.CaseData cardeaCaseData = loadCardeaData(builder);

    Map<Long, Assay> assaysById = cardeaCaseData.getAssaysById();
    Map<String, RunAndLibraries> runsByName = sortRuns(cardeaCaseData.getCases());
    List<OmittedSample> omittedSamples = cardeaCaseData.getOmittedSamples();
    Set<String> requisitionNames = loadRequisitionNames(cardeaCaseData.getCases());
    Set<String> projectsNames = loadProjectsNames(cardeaCaseData.getCases());
    Set<String> donorNames = loadDonorNames(cardeaCaseData.getCases());
    Set<String> testNames = getTestNames(cardeaCaseData.getCases());
    Map<String, ProjectSummary> projectSummariesByName =
        calculateProjectSummaries(cardeaCaseData.getCases(), null);
    ZonedDateTime afterTimestamp = cardeaCaseData.getTimestamp();

    CaseData caseData =
        new CaseData(cardeaCaseData.getCases(), runsByName, assaysById, omittedSamples,
            afterTimestamp,
            requisitionNames, projectsNames, donorNames, getRunNames(runsByName), testNames,
            projectSummariesByName);

    log.debug(String.format("Completed loading %d cases.", cardeaCaseData.getCases().size()));

    return caseData;
  }

  /**
   * 
   * @param builder WebClient builder state used to fetch data from Cardea API `/dimsum` endpoint
   */
  public ca.on.oicr.gsi.cardea.data.CaseData loadCardeaData(WebClient.Builder builder)
      throws IOException {
    ca.on.oicr.gsi.cardea.data.CaseData data = builder
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(LIMIT_FOR_DATA_LOAD))
        .build().get().uri(cardeaUrl + "/dimsum").retrieve()
        .bodyToFlux(ca.on.oicr.gsi.cardea.data.CaseData.class).blockLast();
    if (data == null) {
      throw new IOException("Cardea's Case API returned an empty response");
    }
    return data;
  }


  public Set<String> loadRequisitionNames(List<Case> cases) {
    Set<String> requisitionNames = new HashSet<>();
    cases.forEach(kase -> {
      requisitionNames.add(kase.getRequisition().getName());
    });
    return requisitionNames;
  }

  public Set<String> loadProjectsNames(List<Case> cases) {
    Set<String> projectsNames = new HashSet<>();
    cases.forEach(kase -> {
      kase.getProjects().forEach(project -> {
        projectsNames.add(project.getName());
      });
    });
    return projectsNames;
  }

  public Set<String> loadDonorNames(List<Case> cases) {
    Set<String> donorNames = new HashSet<>();
    cases.forEach(kase -> {
      donorNames.add(kase.getDonor().getName());
    });
    return donorNames;
  }

  private static Set<String> getTestNames(List<Case> cases) {
    return cases.stream().flatMap(kase -> kase.getTests().stream()).map(test -> test.getName())
        .collect(Collectors.toSet());
  }

  private static Set<String> getRunNames(Map<String, RunAndLibraries> runsByName) {
    return runsByName.keySet();
  }

  private Map<String, RunAndLibraries> sortRuns(List<Case> cases) {
    Map<String, RunAndLibraries.Builder> map = new HashMap<>();
    for (Case kase : cases) {
      for (Test test : kase.getTests()) {
        for (Sample sample : test.getLibraryQualifications()) {
          if (sample.getRun() != null) {
            addRunLibrary(map, sample, RunAndLibraries.Builder::addLibraryQualification);
          }
        }
        for (Sample sample : test.getFullDepthSequencings()) {
          addRunLibrary(map, sample, RunAndLibraries.Builder::addFullDepthSequencing);
        }
      }
    }
    return map.values().stream()
        .map(RunAndLibraries.Builder::build)
        .collect(Collectors.toMap(x -> x.getRun().getName(), Function.identity()));
  }

  private void addRunLibrary(Map<String, RunAndLibraries.Builder> map, Sample sample,
      BiConsumer<RunAndLibraries.Builder, Sample> addSample) {
    String runName = sample.getRun().getName();
    if (!map.containsKey(runName)) {
      map.put(runName, new RunAndLibraries.Builder().run(sample.getRun()));
    }
    addSample.accept(map.get(runName), sample);
  }

  @FunctionalInterface
  private static interface ParseFunction<T, R> {
    R apply(T input) throws DataParseException;
  }

  public static Map<String, ProjectSummary> calculateProjectSummaries(List<Case> cases,
      Collection<DateFilter> dateFilters) {
    Map<String, ProjectSummary.Builder> tempProjectSummariesByName = new HashMap<>();
    for (Case kase : cases) {
      addCounts(kase, kase.getTests(), tempProjectSummariesByName, dateFilters);
    }

    return buildProjectSummaries(tempProjectSummariesByName);
  }

  public static Map<String, ProjectSummary> calculateFilteredProjectSummaries(
      Map<Case, List<Test>> map, Collection<DateFilter> dateFilters) {
    Map<String, ProjectSummary.Builder> tempProjectSummariesByName = new HashMap<>();
    List<Case> cases = new ArrayList<>(map.keySet());
    for (Case kase : cases) {
      addCounts(kase, map.get(kase), tempProjectSummariesByName, dateFilters);
    }
    return buildProjectSummaries(tempProjectSummariesByName);

  }

  private static void addCounts(Case kase, List<Test> tests,
      Map<String, ProjectSummary.Builder> tempProjectSummariesByName,
      Collection<DateFilter> dateFilters) {
    ProjectSummary.Builder caseSummary =
        new ProjectSummary.Builder();
    int testSize = tests != null ? tests.size() : 0;
    caseSummary.totalTestCount(testSize);
    if (PendingState.RECEIPT_QC.qualifyCase(kase) && !kase.isStopped()) {
      caseSummary.receiptPendingQcCount(testSize);
    }
    if (CompletedGate.RECEIPT.qualifyCase(kase)
        && anySamplesMatch(kase.getReceipts(), dateFilters)) {
      caseSummary.receiptCompletedCount(testSize);
    }
    for (Test test : tests) {
      if ((test.isExtractionSkipped() && emptyOrNull(dateFilters))
          || (CompletedGate.EXTRACTION.qualifyTest(test)
              && anySamplesMatch(test.getExtractions(), dateFilters))) {
        caseSummary.incrementExtractionCompletedCount();
      } else if (PendingState.EXTRACTION_QC.qualifyTest(test) && !kase.isStopped()) {
        caseSummary.incrementExtractionPendingQcCount();
      } else if (PendingState.EXTRACTION.qualifyTest(test) && !kase.isStopped()) {
        caseSummary.incrementExtractionPendingCount();
      }

      // library Preparation
      if ((test.isLibraryPreparationSkipped() && emptyOrNull(dateFilters))
          || (CompletedGate.LIBRARY_PREPARATION.qualifyTest(test)
              && anySamplesMatch(test.getLibraryPreparations(), dateFilters))) {
        caseSummary.incrementLibraryPrepCompletedCount();
      } else if (PendingState.LIBRARY_QC.qualifyTest(test) && !kase.isStopped()) {
        caseSummary.incrementLibraryPrepPendingQcCount();
      } else if (PendingState.LIBRARY_PREPARATION.qualifyTest(test) && !kase.isStopped()) {
        caseSummary.incrementLibraryPrepPendingCount();
      }

      // Library Qualification
      if (CompletedGate.LIBRARY_QUALIFICATION.qualifyTest(test)
          && anySamplesMatch(test.getLibraryQualifications(), dateFilters)) {
        caseSummary.incrementLibraryQualCompletedCount();
      } else if ((PendingState.LIBRARY_QUALIFICATION_QC.qualifyTest(test)
          || PendingState.LIBRARY_QUALIFICATION_DATA_REVIEW.qualifyTest(test))
          && !kase.isStopped()) {
        caseSummary.incrementLibraryQualPendingQcCount();
      } else if (PendingState.LIBRARY_QUALIFICATION.qualifyTest(test) && !kase.isStopped()) {
        caseSummary.incrementLibraryQualPendingCount();
      }

      // Full depth sequncing
      if (CompletedGate.FULL_DEPTH_SEQUENCING.qualifyTest(test)
          && anySamplesMatch(test.getFullDepthSequencings(), dateFilters)) {
        caseSummary.incrementFullDepthSeqCompletedCount();
      } else if ((PendingState.FULL_DEPTH_QC.qualifyTest(test)
          || PendingState.FULL_DEPTH_DATA_REVIEW.qualifyTest(test)) && !kase.isStopped()) {
        caseSummary.incrementFullDepthSeqPendingQcCount();
      } else if (PendingState.FULL_DEPTH_SEQUENCING.qualifyTest(test) && !kase.isStopped()) {
        caseSummary.incrementFullDepthSeqPendingCount();
      }
    }

    // analysis review
    if (CompletedGate.ANALYSIS_REVIEW.qualifyCase(kase)
        && anyRequisitionQcsMatch(kase.getRequisition().getAnalysisReviews(), dateFilters)) {
      caseSummary.analysisReviewCompletedCount(testSize);
    }
    if (PendingState.ANALYSIS_REVIEW.qualifyCase(kase) && !kase.isStopped()) {
      caseSummary.analysisReviewPendingCount(testSize);
    }

    // release approval
    if (CompletedGate.RELEASE_APPROVAL.qualifyCase(kase)
        && anyRequisitionQcsMatch(kase.getRequisition().getReleaseApprovals(), dateFilters)) {
      caseSummary.releaseApprovalCompletedCount(testSize);
    }
    if (PendingState.RELEASE_APPROVAL.qualifyCase(kase)) {
      caseSummary.releaseApprovalPendingCount(testSize);
    }

    // release
    if (CompletedGate.RELEASE.qualifyCase(kase)
        && anyRequisitionQcsMatch(kase.getRequisition().getReleases(), dateFilters)) {
      caseSummary.releaseCompletedCount(testSize);
    }
    if (PendingState.RELEASE.qualifyCase(kase)) {
      caseSummary.releasePendingCount(testSize);
    }

    // add the counts to each project in the case if the project exists in the
    // projectSummariesByName
    for (Project project : kase.getProjects()) {
      if (tempProjectSummariesByName.containsKey(project.getName())
          && !tempProjectSummariesByName.isEmpty()) {
        tempProjectSummariesByName.get(project.getName()).addCounts(caseSummary);
      } else {
        ProjectSummary.Builder projectSummary =
            new ProjectSummary.Builder().name(project.getName()).addCounts(caseSummary)
                .pipeline(project.getPipeline());
        tempProjectSummariesByName.put(project.getName(), projectSummary);
      }
    }
  }

  private static Map<String, ProjectSummary> buildProjectSummaries(
      Map<String, ProjectSummary.Builder> tempProjectSummariesByName) {
    Map<String, ProjectSummary> projectSummariesByName = new HashMap<>();
    for (Map.Entry<String, ProjectSummary.Builder> entry : tempProjectSummariesByName.entrySet()) {
      ProjectSummary projectSummary = entry.getValue().build();
      projectSummariesByName.put(entry.getKey(), projectSummary);
    }
    return projectSummariesByName;
  }

  private static boolean anySamplesMatch(List<Sample> samples, Collection<DateFilter> filters) {
    if (filters == null || filters.isEmpty()) {
      return !samples.isEmpty();
    }
    return samples.stream()
        .anyMatch(sample -> filters.stream()
            .allMatch(filter -> filter.samplePredicate().test(sample)));
  }

  private static boolean anyRequisitionQcsMatch(List<RequisitionQc> requisitionQcs,
      Collection<DateFilter> filters) {
    if (filters == null || filters.isEmpty()) {
      return !requisitionQcs.isEmpty();
    }
    return requisitionQcs.stream()
        .anyMatch(qc -> filters.stream()
            .allMatch(filter -> filter.requisitionQcPredicate().test(qc)));
  }

  private static boolean emptyOrNull(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

}
