package ca.on.oicr.gsi.dimsum;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class CaseLoaderTest {

  @Test
  public void testDateBetween() {
    LocalDate afterDate = LocalDate.of(2024, 1, 25);
    LocalDate beforeDate = LocalDate.of(2024, 1, 27);
    assertFalse(CaseLoader.dateBetween(LocalDate.of(2024, 1, 24), afterDate, beforeDate));
    // afterDate is inclusive
    assertTrue(CaseLoader.dateBetween(LocalDate.of(2024, 1, 25), afterDate, beforeDate));
    assertTrue(CaseLoader.dateBetween(LocalDate.of(2024, 1, 26), afterDate, beforeDate));
    // beforeDate is exclusive
    assertFalse(CaseLoader.dateBetween(LocalDate.of(2024, 1, 27), afterDate, beforeDate));
    assertFalse(CaseLoader.dateBetween(LocalDate.of(2024, 1, 28), afterDate, beforeDate));
  }

}
