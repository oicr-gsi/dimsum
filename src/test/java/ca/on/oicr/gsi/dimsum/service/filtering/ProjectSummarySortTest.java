package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;

public class ProjectSummarySortTest {
  private static final String[] namesOrdered = {"Project_A", "Project_B", "Project_C"};
  private static final String[] projectNames = {namesOrdered[1], namesOrdered[2], namesOrdered[0]};

  @Test
  public void testSortByNameAscending() {
    List<ProjectSummary> projectSummaries =
        getProjectSummariesSorted(ProjectSummarySort.NAME, false);
    assertOrder(projectSummaries, ProjectSummary::getName, namesOrdered, false);
  }

  @Test
  public void testSortByNameDescending() {
    List<ProjectSummary> projectSummaries =
        getProjectSummariesSorted(ProjectSummarySort.NAME, true);
    assertOrder(projectSummaries, ProjectSummary::getName, namesOrdered, true);
  }

  private static List<ProjectSummary> getProjectSummariesSorted(ProjectSummarySort sort,
      boolean descending) {
    Comparator<ProjectSummary> comparator =
        descending ? sort.comparator().reversed() : sort.comparator();
    return IntStream.range(0, 3)
        .mapToObj(ProjectSummarySortTest::mockProjectSummary)
        .sorted(comparator)
        .toList();
  }

  private static ProjectSummary mockProjectSummary(int projectSummaryNumber) {
    ProjectSummary projectSummary = Mockito.mock(ProjectSummary.class);
    Mockito.when(projectSummary.getName()).thenReturn(projectNames[projectSummaryNumber]);
    return projectSummary;
  }

  private static <T> void assertOrder(List<ProjectSummary> projectSummaries,
      Function<ProjectSummary, T> getter, T[] expectedOrder,
      boolean reversed) {
    assertNotNull(projectSummaries);
    assertEquals(projectSummaries.size(), expectedOrder.length);
    assertEquals(expectedOrder[reversed ? 2 : 0], getter.apply(projectSummaries.get(0)));
    assertEquals(expectedOrder[reversed ? 1 : 1], getter.apply(projectSummaries.get(1)));
    assertEquals(expectedOrder[reversed ? 0 : 2], getter.apply(projectSummaries.get(2)));
  }

}
