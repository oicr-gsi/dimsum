package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Metric;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.MetricSubcategory;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.CompletedGate;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.TableReportSection;

public class TglTrackingReport extends Report {

  private static final String METRIC_COVERAGE = "Mean Coverage Deduplicated";
  private static final String METRIC_CLUSTERS = "Pipeline Filtered Clusters";

  private static class RowData {

    private final Case kase;
    private final Test test;
    private final Assay assay;

    public RowData(Case kase, Test test, Assay assay) {
      this.kase = kase;
      this.test = test;
      this.assay = assay;
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
  }

  private static final ReportSection<RowData> trackerSection =
      new TableReportSection<RowData>("Tracker",
          Arrays.asList(
              Column.forString("Case ID", x -> x.getCase().getId()),
              Column.forString("External Name", x -> x.getCase().getDonor().getExternalName()),
              Column.forString("Test", x -> x.getTest().getName()),
              Column.forString("Tissue Type", x -> x.getTest().getTissueType()),
              Column.forString("Group ID", x -> x.getTest().getGroupId()),
              Column.forString("Extraction Pass?",
                  x -> CompletedGate.EXTRACTION.qualifyTest(x.getTest()) ? "Yes" : null),
              Column.forString("Stock ID", x -> x.getTest().getExtractions().stream()
                  .map(Sample::getId).collect(Collectors.joining(", "))),
              Column.forString("Stock Name", x -> x.getTest().getExtractions().stream()
                  .map(Sample::getName).collect(Collectors.joining(", "))),
              Column.forString("Library Aliquot ID",
                  x -> Stream
                      .concat(x.getTest().getLibraryQualifications().stream(),
                          x.getTest().getFullDepthSequencings().stream())
                      .map(Sample::getId).collect(Collectors.joining(", "))),
              Column.forString("Library Aliquot Name",
                  x -> Stream
                      .concat(x.getTest().getLibraryQualifications().stream(),
                          x.getTest().getFullDepthSequencings().stream())
                      .map(Sample::getName).collect(Collectors.joining(", "))),
              Column.forString("Library Qualification Runs",
                  x -> x.getTest().getLibraryQualifications().stream()
                      .filter(sample -> sample.getRun() != null)
                      .map(runlib -> runlib.getRun().getName())
                      .collect(Collectors.joining(", "))),
              Column.forString("Library Qualification Pass?",
                  x -> CompletedGate.LIBRARY_QUALIFICATION.qualifyTest(x.getTest()) ? "Yes"
                      : null),
              Column.forString("Full-Depth Runs",
                  x -> x.getTest().getFullDepthSequencings().stream()
                      .map(runlib -> runlib.getRun().getName())
                      .collect(Collectors.joining(", "))),
              Column.forDecimal("Coverage Required", TglTrackingReport::getCoverageRequired),
              Column.forDecimal("Coverage Achieved", TglTrackingReport::getCoverageAchieved),
              Column.forString("Case Status", x -> getCaseStatus(x.getCase())))) {

        @Override
        public List<RowData> getData(CaseService caseService,
            JsonNode parameters) {
          Set<String> projectNames = getParameterStringSet(parameters, "projects");
          List<CaseFilter> filters = null;
          if (projectNames != null) {
            filters = projectNames.stream()
                .map(project -> new CaseFilter(CaseFilterKey.PROJECT, project))
                .toList();
          }

          Map<Long, Assay> assaysById = caseService.getAssaysById();

          return caseService.getCaseStream(filters)
              .flatMap(kase -> kase.getTests().stream()
                  .map(test -> new RowData(kase, test, assaysById.get(kase.getAssayId()))))
              .toList();
        }
      };

  public static final TglTrackingReport INSTANCE = new TglTrackingReport();

  private TglTrackingReport() {
    super("TGL Tracking Sheet", trackerSection);
  }

  private static String getCaseStatus(Case kase) {
    if (CompletedGate.RELEASE.qualifyCase(kase)) {
      return "Completed";
    } else if (kase.isStopped()) {
      return "Failed (%s)".formatted(
          kase.getRequisition().getStopReason() != null ? kase.getRequisition().getStopReason()
              : "Reason Unspecified");
    } else {
      return "In Progress";
    }
  }

  private static BigDecimal getCoverageRequired(RowData rowData) {
    Metric metric = getCoverageMetric(rowData.getAssay(), rowData.getTest());
    if (metric == null) {
      return null;
    }
    return metric.getMinimum();
  }

  private static BigDecimal getCoverageAchieved(RowData rowData) {
    Metric metric = getCoverageMetric(rowData.getAssay(), rowData.getTest());
    if (metric == null) {
      return null;
    }
    switch (metric.getName()) {
      case METRIC_COVERAGE:
        return getMetricValue(rowData.getTest(), Sample::getMeanCoverageDeduplicated);
      case METRIC_CLUSTERS:
        return getMetricValue(rowData.getTest(),
            sample -> sample.getClustersPerSample() == null ? null
                : new BigDecimal(sample.getClustersPerSample()));
      default:
        throw new IllegalArgumentException("Invalid metric: " + metric.getName());
    }
  }

  private static BigDecimal getMetricValue(Test test, Function<Sample, BigDecimal> getter) {
    return test.getFullDepthSequencings().stream()
        .map(getter)
        .filter(Objects::nonNull)
        .max(BigDecimal::compareTo)
        .orElse(null);
  }

  private static Metric getCoverageMetric(Assay assay, Test test) {
    List<MetricSubcategory> fullDepthSubcategories =
        assay.getMetricCategories().get(MetricCategory.FULL_DEPTH_SEQUENCING);
    if (fullDepthSubcategories == null) {
      return null;
    }
    Metric value = null;
    for (MetricSubcategory subcategory : fullDepthSubcategories) {
      if (subcategory.getLibraryDesignCode() != null
          && !subcategory.getLibraryDesignCode().equals(test.getLibraryDesignCode())) {
        continue;
      }
      for (Metric metric : subcategory.getMetrics()) {
        if (metric.getTissueType() != null
            && (metric.isNegateTissueType() == metric.getTissueType()
                .equals(test.getTissueType()))) {
          continue;
        }
        if (metric.getTissueOrigin() != null
            && !metric.getTissueOrigin().equals(test.getTissueOrigin())) {
          continue;
        }
        if (METRIC_COVERAGE.equals(metric.getName())) {
          return metric; // always prefer this metric
        } else if (METRIC_CLUSTERS.equals(metric.getName())) {
          value = metric; // return if there is no coverage metric
        }
      }
    }
    return value;
  }

}
