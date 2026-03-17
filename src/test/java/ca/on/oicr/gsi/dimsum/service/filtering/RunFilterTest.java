package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.cardea.data.Run;

public class RunFilterTest {


  private static List<Run> runs =
      Arrays.asList(
          makeRun("RUN1", LocalDate.of(2025, 11, 22)),
          makeRun("RUN2", LocalDate.of(2026, 1, 3)),
          makeRun("RUN3", LocalDate.of(2026, 1, 15)),
          makeRun("RUN4", LocalDate.of(2026, 2, 1)),
          makeRun("RUN5", LocalDate.of(2026, 2, 4)));

  @Test
  public void testFilterName() {
    RunFilter filter = new RunFilter(RunFilterKey.NAME, "RUN1");
    testFilter(filter, Arrays.asList("RUN1"));
  }

  @Test
  public void testFilterCompletedBefore() {
    RunFilter filter = new RunFilter(RunFilterKey.COMPLETED_BEFORE, "2026-01-15");
    testFilter(filter, Arrays.asList("RUN1", "RUN2"));
  }

  @Test
  public void testFilterCompletedAfter() {
    RunFilter filter = new RunFilter(RunFilterKey.COMPLETED_AFTER, "2026-01-15");
    testFilter(filter, Arrays.asList("RUN4", "RUN5"));
  }

  private static void testFilter(RunFilter filter, List<String> expectedNames) {
    List<Run> filtered = runs.stream().filter(filter.predicate()).toList();
    for (String expectedName : expectedNames) {
      assertTrue(filtered.stream().anyMatch(x -> x.getName().equals(expectedName)),
          "Run %s included".formatted(expectedName));
    }
    assertEquals(expectedNames.size(), filtered.size(), "Run count");
  }

  private static Run makeRun(String name, LocalDate completedDate) {
    Run run = mock(Run.class);
    when(run.getName()).thenReturn(name);
    when(run.getCompletionDate()).thenReturn(completedDate);
    return run;
  }
}
