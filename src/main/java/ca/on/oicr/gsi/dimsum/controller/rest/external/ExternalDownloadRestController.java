package ca.on.oicr.gsi.dimsum.controller.rest.external;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.ControllerUtils;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.CaseSummaryReport;

@RestController
@RequestMapping("/rest/external/downloads")
public class ExternalDownloadRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping("/reports/{reportName}")
  public HttpEntity<byte[]> generateReport(@PathVariable String reportName,
      @RequestBody JsonNode parameters) throws IOException {
    Report report = getReport(reportName);
    return ControllerUtils.generateReport(reportName, report, parameters, caseService);
  }

  private static Report getReport(String reportName) {
    switch (reportName) {
      case "case-summary-report":
        return CaseSummaryReport.INSTANCE;
      default:
        throw new BadRequestException("Invalid report name: " + reportName);
    }
  }

}
