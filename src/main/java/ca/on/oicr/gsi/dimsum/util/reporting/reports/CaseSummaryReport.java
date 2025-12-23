package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.dimsum.data.external.ExternalCase;
import ca.on.oicr.gsi.dimsum.data.external.ExternalCaseDeliverable;
import ca.on.oicr.gsi.dimsum.data.external.ExternalCaseRelease;
import ca.on.oicr.gsi.dimsum.data.external.ExternalSample;
import ca.on.oicr.gsi.dimsum.data.external.ExternalTest;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.util.DataUtils;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.DynamicTableReportSection;

public class CaseSummaryReport extends Report {

  private record RowData(ExternalCase kase, Assay assay) {
  }

  private static final ReportSection<RowData> mainSection =
      new DynamicTableReportSection<RowData>("Cases") {

        @Override
        public List<Column<RowData>> getColumns(List<RowData> data) {
          List<Column<RowData>> columns = new ArrayList<>();
          columns.addAll(Arrays.asList(
              Column.forString("Case ID", row -> row.kase().id()),
              Column.forString("Project", CaseSummaryReport::getProjectNames),
              Column.forString("Donor", row -> row.kase().donor().getName()),
              Column.forString("External Name", row -> row.kase().donor().getExternalName()),
              Column.forString("Requisition", row -> row.kase().requisition().getName()),
              Column.forString("Assay", row -> row.assay().getName()),
              Column.forString("Assay Version", row -> row.assay().getVersion()),
              Column.forString("Stopped",
                  row -> row.kase().requisition().isStopped() ? "Yes" : "No"),
              Column.forString("Receipt Completed", CaseSummaryReport::getReceiptCompletedDate),
              Column.forString("Extraction Completed",
                  CaseSummaryReport::getExtractionCompletedDate),
              Column.forString("Library Prep. Completed",
                  CaseSummaryReport::getLibraryPrepCompletedDate),
              Column.forString("Library Qual. Completed",
                  CaseSummaryReport::getLibraryQualCompletedDate),
              Column.forString("Full-Depth Completed",
                  CaseSummaryReport::getFullDepthCompletedDate),
              Column.forString("Analysis Review Completed",
                  CaseSummaryReport::getAnalysisReviewCompletedDate),
              Column.forString("Release Approval Completed",
                  CaseSummaryReport::getReleaseApprovalCompletedDate)));

          Map<String, Set<String>> deliverablesByCategory = new HashMap<>();
          for (RowData row : data) {
            for (ExternalCaseDeliverable deliverableCategory : row.kase().deliverables()) {
              Set<String> deliverables =
                  deliverablesByCategory.computeIfAbsent(deliverableCategory.deliverableCategory(),
                      x -> new HashSet<>());
              for (ExternalCaseRelease release : deliverableCategory.releases()) {
                deliverables.add(release.deliverable());
              }
            }
          }

          deliverablesByCategory.forEach((deliverableCategory, deliverables) -> {
            for (String deliverable : deliverables) {
              String deliverableLabel = deliverable.startsWith(deliverableCategory) ? deliverable
                  : "%s %s".formatted(deliverableCategory, deliverable);
              columns.add(Column.forString(deliverableLabel + " Status",
                  kase -> getDeliverableStatus(kase, deliverableCategory, deliverable)));
              columns.add(Column.forString(deliverableLabel + " Completed",
                  kase -> getDeliverableCompletedDate(kase, deliverableCategory, deliverable)));
            }
          });

          return columns;
        }

        @Override
        public List<RowData> getData(CaseService caseService, JsonNode parameters) {
          List<CaseFilter> filters = getParameterFilters(parameters);
          Map<Long, Assay> assays = caseService.getAssaysById();
          return caseService.getExternalCaseStream(filters)
              .map(kase -> new RowData(kase, assays.get(kase.assayId())))
              .toList();
        }

      };

  public static CaseSummaryReport INSTANCE = new CaseSummaryReport();

  private CaseSummaryReport() {
    super("Case Summary Report", mainSection);
  }

  private static String getProjectNames(RowData row) {
    return row.kase().projects().stream()
        .map(Project::getName)
        .sorted()
        .collect(Collectors.joining(", "));
  }

  private static String getReceiptCompletedDate(RowData row) {
    if (row.kase().receipts().stream().noneMatch(DataUtils::isPassed)) {
      return null;
    }
    return findLatestCompletionDate(row.kase().receipts());
  }

  private static String getExtractionCompletedDate(RowData row) {
    return getTestStepCompletedDate(row, ExternalTest::extractionSkipped,
        ExternalTest::extractions);
  }

  private static String getLibraryPrepCompletedDate(RowData row) {
    return getTestStepCompletedDate(row, ExternalTest::libraryPreparationSkipped,
        ExternalTest::libraryPreparations);
  }

  private static String getLibraryQualCompletedDate(RowData row) {
    return getTestStepCompletedDate(row, ExternalTest::libraryQualificationSkipped,
        ExternalTest::libraryQualifications);
  }

