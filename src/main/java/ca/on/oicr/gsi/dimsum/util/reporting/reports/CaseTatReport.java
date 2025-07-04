package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseQc;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.util.DataUtils;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.StaticTableReportSection;

public class CaseTatReport extends Report {

  // TODO: generate columns dynamically instead of hard-coding deliverable categories:
  // Ticket: GLT-4422
  private static final String DELIVERABLE_CLINICAL = "Clinical Report";
  private static final String DELIVERABLE_DATA = "Data Release";

  private static record RowData(Case kase, Test test, CaseDeliverable clinical,
      CaseDeliverable dataRelease) {
  }

  // the TAT Trend Report depends on the column names defined here
  private static final ReportSection<RowData> caseSection =
      new StaticTableReportSection<>("Case TAT",
          Arrays.asList(
              Column.forString("Case ID", x -> x.kase().getId()),
              Column.forString("Projects",
                  x -> getSortedProjectNameOrPipeline(x.kase(), Project::getName)),
              Column.forString("Pipeline",
                  x -> getSortedProjectNameOrPipeline(x.kase(), Project::getPipeline)),
              Column.forString("Requisition", x -> x.kase().getRequisition().getName()),
              Column.forString("Assay", x -> x.kase().getAssayName()),
              Column.forString("Start Date",
                  x -> x.kase().getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)),
              Column.forString("Receipt Completed",
                  x -> findLatestCompletionDate(x.kase().getReceipts(), false)),
              Column.forInteger("Receipt Days", x -> x.kase().getReceiptDaysSpent()),
              Column.forString("Test", x -> x.test().getName()),
              Column.forString("Supplemental Only",
                  x -> isSupplementalOnly(x.test(), x.kase().getRequisition()) ? "Yes" : "No"),
              Column.forString(
                  "Extraction (EX) Completed",
                  x -> findLatestCompletionDate(x.test().getExtractions(), true)),
              Column.forInteger("EX Days", x -> x.test().getExtractionDaysSpent()),
              Column.forInteger("EX Prep. Days", x -> x.test().getExtractionPreparationDaysSpent()),
              Column.forInteger("EX QC Days", x -> x.test().getExtractionQcDaysSpent()),
              Column.forInteger("EX Transfer Days", x -> x.test().getExtractionTransferDaysSpent()),
              Column.forString("Library Prep. Completed",
                  x -> findLatestCompletionDate(x.test().getLibraryPreparations(), false)),
              Column.forInteger("Library Prep. Days",
                  x -> x.test().getLibraryPreparationDaysSpent()),
              Column.forString("Library Qual. (LQ) Completed",
                  x -> findLatestCompletionDate(x.test().getLibraryQualifications(), false)),
              Column.forInteger("LQ Loading Days",
                  x -> x.test().getLibraryQualificationLoadingDaysSpent()),
              Column.forInteger("LQ Sequencing Days",
                  x -> x.test().getLibraryQualificationSequencingDaysSpent()),
              Column.forInteger("LQ QC Days", x -> x.test().getLibraryQualificationQcDaysSpent()),
              Column.forInteger("LQ Total Days", x -> x.test().getLibraryQualificationDaysSpent()),
              Column.forString("Full-Depth (FD) Completed",
                  x -> findLatestCompletionDate(x.test().getFullDepthSequencings(), false)),
              Column.forInteger("FD Loading Days",
                  x -> x.test().getFullDepthSequencingLoadingDaysSpent()),
              Column.forInteger("FD Sequencing Days",
                  x -> x.test().getFullDepthSequencingSequencingDaysSpent()),
              Column.forInteger("FD QC Days", x -> x.test().getFullDepthSequencingQcDaysSpent()),
              Column.forInteger("FD Total Days", x -> x.test().getFullDepthSequencingDaysSpent()),
              // Clinical Report columns
              Column.forString("Clinical Report (CR)",
                  x -> hasDeliverableCategory(x, DELIVERABLE_CLINICAL)),
              Column.forString("CR Analysis Review Completed",
                  x -> getAnalysisReviewCompletedDate(x.clinical())),
              Column.forInteger("CR Analysis Review Days",
                  x -> x.clinical() == null ? null : x.clinical().getAnalysisReviewDaysSpent()),
              Column.forString("CR Release Approval Completed",
                  x -> getReleaseApprovalCompletedDate(x.clinical())),
              Column.forInteger("CR Release Approval Days",
                  x -> x.clinical() == null ? null : x.clinical().getReleaseApprovalDaysSpent()),
              Column.forString("CR Release Completed", x -> getCompletionDate(x.clinical())),
              Column.forInteger("CR Release Days",
                  x -> x.clinical() == null ? null : x.clinical().getReleaseDaysSpent()),
              Column.forInteger("CR Total Days",
                  x -> x.clinical() == null ? null : x.clinical().getDeliverableDaysSpent()),
              // Data Release columns
              Column.forString("Data Release (DR)",
                  x -> hasDeliverableCategory(x, DELIVERABLE_DATA)),
              Column.forString("DR Analysis Review Completed",
                  x -> getAnalysisReviewCompletedDate(x.dataRelease())),
              Column.forInteger("DR Analysis Review Days",
                  x -> x.dataRelease() == null ? null
                      : x.dataRelease().getAnalysisReviewDaysSpent()),
              Column.forString("DR Release Approval Completed",
                  x -> getReleaseApprovalCompletedDate(x.dataRelease())),
              Column.forInteger("DR Release Approval Days",
                  x -> x.dataRelease() == null ? null
                      : x.dataRelease().getReleaseApprovalDaysSpent()),
              Column.forString("DR Release Completed", x -> getCompletionDate(x.dataRelease())),
              Column.forInteger("DR Release Days",
                  x -> x.dataRelease() == null ? null : x.dataRelease().getReleaseDaysSpent()),
              Column.forInteger("DR Total Days",
                  x -> x.dataRelease() == null ? null : x.dataRelease().getDeliverableDaysSpent()),
              // Combined (case-level) columns
              Column.forString("ALL Analysis Review Completed",
                  x -> getGateCompletedDate(x, CaseDeliverable::getAnalysisReviewQcStatus,
                      CaseDeliverable::getAnalysisReviewQcDate)),
              Column.forInteger("ALL Analysis Review Days",
                  x -> x.kase().getAnalysisReviewDaysSpent()),
              Column.forString("ALL Release Approval Completed",
                  x -> getGateCompletedDate(x, CaseDeliverable::getReleaseApprovalQcStatus,
                      CaseDeliverable::getReleaseApprovalQcDate)),
              Column.forInteger("ALL Release Approval Days",
                  x -> x.kase().getReleaseApprovalDaysSpent()),
              Column.forString("ALL Release Completed", caseTatReport -> {
                LocalDate completionDate = DataUtils.getCompletionDate(caseTatReport.kase);
                return completionDate != null ? completionDate.toString() : null;
              }),
              Column.forInteger("ALL Release Days", x -> x.kase().getReleaseDaysSpent()),
              Column.forInteger("ALL Total Days", x -> x.kase().getCaseDaysSpent()),
              Column.forString("Stopped", x -> x.kase().isStopped() ? "YES" : "no"))) {

        @Override
        public List<RowData> getData(CaseService caseService, JsonNode parameters) {
          List<CaseFilter> filters = getParameterFilters(parameters);
          return caseService.getCaseStream(filters)
              .flatMap(kase -> kase.getTests().stream().map(test -> {
                CaseDeliverable clinical = getDeliverableCategory(kase, DELIVERABLE_CLINICAL);
                CaseDeliverable dataRelease = getDeliverableCategory(kase, DELIVERABLE_DATA);
                return new RowData(kase, test, clinical, dataRelease);
              }))
              .collect(Collectors.toList());
        }

        private static CaseDeliverable getDeliverableCategory(Case kase,
            String deliverableCategory) {
          return kase.getDeliverables().stream()
              .filter(x -> Objects.equals(x.getDeliverableCategory(), deliverableCategory))
              .findFirst()
              .orElse(null);
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

  private static boolean isSupplementalOnly(Test test, Requisition requisition) {
    if (test.getExtractions().isEmpty() && test.getLibraryPreparations().isEmpty()
        && test.getLibraryQualifications().isEmpty() && test.getFullDepthSequencings().isEmpty()) {
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

  private static String findLatestCompletionDate(List<Sample> samples, boolean requiresTransfer) {
    LocalDate latestDate = null;
    for (Sample sample : samples) {
      if (!DataUtils.isPassed(sample) || (requiresTransfer && sample.getTransferDate() == null)) {
        continue;
      }
      LocalDate dateToCompare = null;
      if (requiresTransfer) {
        dateToCompare = sample.getTransferDate();
      } else if (sample.getRun() != null) {
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
    return latestDate != null ? latestDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
  }

  private static String getGateCompletedDate(RowData rowData,
      Function<CaseDeliverable, CaseQc> statusExtractor,
      Function<CaseDeliverable, LocalDate> dateExtractor) {
    if (rowData.kase().getDeliverables().stream()
        .anyMatch(deliverable -> DataUtils.isPending((statusExtractor.apply(deliverable))))) {
      return null;
    }
    return formatDate(rowData.kase().getDeliverables().stream()
        .map(dateExtractor)
        .max(LocalDate::compareTo)
        .orElse(null));
  }

  private static String getAnalysisReviewCompletedDate(CaseDeliverable deliverable) {
    if (deliverable == null || DataUtils.isPending(deliverable.getAnalysisReviewQcStatus())) {
      return null;
    }
    return formatDate(deliverable.getAnalysisReviewQcDate());
  }

  private static String getReleaseApprovalCompletedDate(CaseDeliverable deliverable) {
    if (deliverable == null || DataUtils.isPending(deliverable.getReleaseApprovalQcStatus())) {
      return null;
    }
    return formatDate(deliverable.getReleaseApprovalQcDate());
  }

  private static String getCompletionDate(CaseDeliverable deliverable) {
    if (deliverable == null || deliverable.getReleases().stream()
        .anyMatch(release -> DataUtils.isPending(release.getQcStatus()))) {
      return null;
    }

    return formatDate(deliverable.getReleases().stream()
        .map(CaseRelease::getQcDate)
        .max(LocalDate::compareTo)
        .orElse(null));
  }

  private static String hasDeliverableCategory(RowData rowData, String deliverableCategory) {
    return rowData.kase().getDeliverables().stream().anyMatch(
        deliverable -> Objects.equals(deliverable.getDeliverableCategory(), deliverableCategory))
            ? "YES"
            : "no";
  }

  private static String formatDate(LocalDate date) {
    return date == null ? null : date.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }
}
