package ca.on.oicr.gsi.dimsum.util.reporting;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
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

    @Override
    public void writeDelimitedText(StringBuilder sb, List<T> objects, String delimiter,
        boolean includeHeaders) {
      List<Column<T>> columns = getColumns();
      if (includeHeaders) {
        for (int i = 0; i < columns.size(); i++) {
          if (i > 0) {
            sb.append(delimiter);
          }
          sb.append(columns.get(i).getTitle());
        }
      }
      for (T object : objects) {
        for (int i = 0; i < columns.size(); i++) {
          if (i > 0) {
            sb.append(delimiter);
          }
          sb.append(columns.get(i).getDelimitedColumnString(delimiter, object));
        }
        sb.append("\r\n");
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
      JsonNode parameters) {
    List<T> objects = getData(caseService, parameters);
    XSSFSheet worksheet = workbook.createSheet(getTitle());
    writeExcelSheet(worksheet, objects);
  }

  protected abstract void writeExcelSheet(XSSFSheet worksheet, List<T> objects);

  public void createDelimitedText(StringBuilder sb, CaseService caseService,
      String delimiter, boolean includeHeadings, JsonNode parameters) {
    List<T> objects = getData(caseService, parameters);
    writeDelimitedText(sb, objects, delimiter, includeHeadings);
  }

  protected abstract void writeDelimitedText(StringBuilder sb, List<T> objects, String delimiter,
      boolean includeHeaders);

  /**
   * Fetches data from the CaseService based on parameters provided
   * 
   * @param caseService
   * @param parameters options provided from front-end; must be validated
   * @return
   * 
   * @throws BadRequestException if there are invalid parameters
   */
  public abstract List<T> getData(CaseService caseService, JsonNode parameters);

  protected static Set<String> getParameterStringSet(JsonNode parameters, String name) {
    JsonNode valueNode = parameters.get(name);
    String value = (valueNode != null) ? valueNode.asText() : null;
    if (value == null || value.isEmpty()) {
      return null;
    }
    return Stream.of(value.split("\\s*,\\s*")).collect(Collectors.toSet());
  }

}
