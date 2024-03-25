package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.TableReportSection;

public class CaseReport extends Report {

        private static class RowData {
                private final Case kase;
                private final Test test;
                private final Assay assay;
                private final Requisition requisition;
                private final LocalDate completionDate;

                public RowData(Case kase, Test test, Assay assay, Requisition requisition,
                                LocalDate completionDate) {
                        this.kase = kase;
                        this.test = test;
                        this.assay = assay;
                        this.requisition = requisition;
                        this.completionDate = completionDate;
                }

                public Case getCase() {
                        return kase;
                }

                public Test getTest() {
                        return test;
                }

                public Assay getAssay() {
                        return assay;
                }

                public Requisition getRequisition() {
                        return requisition;
                }
        }

        private static final ReportSection<RowData> caseSection =
                        new TableReportSection<RowData>("Case Report",
                                        Arrays.asList(
                                                        Column.forString("Case ID",
                                                                        x -> x.getCase().getId()),
                                                        Column.forString("Projects",
                                                                        x -> x.getCase().getProjects()
                                                                                        .stream()
                                                                                        .map(Project::getName)
                                                                                        .collect(Collectors
                                                                                                        .joining(", "))),
                                                        Column.forString("Requisition",
                                                                        x -> x.getRequisition()
                                                                                        .getName()),
                                                        Column.forString("Assay",
                                                                        x -> x.getAssay()
                                                                                        .getName()),
                                                        Column.forString("Assay Test",
                                                                        x -> x.getTest().getName()),
                                                        Column.forString("Start Date",
                                                                        x -> x.getCase().getStartDate()
                                                                                        .format(DateTimeFormatter.ISO_LOCAL_DATE)),
                                                        Column.forString("Receipt Completed",
                                                                        x -> Optional.ofNullable(
                                                                                        findReceiptCompletedDate(
                                                                                                        x.getCase()))
                                                                                        .map(date -> date
                                                                                                        .format(DateTimeFormatter.ISO_LOCAL_DATE))
                                                                                        .orElse("")),
                                                        Column.forInteger("Receipt Days",
                                                                        x -> x.getCase().getReceiptDaysSpent()),
                                                        Column.forString("Supplemental Only",
                                                                        x -> isSupplementalOnly(
                                                                                        x.getTest(),
                                                                                        x.getRequisition())
                                                                                                        ? "Yes"
                                                                                                        : "No"),
                                                        Column.forString("Extraction Completed",
                                                                        x -> formatLocalDate(
                                                                                        findExtractionCompletedDate(
                                                                                                        x.getCase(),
                                                                                                        x.getTest()))),
                                                        Column.forInteger("Extraction Days",
                                                                        x -> x.getTest().getExtractionDaysSpent()),
                                                        Column.forString("Library Prep. Completed",
                                                                        x -> formatLocalDate(
                                                                                        findLibraryPrepCompletedDate(
                                                                                                        x.getCase(),
                                                                                                        x.getTest()))),
                                                        Column.forInteger("Library Prep. Days",
                                                                        x -> x.getTest().getLibraryPreparationDaysSpent()),
                                                        Column.forString("Library Qual. Completed",
                                                                        x -> formatLocalDate(
                                                                                        findLibraryQualCompletedDate(
                                                                                                        x.getCase(),
                                                                                                        x.getTest()))),
                                                        Column.forInteger("Library Qual. Days",
                                                                        x -> x.getTest().getLibraryQualificationDaysSpent()),
                                                        Column.forString("Full-Depth Completed",
                                                                        x -> formatLocalDate(
                                                                                        findFullDepthCompletedDate(
                                                                                                        x.getCase(),
                                                                                                        x.getTest()))),
                                                        Column.forInteger("Full-Depth Days",
                                                                        x -> x.getTest().getFullDepthSequencingDaysSpent()),
                                                        Column.forString(
                                                                        "Analysis Review Completed",
                                                                        x -> formatLocalDate(
                                                                                        findAnalysisReviewCompletedDate(
                                                                                                        x.completionDate))),
                                                        Column.forInteger("Analysis Review Days",
                                                                        x -> x.getCase().getAnalysisReviewDaysSpent()),
                                                        Column.forString(
                                                                        "Release Approval Completed",
                                                                        x -> formatLocalDate(
                                                                                        findReleaseApprovalCompletedDate(
                                                                                                        x.completionDate))),
                                                        Column.forInteger("Release Approval Days",
                                                                        x -> x.getCase().getReleaseApprovalDaysSpent()),
                                                        Column.forString("Release Completed",
                                                                        x -> formatLocalDate(
                                                                                        findReleaseCompletedDate(
                                                                                                        x.completionDate))),
                                                        Column.forInteger("Release Days",
                                                                        x -> x.getCase().getReleaseDaysSpent()),
                                                        Column.forString("Completion Date",
                                                                        x -> formatLocalDate(
                                                                                        x.completionDate)),
                                                        Column.forInteger("Total Days",
                                                                        x -> calculateDaysBetween(x
                                                                                        .getCase()
                                                                                        .getStartDate(),
                                                                                        x.completionDate)))) {

                                @Override
                                public List<RowData> getData(CaseService caseService,
                                                Map<String, String> parameters) {
                                        Map<Long, Assay> assaysById = caseService.getAssaysById();

                                        return caseService.getFilteredCases(parameters)
                                                        .flatMap(kase -> kase.getTests().stream()
                                                                        .map(test -> {
                                                                                LocalDate completionDate =
                                                                                                calculateCompletionDate(
                                                                                                                test);
                                                                                return new RowData(
                                                                                                kase,
                                                                                                test,
                                                                                                assaysById.get(kase
                                                                                                                .getAssayId()),
                                                                                                kase.getRequisition(),
                                                                                                completionDate);
                                                                        }))
                                                        .collect(Collectors.toList());
                                }

                        };

