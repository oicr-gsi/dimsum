package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;

public class ProjectSummaryFilterTest {

  private static List<ProjectSummary> projectSummaries = Arrays.asList(makeProjectSummary("PROJ1"),
      makeProjectSummary("PROJ2"), makeProjectSummary("PROJ3"), makeProjectSummary("PROJ4"),
      makeProjectSummary("PROJ5"));

  @Test
  public void testFilterName() {
    ProjectSummaryFilter filter = new ProjectSummaryFilter(ProjectSummaryFilterKey.NAME, "PROJ1");
    testFilter(filter, Arrays.asList("PROJ1"));
  }

  private static void testFilter(ProjectSummaryFilter filter, List<String> expectedNames) {
    List<ProjectSummary> filtered = projectSummaries.stream().filter(filter.predicate()).toList();
    for (String expectedName : expectedNames) {
      assertTrue(filtered.stream().anyMatch(x -> x.getName().equals(expectedName)),
          "Project %s included".formatted(expectedName));
    }
    assertEquals(expectedNames.size(), filtered.size(), "Project Summary count");
  }

  private static ProjectSummary makeProjectSummary(String name) {
    ProjectSummary projectSummary = mock(ProjectSummary.class);
    when(projectSummary.getName()).thenReturn(name);
    return projectSummary;
  }
}
