package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.SampleMetric;
import ca.on.oicr.gsi.cardea.data.SampleMetric.MetricLevel;
import ca.on.oicr.gsi.cardea.data.ThresholdType;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.DynamicTableReportSection;

public class SampleMetricsReport extends Report {

  private static final ReportSection<Sample> mainSection =
      new DynamicTableReportSection<Sample>("Samples") {

        private static final List<Column<Sample>> STATIC_COLUMNS = Arrays.asList(
            Column.forString("Sample", Sample::getName),
            Column.forString("Group ID", Sample::getGroupId),
            Column.forString("External Name", x -> x.getDonor().getExternalName()),
            Column.forString("Run", x -> x.getRun() == null ? null : x.getRun().getName()),
            Column.forString("Tissue Origin", Sample::getTissueOrigin),
            Column.forString("Tissue Type", Sample::getTissueType),
            Column.forString("Timepoint", Sample::getTimepoint),
            Column.forString("Design", Sample::getLibraryDesignCode));

        @Override
        public List<Sample> getData(CaseService caseService, JsonNode parameters) {
          String runName = getParameterString(parameters, "runName", false);
          String category = getParameterString(parameters, "category", true);
          CaseFilter baseFilter = getParameterFilter(parameters, "baseFilter");
          List<CaseFilter> filters = getParameterFilters(parameters);
          if (runName == null) {
            // Looking up based on case(s)
            if (Objects.equals(category, MetricCategory.LIBRARY_QUALIFICATION.name())) {
              return caseService.getLibraryQualifications(baseFilter, filters);
            } else if (Objects.equals(category, MetricCategory.FULL_DEPTH_SEQUENCING.name())) {
              return caseService.getFullDepthSequencings(baseFilter, filters);
            } else {
              throw new BadRequestException("Invalid category: " + category);
            }
          } else {
            // Looking up based on run
            if (Objects.equals(category, MetricCategory.LIBRARY_QUALIFICATION.name())) {
              return caseService.getLibraryQualificationsForRun(runName, filters);
            } else if (Objects.equals(category, MetricCategory.FULL_DEPTH_SEQUENCING.name())) {
              return caseService.getFullDepthSequencingsForRun(runName, filters);
            } else {
              throw new BadRequestException("Invalid category: " + category);
            }
          }
        }

        @Override
        public List<Column<Sample>> getColumns(List<Sample> data) {
          Stream<Column<Sample>> metricColumns = data.stream()
              .flatMap(sample -> sample.getMetrics().stream()
                  // Exclude boolean metrics (no value)
                  .filter(metric -> metric.getThresholdType() != ThresholdType.BOOLEAN)
                  // Exclude run/lane-level metrics (multiple values)
                  .filter(metric -> metric.getMetricLevel() == MetricLevel.SAMPLE)
                  .map(SampleMetric::getName))
              .distinct()
              .map(metricName -> makeMetricColumn(metricName));

          return Stream.concat(STATIC_COLUMNS.stream(), metricColumns).toList();
        }

        private static Column<Sample> makeMetricColumn(String metricName) {
          return Column.forDecimal(metricName, sample -> {
            SampleMetric metric = sample.getMetrics().stream()
                .filter(x -> Objects.equals(x.getName(), metricName)
                    && !Boolean.TRUE.equals(x.getPreliminary()))
                .findAny().orElse(null);
            return metric == null ? null : metric.getValue();
          });
        }
      };

  public static final SampleMetricsReport INSTANCE = new SampleMetricsReport();

  private SampleMetricsReport() {
    super("Metrics Report", mainSection);
  }

}