  private static String getFullDepthCompletedDate(RowData row) {
    return getTestStepCompletedDate(row, test -> false,
        ExternalTest::fullDepthSequencings);
  }

  private static String getTestStepCompletedDate(RowData row,
      Predicate<ExternalTest> isSkipped, Function<ExternalTest, List<ExternalSample>> getSamples) {
    if (row.kase().tests().stream().allMatch(isSkipped)) {
      return "N/A";
    }
    if (row.kase().tests().stream()
        .anyMatch(test -> !isSkipped.test(test) && getSamples.apply(test).stream()
            .noneMatch(sample -> DataUtils.isPassed(sample)))) {
      // incomplete
      return null;
    }
    return findLatestCompletionDate(row.kase().tests().stream()
        .flatMap(test -> getSamples.apply(test).stream())
        .toList());
  }

  private static String getAnalysisReviewCompletedDate(RowData row) {
    return getDeliverableTypeCompletedDate(row, ExternalCaseDeliverable::analysisReviewQcStatus,
        AnalysisReviewQcStatus.PENDING, AnalysisReviewQcStatus.NOT_APPLICABLE,
        ExternalCaseDeliverable::analysisReviewQcDate);
  }

  private static String getReleaseApprovalCompletedDate(RowData row) {
    return getDeliverableTypeCompletedDate(row, ExternalCaseDeliverable::releaseApprovalQcStatus,
        AnalysisReviewQcStatus.PENDING, ReleaseApprovalQcStatus.NOT_APPLICABLE,
        ExternalCaseDeliverable::releaseApprovalQcDate);
  }

  private static <T> String getDeliverableTypeCompletedDate(RowData row,
      Function<ExternalCaseDeliverable, T> getQcStatus, T pendingStatus, T notApplicableStatus,
      Function<ExternalCaseDeliverable, LocalDate> getQcDate) {
    boolean allNotApplicable = true;
    LocalDate latestDate = null;
    for (ExternalCaseDeliverable deliverable : row.kase().deliverables()) {
      T qcStatus = getQcStatus.apply(deliverable);
      if (qcStatus == null || qcStatus == pendingStatus) {
        // incomplete
        return null;
      } else if (qcStatus != notApplicableStatus) {
        allNotApplicable = false;
        LocalDate qcDate = getQcDate.apply(deliverable);
        if (latestDate == null || qcDate.isAfter(latestDate)) {
          latestDate = qcDate;
        }
      }
    }
    if (allNotApplicable) {
      return "N/A";
    } else {
      return latestDate == null ? null : latestDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
  }

  private static String findLatestCompletionDate(List<ExternalSample> samples) {
    LocalDate latestDate = null;
    for (ExternalSample sample : samples) {
      if (!DataUtils.isPassed(sample)) {
        continue;
      }
      LocalDate dateToCompare = null;
      if (sample.run() != null) {
        LocalDate sampleReviewDate = sample.dataReviewDate();
        LocalDate runReviewDate = sample.run().dataReviewDate();
        if (sampleReviewDate != null && runReviewDate != null) {
          dateToCompare =
              runReviewDate.isAfter(sampleReviewDate) ? runReviewDate : sampleReviewDate;
        }
      } else {
        dateToCompare = sample.qcDate();
      }
      if (dateToCompare != null && (latestDate == null || dateToCompare.isAfter(latestDate))) {
        latestDate = dateToCompare;
      }
    }
    return latestDate != null ? latestDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
  }

  private static String getDeliverableStatus(RowData row, String deliverableCategory,
      String deliverable) {
    ExternalCaseRelease release = getRelease(row.kase(), deliverableCategory, deliverable);
    if (release == null) {
      return ReleaseQcStatus.NOT_APPLICABLE.getLabel();
    } else if (release.qcStatus() == null || release.qcStatus() == ReleaseQcStatus.PENDING) {
      return null;
    } else {
      return release.qcStatus().getLabel();
    }
  }

  private static String getDeliverableCompletedDate(RowData row, String deliverableCategory,
      String deliverable) {
    ExternalCaseRelease release = getRelease(row.kase(), deliverableCategory, deliverable);
    if (release == null) {
      return "N/A";
    } else if (release.qcStatus() == null || release.qcStatus() == ReleaseQcStatus.PENDING) {
      return null;
    } else {
      return release.qcDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
  }

  private static ExternalCaseRelease getRelease(ExternalCase kase, String deliverableCategory,
      String deliverable) {
    for (ExternalCaseDeliverable cat : kase.deliverables()) {
      if (!Objects.equals(deliverableCategory, cat.deliverableCategory())) {
        continue;
      }
      for (ExternalCaseRelease release : cat.releases()) {
        if (!Objects.equals(deliverable, release.deliverable())) {
          continue;
        }
        return release;
      }
      break;
    }
    return null;
  }

}
