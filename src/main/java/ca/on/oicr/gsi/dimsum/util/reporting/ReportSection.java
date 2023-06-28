package ca.on.oicr.gsi.dimsum.util.reporting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ca.on.oicr.gsi.dimsum.service.CaseService;

public abstract class ReportSection<T> {

  public static abstract class TableReportSection<T> extends ReportSection<T> {

    public TableReportSection(String title, List<Column<T>> columns) {
      super(title, columns);
    }

    @Override
    public void writeExcelSheet(XSSFSheet worksheet, List<T> objects) {
      int row = 0;
      Row headRow = worksheet.createRow(row++);
      List<Column<T>> columns = getColumns();
      for (int i = 0; i < columns.size(); i++) {
        Column<T> column = columns.get(i);
        Cell cell = headRow.createCell(i);
        cell.setCellValue(column.getTitle());
      }

      for (T object : objects) {
        Row dataRow = worksheet.createRow(row++);
        for (int i = 0; i < columns.size(); i++) {
          Column<T> column = columns.get(i);
          Cell cell = dataRow.createCell(i);
          column.writeExcelCell(cell, object);
        }
      }
    }

  }

  private final String title;
  private final List<Column<T>> columns;

  public ReportSection(String title, List<Column<T>> columns) {
    this.title = title;
    this.columns = Collections.unmodifiableList(columns);
  }

  public String getTitle() {
    return title;
  }

  public List<Column<T>> getColumns() {
    return columns;
  }

  public void createExcelSheet(XSSFWorkbook workbook, CaseService caseService,
      Map<String, String> parameters) {
    List<T> objects = getData(caseService, parameters);
    XSSFSheet worksheet = workbook.createSheet(getTitle());
    writeExcelSheet(worksheet, objects);
  }

  protected abstract void writeExcelSheet(XSSFSheet worksheet, List<T> objects);

  /**
   * Fetches data from the CaseService based on parameters provided
   * 
   * @param caseService
   * @param parameters options provided from front-end; must be validated
   * @return
   * 
   * @throws BadRequestException if there are invalid parameters
   */
  public abstract List<T> getData(CaseService caseService, Map<String, String> parameters);

  protected static Set<String> getParameterStringSet(Map<String, String> parameters, String name) {
    String value = parameters.get(name);
    if (value == null || value.isEmpty()) {
      return null;
    }
    return Stream.of(value.split("\\s*,\\s*")).collect(Collectors.toSet());
  }

}