        public static final CaseReport INSTANCE = new CaseReport();

        private CaseReport() {
                super("Case Report", caseSection);
        }

        private static String formatLocalDate(LocalDate date) {
                return Optional.ofNullable(date)
                                .map(d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                .orElse("");
        }

        private static boolean isSupplementalOnly(Test test, Requisition requisition) {
                if (test == null || test.getExtractions().isEmpty()) {
                        return false;
                }
                return test.getExtractions().stream()
                                .allMatch(sample -> !sample.getRequisitionId()
                                                .equals(requisition.getId()));
        }

        private static LocalDate findReceiptCompletedDate(Case kase) {
                return findLatestCompletionDate(new ArrayList<>(kase.getReceipts()));
        }

        private static LocalDate findExtractionCompletedDate(Case kase, Test test) {
                return findLatestCompletionDate(new ArrayList<>(test.getExtractions()));
        }

        private static LocalDate findLibraryPrepCompletedDate(Case kase, Test test) {
                return findLatestCompletionDate(new ArrayList<>(test.getLibraryPreparations()));
        }

        private static LocalDate findLibraryQualCompletedDate(Case kase, Test test) {
                return findLatestCompletionDate(new ArrayList<>(test.getLibraryQualifications()));
        }

        private static LocalDate findFullDepthCompletedDate(Case kase, Test test) {
                return findLatestCompletionDate(new ArrayList<>(test.getFullDepthSequencings()));
        }

        private static LocalDate findAnalysisReviewCompletedDate(LocalDate completionDate) {
                return completionDate;

        }

        private static LocalDate findReleaseApprovalCompletedDate(LocalDate completionDate) {
                return completionDate;
        }

        private static LocalDate findReleaseCompletedDate(LocalDate completionDate) {
                return completionDate;
        }

        private static LocalDate calculateCompletionDate(Test test) {
                if (test == null) {
                        return null;
                }
                return test.getFullDepthSequencings().stream()
                                .map(Sample::getQcDate)
                                .filter(Objects::nonNull)
                                .max(LocalDate::compareTo)
                                .orElse(null);
        }

        private static LocalDate findLatestCompletionDate(List<Sample> samples) {
                return samples.stream()
                                .map(Sample::getQcDate)
                                .filter(Objects::nonNull)
                                .max(LocalDate::compareTo)
                                .orElse(null);
        }

        private static Integer calculateDaysBetween(LocalDate start, LocalDate end) {
                if (start != null && end != null) {
                        return (int) ChronoUnit.DAYS.between(start, end);
                }
                return null;
        }

}

