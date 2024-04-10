package ca.on.oicr.gsi.dimsum.util.reporting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;

public abstract class Report {

  private static final String PARAM_FORMAT = "format";
  private static final String PARAM_HEADINGS = "includeHeadings";

  private final String title;
  private final List<ReportSection<?>> sections;

  public Report(String title, ReportSection<?>... sections) {
    this.title = title;
    this.sections = Collections.unmodifiableList(Arrays.asList(sections));
  }

  public String getTitle() {
    return title;
  }

  public static ReportFormat getFormat(JsonNode parameters) {
    String format = parameters.has(PARAM_FORMAT) ? parameters.get(PARAM_FORMAT).asText() : null;
    if (format == null) {
      return ReportFormat.EXCEL;
    }
    switch (format) {
      case "excel":
        return ReportFormat.EXCEL;
      case "csv":
        return ReportFormat.CSV;
      case "tsv":
        return ReportFormat.TSV;
      default:
        throw new BadRequestException("Invalid report format: " + format);
    }
  }

  public byte[] writeFile(CaseService caseService, JsonNode parameters)
      throws IOException {
    ReportFormat format = getFormat(parameters);

    switch (format) {
      case EXCEL:
        return writeExcelFile(caseService, parameters);
      case CSV:
        return writeDelimitedFile(caseService, ",", parameters);
      case TSV:
        return writeDelimitedFile(caseService, "\t", parameters);
      default:
        throw new BadRequestException("Invalid download format: " + format);
    }
  }

  private byte[] writeExcelFile(CaseService caseService, JsonNode parameters)
      throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    for (ReportSection<?> section : sections) {
      section.createExcelSheet(workbook, caseService, parameters);
    }
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      workbook.write(output);
      return output.toByteArray();
    }
  }

  private byte[] writeDelimitedFile(CaseService caseService, String delimiter,
      JsonNode parameters) {
    StringBuilder sb = new StringBuilder();
    boolean includeHeadings =
        parameters.has(PARAM_HEADINGS) && parameters.get(PARAM_HEADINGS).asBoolean();
    // This does not support multiple sections
    sections.get(0).createDelimitedText(sb, caseService, delimiter, includeHeadings, parameters);
    return sb.toString().getBytes();
  }

}
