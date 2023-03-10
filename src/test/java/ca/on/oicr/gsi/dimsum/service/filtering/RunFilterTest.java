package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.dimsum.data.Run;

public class RunFilterTest {


  private static List<Run> runs =
      Arrays.asList(makeRun("RUN1"),
          makeRun("RUN2"), makeRun("RUN3"), makeRun("RUN4"),
          makeRun("RUN5"));

  @Test
  public void testFilterName() {
    RunFilter filter = new RunFilter(RunFilterKey.NAME, "RUN1");
    testFilter(filter, Arrays.asList("RUN1"));
  }

  private static void testFilter(RunFilter filter, List<String> expectedNames) {
    List<Run> filtered = runs.stream().filter(filter.predicate()).toList();
    for (String expectedName : expectedNames) {
      assertTrue(filtered.stream().anyMatch(x -> x.getName().equals(expectedName)),
          "Run %s included".formatted(expectedName));
    }
    assertEquals(expectedNames.size(), filtered.size(), "Run count");
  }

  private static Run makeRun(String name) {
    Run run = mock(Run.class);
    when(run.getName()).thenReturn(name);
    return run;
  }
}
