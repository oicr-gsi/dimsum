package ca.on.oicr.gsi.dimsum.controller.rest;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportFormat;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.CaseTatReport;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.DareInputSheet;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.FullDepthSummary;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.TglTrackingReport;

@RestController
@RequestMapping("/rest/downloads")
public class DownloadRestController {

  @Autowired
  private CaseService caseService;

  @Autowired
  private ObjectMapper objectMapper; // Autowire ObjectMapper

  @PostMapping("/reports/{reportName}")
  public HttpEntity<byte[]> generateReport(@PathVariable String reportName,
      @RequestBody JsonNode parameters) throws IOException {

    ReportFormat format = Report.getFormat(parameters);
    Report report = getReport(reportName);
    byte[] bytes = report.writeFile(caseService, parameters);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(format.getMediaType());
    headers.set("Content-Disposition",
        "attachment; filename=" + String.format("%s-%s.%s", reportName,
            DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now()), format.getExtension()));

    return new HttpEntity<>(bytes, headers);
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
      default:
        throw new BadRequestException("Invalid report name");
    }
  }
}
