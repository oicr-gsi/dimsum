package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
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

    private static final ReportSection<RowData> caseSection = new TableReportSection<>(
            "Case Report",
            Arrays.asList(
                    Column.forString("Case ID", x -> x.getCase().getId()),
                    Column.forString("Projects",
                            x -> getSortedProjectNameAndPipeline(x.getCase()).stream()
                                    .map(str -> str.split(" - ")[0])
                                    .collect(Collectors.joining(", "))),
                    Column.forString("Pipeline",
                            x -> getSortedProjectNameAndPipeline(x.getCase()).stream()
                                    .map(str -> str.split(" - ")[1])
                                    .collect(Collectors.joining(", "))),
                    Column.forString("Requisition", x -> x.getCase().getRequisition().getName()),
                    Column.forString("Assay", x -> x.getCase().getAssayName()),
                    Column.forString("Start Date",
                            x -> x.getCase().getStartDate()
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE)),
                    Column.forString("Receipt Completed Date", x -> Optional
                            .ofNullable(findLatestCompletionDate(x.getCase().getReceipts()))
                            .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .orElse("")),
                    Column.forInteger("Receipt Days", x -> x.getCase().getReceiptDaysSpent()),
                    Column.forString("Test", x -> x.getTest().getName()),
                    Column.forString("Supplemental Only",
                            x -> isSupplementalOnly(x.getTest(), x.getCase().getRequisition())
                                    ? "Yes"
                                    : "No"),
                    Column.forString("Extraction Completed Date", x -> Optional
                            .ofNullable(findLatestCompletionDate(x.getTest().getExtractions()))
                            .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .orElse("")),
                    Column.forInteger("Extraction Days", x -> x.getTest().getExtractionDaysSpent()),
                    Column.forString("Library Prep Completed Date", x -> Optional.ofNullable(
                            findLatestCompletionDate(x.getTest().getLibraryPreparations()))
                            .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .orElse("")),
                    Column.forInteger("Library Prep. Days",
                            x -> x.getTest().getLibraryPreparationDaysSpent()),
                    Column.forString("Library Qual Completed Date", x -> Optional.ofNullable(
                            findLatestCompletionDate(x.getTest().getLibraryQualifications()))
                            .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .orElse("")),
                    Column.forInteger("Library Qual. Days",
                            x -> x.getTest().getLibraryQualificationDaysSpent()),
                    Column.forString("Full Depth Completed Date", x -> Optional
                            .ofNullable(
                                    findLatestCompletionDate(x.getTest().getFullDepthSequencings()))
                            .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .orElse("")),
                    Column.forInteger("Full-Depth Days",
                            x -> x.getTest().getFullDepthSequencingDaysSpent()),
                    Column.forString("Analysis Review Completed",
                            x -> x.getCase().getDeliverables().stream()
                                    .map(CaseDeliverable::getAnalysisReviewQcDate)
                                    .filter(Objects::nonNull)
                                    .max(LocalDate::compareTo)
                                    .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    .orElse("")),
                    Column.forInteger("Analysis Review Days",
                            x -> x.getCase().getAnalysisReviewDaysSpent()),
                    Column.forString("Release Approval Completed",
                            x -> x.getCase().getDeliverables().stream()
                                    .map(CaseDeliverable::getReleaseApprovalQcDate)
                                    .filter(Objects::nonNull)
                                    .max(LocalDate::compareTo)
                                    .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    .orElse("")),
                    Column.forInteger("Release Approval Days",
                            x -> x.getCase().getReleaseApprovalDaysSpent()),
                    Column.forString("Completion Date", x -> getCompletionDate(x.getCase())),
                    Column.forInteger("Total Days", x -> x.getCase().getCaseDaysSpent()))) {
        @Override
        public List<RowData> getData(CaseService caseService, Map<String, String> parameters) {
            List<CaseFilter> filters = convertParametersToFilters(parameters);
            return caseService.getCaseStream(filters)
                    .flatMap(kase -> kase.getTests().stream()
                            .map(test -> new RowData(kase, test)))
                    .collect(Collectors.toList());
        }
    };

    public static final CaseTatReport INSTANCE = new CaseTatReport();

    private CaseTatReport() {
        super("Case TAT Report", caseSection);
    }

    private static Collection<String> getSortedProjectNameAndPipeline(Case case1) {
        return case1.getProjects()
                .stream()
                .map(project -> project.getName() + " - " + project.getPipeline())
                .sorted(Comparator
                        .comparing(str -> !str.contains("Accredited with Clinical Report")))
                .collect(Collectors.toList());
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
                .allMatch(sample -> !sample.getRequisitionId().equals(requisition.getId()));
    }

    private static LocalDate findLatestCompletionDate(List<Sample> samples) {
        return samples.stream()
                .filter(SampleUtils::isPassed)
                .map(sample -> {
                    if (sample.getRun() != null) {
                        LocalDate sampleReviewDate = sample.getDataReviewDate();
                        LocalDate runReviewDate = sample.getRun().getDataReviewDate();
                        return Stream.of(sampleReviewDate, runReviewDate)
                                .filter(Objects::nonNull)
                                .max(LocalDate::compareTo)
                                .orElse(null);
                    } else {
                        return sample.getQcDate();
                    }
                })
                .filter(Objects::nonNull)
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
        List<CaseRelease> releases = deliverables.get(0).getReleases();
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
