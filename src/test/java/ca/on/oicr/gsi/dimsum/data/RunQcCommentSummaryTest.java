package ca.on.oicr.gsi.dimsum.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import com.atlassian.jira.rest.client.api.domain.Issue;

public class RunQcCommentSummaryTest {

  @Test
  public void testFindLatest() {
    String code = "R1A2Q3D4";
    Issue issue = makeIssue(code);
    RunQcCommentSummary sut = RunQcCommentSummary.findLatest(issue);
    assertNotNull(sut);
    assertEquals(code, sut.getCode());
    assertEquals(2, sut.getPendingAnalysisCount());
    assertEquals(3, sut.getPendingQcCount());
    assertEquals(4, sut.getPendingDataReviewCount());
  }

  private Issue makeIssue(String code) {
    Issue issue = mock(Issue.class);
    when(issue.getDescription()).thenReturn("""
        (Human readable stuff here)

        Internal use: <%s>""".formatted(code));
    return issue;
  }

}
