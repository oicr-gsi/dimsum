package ca.on.oicr.gsi.dimsum.controller.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.DareInputSheet;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.FullDepthSummary;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.TglTrackingReport;

@RestController
@RequestMapping("/rest/downloads")
public class DownloadRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping("/reports/{reportName}")
  public HttpEntity<byte[]> generateReport(@PathVariable String reportName,
      @RequestBody Map<String, String> parameters, HttpServletResponse response)
      throws IOException {

    Report report = getReport(reportName);
    XSSFWorkbook workbook = report.writeExcelFile(caseService, parameters);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType("application", "vnd.ms-excel"));
    response.setHeader("Content-Disposition",
        "attachment; filename="
            + String.format("%s-%s.xlsx", reportName, DateTimeFormatter.ISO_LOCAL_DATE.format(
                ZonedDateTime.now())));

    byte[] bytes = getReportBytes(workbook);
    return new HttpEntity<byte[]>(bytes, headers);
  }

  private static Report getReport(String reportName) {
    switch (reportName) {
      case "tgl-tracking-sheet":
        return TglTrackingReport.INSTANCE;
      case "full-depth-summary":
        return FullDepthSummary.INSTANCE;
      case "dare-input-sheet":
        return DareInputSheet.INSTANCE;
      default:
        throw new BadRequestException("Invalid report name");
    }
  }

  private static byte[] getReportBytes(XSSFWorkbook workbook) throws IOException {
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      workbook.write(output);
      return output.toByteArray();
    }
  }

}
