package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;

public class ProjectSummaryFilterTest {

  private static List<ProjectSummary> projectSummaries = Arrays.asList(
      makeProjectSummary("PROJ1", "PipelineA"),
      makeProjectSummary("PROJ2", "PipelineB"),
      makeProjectSummary("PROJ3", "PipelineC"),
      makeProjectSummary("PROJ4", "PipelineA"),
      makeProjectSummary("PROJ5", "PipelineB"));

  @Test
  public void testFilterName() {
    ProjectSummaryFilter filter = new ProjectSummaryFilter(ProjectSummaryFilterKey.NAME, "PROJ1");
    testFilter(filter, Arrays.asList("PROJ1"));
  }

  @Test
  public void testFilterPipeline() {
    ProjectSummaryFilter filter =
        new ProjectSummaryFilter(ProjectSummaryFilterKey.PIPELINE, "PipelineA");
    testFilter(filter, Arrays.asList("PROJ1", "PROJ4"));
  }

  private static void testFilter(ProjectSummaryFilter filter, List<String> expectedNames) {
    List<ProjectSummary> filtered = projectSummaries.stream().filter(filter.predicate()).toList();
    for (String expectedName : expectedNames) {
      assertTrue(filtered.stream().anyMatch(x -> x.getName().equals(expectedName)),
          "Project %s included".formatted(expectedName));
    }
    assertEquals(expectedNames.size(), filtered.size(), "Project Summary count");
  }

  private static ProjectSummary makeProjectSummary(String name, String pipelineName) {
    ProjectSummary projectSummary = mock(ProjectSummary.class);
    when(projectSummary.getName()).thenReturn(name);
    when(projectSummary.getPipeline()).thenReturn(pipelineName);
    return projectSummary;
  }
}
