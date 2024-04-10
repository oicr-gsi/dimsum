package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.controller.rest.request.KeyValuePair;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.util.SampleUtils;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.TableReportSection;

public class CaseTatReport extends Report {

  private static class RowData {

    private final Case kase;
    private final Test test;

    public RowData(Case kase, Test test) {
      this.kase = kase;
      this.test = test;
    }

    public Case getCase() {
      return kase;
    }

    public Test getTest() {
      return test;
    }
  }

  private static final ReportSection<RowData> caseSection =
      new TableReportSection<>("Case Report",
          Arrays.asList(
              Column.forString("Case ID", x -> x.getCase().getId()),
              Column.forString("Projects",
                  x -> getSortedProjectNameOrPipeline(x.getCase(), Project::getName)),
              Column.forString("Pipeline",
                  x -> getSortedProjectNameOrPipeline(x.getCase(), Project::getPipeline)),
              Column.forString("Requisition", x -> x.getCase().getRequisition().getName()),
              Column.forString("Assay", x -> x.getCase().getAssayName()),
              Column.forString("Start Date",
                  x -> x.getCase().getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)),
              Column.forString("Receipt Completed",
                  x -> findLatestCompletionDate(x.getCase().getReceipts())),
              Column.forInteger("Receipt Days", x -> x.getCase().getReceiptDaysSpent()),
              Column.forString("Test", x -> x.getTest().getName()),
              Column.forString("Supplemental Only",
                  x -> isSupplementalOnly(x.getTest(), x.getCase().getRequisition()) ? "Yes"
                      : "No"),
              Column.forString("Extraction Completed",
                  x -> findLatestCompletionDate(x.getTest().getExtractions())),
              Column.forInteger("Extraction Days", x -> x.getTest().getExtractionDaysSpent()),
              Column.forString("Library Prep Completed",
                  x -> findLatestCompletionDate(x.getTest().getLibraryPreparations())),
              Column.forInteger("Library Prep. Days",
                  x -> x.getTest().getLibraryPreparationDaysSpent()),
              Column.forString("Library Qual Completed",
                  x -> findLatestCompletionDate(x.getTest().getLibraryQualifications())),
              Column.forInteger("Library Qual. Days",
                  x -> x.getTest().getLibraryQualificationDaysSpent()),
              Column.forString("Full-Depth Completed",
                  x -> findLatestCompletionDate(x.getTest().getFullDepthSequencings())),
              Column.forInteger("Full-Depth Days",
                  x -> x.getTest().getFullDepthSequencingDaysSpent()),
              Column.forString("Analysis Review Completed",
                  x -> getMaxDateFromDeliverables(x.getCase(),
                      CaseDeliverable::getAnalysisReviewQcDate)),
              Column.forInteger("Analysis Review Days",
                  x -> x.getCase().getAnalysisReviewDaysSpent()),
              Column.forString("Release Approval Completed",
                  x -> getMaxDateFromDeliverables(x.getCase(),
                      CaseDeliverable::getReleaseApprovalQcDate)),
              Column.forString("Release Completed", x -> {
                if (!x.getCase().isStopped()) {
                  return getCompletionDate(x.getCase());
                }
                List<LocalDate> dates = x.getCase().getDeliverables().stream()
                    .flatMap(deliverable -> deliverable.getReleases().stream())
                    .map(CaseRelease::getQcDate)
                    .collect(Collectors.toList());
                if (dates.contains(null)) {
                  return null;
                }
                return dates.stream().max(LocalDate::compareTo)
                    .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);
              }),
              Column.forInteger("Release Days", x -> x.getCase().getReleaseDaysSpent()),
              Column.forString("Completion Date", x -> getCompletionDate(x.getCase())),
              Column.forInteger("Total Days", x -> x.getCase().getCaseDaysSpent()))) {

        @Override
        public List<RowData> getData(CaseService caseService, JsonNode parameters) {
          List<CaseFilter> filters = convertParametersToFilters(parameters);
          return caseService.getCaseStream(filters)
              .flatMap(kase -> kase.getTests().stream().map(test -> new RowData(kase, test)))
              .collect(Collectors.toList());
        }
      };

  public static final CaseTatReport INSTANCE = new CaseTatReport();

  private CaseTatReport() {
    super("Case TAT Report", caseSection);
  }

  private static String getSortedProjectNameOrPipeline(Case kase,
      Function<Project, String> function) {
    return kase.getProjects().stream()
        .map(function)
        .sorted()
        .collect(Collectors.joining(", "));
  }

  private static List<CaseFilter> convertParametersToFilters(JsonNode parameters) {
    DataQuery dataQuery = new DataQuery();
    List<KeyValuePair> kvpFilters = new ArrayList<>();

    if (parameters.isArray()) {
      for (JsonNode parameter : parameters) {
        String key = parameter.get("key").asText();
        String value = parameter.get("value").asText();
        kvpFilters.add(new KeyValuePair(key, value));
      }
    }

    dataQuery.setFilters(kvpFilters);
    List<CaseFilter> caseFilters = MvcUtils.parseCaseFilters(dataQuery);
    return caseFilters;
  }

  private static boolean isSupplementalOnly(Test test, Requisition requisition) {
    if (test.getExtractions().isEmpty()
        && test.getLibraryPreparations().isEmpty()
        && test.getLibraryQualifications().isEmpty()
        && test.getFullDepthSequencings().isEmpty()) {
      return false;
    }
    return test.getExtractions().stream()
        .allMatch(sample -> !Objects.equals(sample.getRequisitionId(), requisition.getId()))
        && test.getLibraryPreparations().stream()
            .allMatch(sample -> !Objects.equals(sample.getRequisitionId(), requisition.getId()))
        && test.getLibraryQualifications().stream()
            .allMatch(sample -> !Objects.equals(sample.getRequisitionId(), requisition.getId()))
        && test.getFullDepthSequencings().stream()
            .allMatch(sample -> !Objects.equals(sample.getRequisitionId(), requisition.getId()));
  }

  private static String findLatestCompletionDate(List<Sample> samples) {
    LocalDate latestDate = null;
    for (Sample sample : samples) {
      if (SampleUtils.isPassed(sample)) {
        LocalDate dateToCompare = null;
        if (sample.getRun() != null) {
          LocalDate sampleReviewDate = sample.getDataReviewDate();
          LocalDate runReviewDate = sample.getRun().getDataReviewDate();
          if (sampleReviewDate != null && runReviewDate != null) {
            dateToCompare =
                runReviewDate.isAfter(sampleReviewDate) ? runReviewDate : sampleReviewDate;
          }
        } else {
          dateToCompare = sample.getQcDate();
        }
        if (dateToCompare != null && (latestDate == null || dateToCompare.isAfter(latestDate))) {
          latestDate = dateToCompare;
        }
      }
    }
    return latestDate != null ? latestDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
  }

  private static String getMaxDateFromDeliverables(Case x,
      Function<CaseDeliverable, LocalDate> dateExtractor) {
    List<LocalDate> dates = x.getDeliverables().stream()
        .map(dateExtractor)
        .collect(Collectors.toList());
    if (dates.contains(null)) {
      return null;
    }
    return dates.stream().max(LocalDate::compareTo)
        .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);
  }

  private static String getCompletionDate(Case x) {
    if (x.isStopped()) {
      return "STOPPED";
    }
    List<CaseDeliverable> deliverables = x.getDeliverables();
    if (deliverables.isEmpty()) {
      return null;
    }
    List<CaseRelease> releases = deliverables.stream()
        .flatMap(deliverable -> deliverable.getReleases().stream())
        .collect(Collectors.toList());
    if (releases.isEmpty()) {
      return null;
    }
    if (releases.stream()
        .anyMatch(release -> release.getQcPassed() == null || !release.getQcPassed())) {
      return null;
    }
    LocalDate latestQcDate = releases.stream()
        .map(CaseRelease::getQcDate)
        .max(LocalDate::compareTo)
        .orElse(null);
    return latestQcDate != null ? latestQcDate.toString() : null;
  }
}
