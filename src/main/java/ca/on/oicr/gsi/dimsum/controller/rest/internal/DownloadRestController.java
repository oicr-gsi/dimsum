package ca.on.oicr.gsi.dimsum.controller.rest.internal;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.ControllerUtils;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.CaseSummaryReport;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.CaseTatReport;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.DareInputSheet;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.DonorAssayReport;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.FullDepthSummary;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.SampleMetricsReport;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.TglTrackingReport;

@RestController
@RequestMapping("/rest/internal/downloads")
public class DownloadRestController {

  @Autowired
  private CaseService caseService;

  @Autowired
  private ObjectMapper objectMapper;

  @PostMapping("/reports/{reportName}")
  public HttpEntity<byte[]> generateReport(@PathVariable String reportName,
      @RequestBody JsonNode parameters) throws IOException {
    Report report = getReport(reportName);
    return ControllerUtils.generateReport(reportName, report, parameters, caseService);
  }

  @PostMapping("/reports/{reportName}/data")
  public JsonNode getReportData(@PathVariable String reportName, @RequestBody JsonNode parameters) {
    Report report = getReport(reportName);
    return report.getData(caseService, parameters, objectMapper); // Return JSON directly
  }

  private static Report getReport(String reportName) {
    switch (reportName) {
      case "tgl-tracking-sheet":
        return TglTrackingReport.INSTANCE;
      case "full-depth-summary":
        return FullDepthSummary.INSTANCE;
      case "dare-input-sheet":
        return DareInputSheet.INSTANCE;
      case "case-tat-report":
        return CaseTatReport.INSTANCE;
      case "donor-assay-report":
        return DonorAssayReport.INSTANCE;
      case "sample-metrics":
        return SampleMetricsReport.INSTANCE;
      case "case-summary-report":
        return CaseSummaryReport.INSTANCE;
      default:
        throw new BadRequestException("Invalid report name: " + reportName);
    }
  }
}
