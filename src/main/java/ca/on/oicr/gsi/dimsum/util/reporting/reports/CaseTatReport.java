package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;
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
              Column.forString("Receipt Completed Date",
                  x -> Optional
                      .ofNullable(findLatestCompletionDate(x.getCase().getReceipts()))
                      .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse("")),
              Column.forInteger("Receipt Days", x -> x.getCase().getReceiptDaysSpent()),
              Column.forString("Test", x -> x.getTest().getName()),
              Column.forString("Supplemental Only",
                  x -> isSupplementalOnly(x.getTest(), x.getCase().getRequisition()) ? "Yes"
                      : "No"),
              Column.forString("Extraction Completed Date",
                  x -> Optional
                      .ofNullable(findLatestCompletionDate(x.getTest().getExtractions()))
                      .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse("")),
              Column.forInteger("Extraction Days", x -> x.getTest().getExtractionDaysSpent()),
              Column.forString("Library Prep Completed Date",
                  x -> Optional
                      .ofNullable(findLatestCompletionDate(x.getTest().getLibraryPreparations()))
                      .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse("")),
              Column.forInteger("Library Prep. Days",
                  x -> x.getTest().getLibraryPreparationDaysSpent()),
              Column.forString("Library Qual Completed Date",
                  x -> Optional
                      .ofNullable(findLatestCompletionDate(x.getTest().getLibraryQualifications()))
                      .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse("")),
              Column.forInteger("Library Qual. Days",
                  x -> x.getTest().getLibraryQualificationDaysSpent()),
              Column.forString("Full Depth Completed Date",
                  x -> Optional
                      .ofNullable(findLatestCompletionDate(x.getTest().getFullDepthSequencings()))
                      .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse("")),
              Column.forInteger("Full-Depth Days",
                  x -> x.getTest().getFullDepthSequencingDaysSpent()),
              Column.forString("Analysis Review Completed", x -> {
                List<LocalDate> dates = x.getCase().getDeliverables().stream()
                    .map(CaseDeliverable::getAnalysisReviewQcDate).collect(Collectors.toList());
                if (dates.contains(null)) {
                  return null;
                }
                return dates.stream().max(LocalDate::compareTo)
                    .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);
              }),
              Column.forInteger("Analysis Review Days",
                  x -> x.getCase().getAnalysisReviewDaysSpent()),
              Column.forString("Release Approval Completed", x -> {
                List<LocalDate> dates = x.getCase().getDeliverables().stream()
                    .map(CaseDeliverable::getReleaseApprovalQcDate).collect(Collectors.toList());
                if (dates.contains(null)) {
                  return null;
                }
                return dates.stream().max(LocalDate::compareTo)
                    .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);
              }),
              Column.forInteger("Release Approval Days",
                  x -> x.getCase().getReleaseApprovalDaysSpent()),
              Column.forString("Completion Date", x -> getCompletionDate(x.getCase())),
              Column.forInteger("Total Days", x -> x.getCase().getCaseDaysSpent()))) {

        @Override
        public List<RowData> getData(CaseService caseService, Map<String, String> parameters) {
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

  private static List<CaseFilter> convertParametersToFilters(Map<String, String> parameters) {
    List<CaseFilter> filters = new ArrayList<>();
    parameters.forEach((key, value) -> {
      CaseFilterKey filterKey = CaseFilterKey.valueOf(key.toUpperCase());
      CaseFilter filter = new CaseFilter(filterKey, value);
      filters.add(filter);
    });
    return filters;
  }

  private static boolean isSupplementalOnly(Test test, Requisition requisition) {
    if (test == null || test.getExtractions().isEmpty()) {
      return false;
    }
    return test.getExtractions().stream()
        .allMatch(sample -> !sample.getRequisitionId()
            .equals(requisition.getId()));
  }

  private static LocalDate findLatestCompletionDate(List<Sample> samples) {
    return samples.stream()
        .filter(SampleUtils::isPassed)
        .map(sample -> {
          if (sample.getRun() != null) {
            LocalDate sampleReviewDate = sample.getDataReviewDate();
            LocalDate runReviewDate = sample.getRun().getDataReviewDate();
            if (sampleReviewDate == null || runReviewDate == null) {
              return null;
            }
            return Stream.of(sampleReviewDate, runReviewDate)
                .max(LocalDate::compareTo)
                .orElse(null);
          } else {
            return sample.getQcDate();
          }
        })
        .max(LocalDate::compareTo)
        .orElse(null);
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
