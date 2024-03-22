package ca.on.oicr.gsi.dimsum.util.reporting;

import java.math.BigDecimal;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.Cell;

public abstract class Column<T> {

  public static <T> Column<T> forString(String title, Function<T, String> getter) {
    return new Column<T>(title) {

      @Override
      public void writeExcelCell(Cell cell, T object) {
        String value = getter.apply(object);
        cell.setCellValue(value);
      }

    };
  }

  public static <T> Column<T> forDecimal(String title, Function<T, BigDecimal> getter) {
    return new Column<T>(title) {

      @Override
      public void writeExcelCell(Cell cell, T object) {
        BigDecimal value = getter.apply(object);
        if (value != null) {
          cell.setCellValue(value.doubleValue());
        }
      }

    };
  }

  public static <T> Column<T> forInteger(String title, Function<T, Integer> getter) {
    return new Column<T>(title) {

      @Override
      public void writeExcelCell(Cell cell, T object) {
        Integer value = getter.apply(object);
        if (value != null) {
          cell.setCellValue(value.doubleValue());
        }
      }
    };
  }

  private final String title;

  public Column(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public abstract void writeExcelCell(Cell cell, T object);

}
