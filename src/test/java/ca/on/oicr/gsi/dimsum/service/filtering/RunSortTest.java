package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ca.on.oicr.gsi.cardea.data.Run;

public class RunSortTest {

  private static final String[] namesOrdered = {"Run_A", "Run_B", "Run_C"};
  private static final String[] names = {namesOrdered[1], namesOrdered[2], namesOrdered[0]};
  private static final LocalDate[] datesOrdered = {LocalDate.of(2022, 1, 1),
      LocalDate.of(2022, 1, 2), LocalDate.of(2022, 2, 1)};
  private static final LocalDate[] dates = {datesOrdered[2], datesOrdered[0], datesOrdered[1]};

  @Test
  public void testSortByNameAscending() {
    List<Run> runs = getRunsSorted(RunSort.NAME, false);
    assertOrder(runs, Run::getName, namesOrdered, false);
  }

  @Test
  public void testSortByNameDescending() {
    List<Run> runs = getRunsSorted(RunSort.NAME, true);
    assertOrder(runs, Run::getName, namesOrdered, true);
  }

  @Test
  public void testSortByCompletionDateAscending() {
    List<Run> runs = getRunsSorted(RunSort.COMPLETION_DATE, false);
    assertOrder(runs, Run::getCompletionDate, datesOrdered, false);
  }

  @Test
  public void testSortByCompletionDateDescending() {
    List<Run> runs = getRunsSorted(RunSort.COMPLETION_DATE, true);
    assertOrder(runs, Run::getCompletionDate, datesOrdered, true);
  }

  private static <T> void assertOrder(List<Run> runs, Function<Run, T> getter, T[] expectedOrder,
      boolean reversed) {
    assertNotNull(runs);
    assertEquals(runs.size(), expectedOrder.length);
    assertEquals(expectedOrder[reversed ? 2 : 0], getter.apply(runs.get(0)));
    assertEquals(expectedOrder[reversed ? 1 : 1], getter.apply(runs.get(1)));
    assertEquals(expectedOrder[reversed ? 0 : 2], getter.apply(runs.get(2)));
  }

  private static List<Run> getRunsSorted(RunSort sort, boolean descending) {
    Comparator<Run> comparator = descending ? sort.comparator().reversed() : sort.comparator();
    return IntStream.range(0, 3)
        .mapToObj(RunSortTest::mockRun)
        .sorted(comparator)
        .toList();
  }

  private static Run mockRun(int runNumber) {
    Run run = Mockito.mock(Run.class);
    Mockito.when(run.getName()).thenReturn(names[runNumber]);
    Mockito.when(run.getCompletionDate()).thenReturn(dates[runNumber]);
    return run;
  }

}
