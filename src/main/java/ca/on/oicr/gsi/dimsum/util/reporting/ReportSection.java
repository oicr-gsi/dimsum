package ca.on.oicr.gsi.dimsum.util.reporting;

import java.util.ArrayList;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;

public abstract class ReportSection<T> {

  public static abstract class DynamicTableReportSection<T> extends ReportSection<T> {

    public DynamicTableReportSection(String title) {
      super(title);
    }

    public abstract List<Column<T>> getColumns(List<T> data);

    @Override
    public void writeExcelSheet(XSSFSheet worksheet, List<T> objects) {
      int row = 0;
      Row headRow = worksheet.createRow(row++);
      List<Column<T>> columns = getColumns(objects);
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
      List<Column<T>> columns = getColumns(objects);
      if (includeHeaders) {
        for (int i = 0; i < columns.size(); i++) {
          if (i > 0) {
            sb.append(delimiter);
          }
          sb.append(columns.get(i).getTitle());
        }
        sb.append("\r\n");
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

    @Override
    public void writeJson(ArrayNode arrayNode, List<T> objects, ObjectMapper objectMapper) {
      List<Column<T>> columns = getColumns(objects);
      for (T object : objects) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        for (Column<T> column : columns) {
          String value = column.getDelimitedColumnString(",", object).replaceAll("\"", "");
          objectNode.put(column.getTitle(), value);
        }
        arrayNode.add(objectNode);
      }
    }
  }

  public static abstract class StaticTableReportSection<T> extends DynamicTableReportSection<T> {

    private final List<Column<T>> columns;

    public StaticTableReportSection(String title, List<Column<T>> columns) {
      super(title);
      this.columns = Collections.unmodifiableList(columns);
    }

    @Override
    public List<Column<T>> getColumns(List<T> data) {
      return columns;
    }

  }

  private final String title;

  public ReportSection(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
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

  public void createJson(ArrayNode json, CaseService caseService, ObjectMapper objectMapper,
      JsonNode parameters) {
    List<T> objects = getData(caseService, parameters);
    writeJson(json, objects, objectMapper);
  }

  protected abstract void writeJson(ArrayNode json, List<T> objects, ObjectMapper objectMapper);

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

  protected static Set<String> getParameterStringSet(JsonNode parameters, String key) {
    JsonNode valueNode = parameters.get(key);
    String value = (valueNode != null) ? valueNode.asText() : null;
    if (value == null || value.isEmpty()) {
      return null;
    }
    return Stream.of(value.split("\\s*,\\s*")).collect(Collectors.toSet());
  }

  protected static String getParameterString(JsonNode parameters, String key, boolean required) {
    JsonNode node = parameters.get(key);
    if (node == null || node.isNull()) {
      if (required) {
        throw new BadRequestException(key + " parameter missing");
      }
      return null;
    }
    return node.asText();
  }

  protected static CaseFilter getParameterFilter(JsonNode parameters, String key) {
    JsonNode node = parameters.get(key);
    if (node == null || node.isNull()) {
      return null;
    }
    return parseCaseFilter(node);
  }

  protected static List<CaseFilter> getParameterFilters(JsonNode parameters) {
    List<CaseFilter> filters = new ArrayList<>();
    JsonNode filtersParam = parameters.get("filters");
    if (filtersParam.isArray()) {
      for (JsonNode filterParam : filtersParam) {
        filters.add(parseCaseFilter(filterParam));
      }
    }
    return filters;
  }

  private static CaseFilter parseCaseFilter(JsonNode node) {
    String key = node.get("key").asText();
    String value = node.get("value").asText();
    return MvcUtils.parseCaseFilter(key, value);
  }
}
