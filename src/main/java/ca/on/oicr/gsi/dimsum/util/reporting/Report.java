package ca.on.oicr.gsi.dimsum.util.reporting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ca.on.oicr.gsi.dimsum.service.CaseService;

public abstract class Report {

  private final String title;
  private final List<ReportSection<?>> sections;

  public Report(String title, ReportSection<?>... sections) {
    this.title = title;
    this.sections = Collections.unmodifiableList(Arrays.asList(sections));
  }

  public String getTitle() {
    return title;
  }

  public XSSFWorkbook writeExcelFile(CaseService caseService, Map<String, String> parameters) {
    XSSFWorkbook workbook = new XSSFWorkbook();
    for (ReportSection<?> section : sections) {
      section.createExcelSheet(workbook, caseService, parameters);
    }
    return workbook;
  }

}
