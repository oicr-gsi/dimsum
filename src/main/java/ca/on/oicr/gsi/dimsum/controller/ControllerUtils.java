package ca.on.oicr.gsi.dimsum.controller;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportFormat;

public class ControllerUtils {

  public static HttpEntity<byte[]> generateReport(String reportName, Report report,
      JsonNode parameters,
      CaseService caseService) throws IOException {
    ReportFormat format = Report.getFormat(parameters);
    byte[] bytes = report.writeFile(caseService, parameters);

    String filename = String.format("%s-%s.%s", reportName,
        DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now()), format.getExtension());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(format.getMediaType());
    headers.setContentLength(bytes.length);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

    return new HttpEntity<>(bytes, headers);
  }

}
