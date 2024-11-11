package ca.on.oicr.gsi.dimsum.data;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.cardea.data.Sample;

public class NotificationTest {

  private static String LIST_PREFIX = ":\n\n";

  @Test
  public void testMakePunctuationAndListSamples() {
    Set<Sample> samples = new HashSet<>();
    samples.add(mockSample(1, 1));
    samples.add(mockSample(1, 2));
    samples.add(mockSample(1, 3));

    String result = Notification.makePunctuationAndList(samples);
    String[] lines = result.split("\n");
    // expecting starting punctuation (2 lines) + 3 library lines
    assertEquals(5, lines.length);
    assertTrue(result.startsWith(LIST_PREFIX));
    for (int i = 2; i < lines.length; i++) {
      assertTrue(lines[2].matches("^\\* Library \\d+ \\(L1\\)$"));
    }
  }

  @Test
  public void testMakePunctuationAndListProjects() {
    Set<Sample> samples = new HashSet<>();
    for (int projectNumber = 1; projectNumber <= 5; projectNumber++) {
      for (int sampleNumber = 1; sampleNumber <= 25; sampleNumber++) {
        samples.add(mockSample(projectNumber, sampleNumber));
      }
    }

    String result = Notification.makePunctuationAndList(samples);
    String[] lines = result.split("\n");
    // expecting starting punctuation (2 lines) + 5 project lines
    assertEquals(7, lines.length);
    assertTrue(result.startsWith(LIST_PREFIX));
    for (int i = 2; i < lines.length; i++) {
      assertTrue(lines[2].matches("^\\* \\d+ Project \\d libraries$"));
    }
  }

  @Test
  public void testMakePunctuationAndListSummaryOnly() {
    Set<Sample> samples = new HashSet<>();
    for (int projectNumber = 1; projectNumber <= 105; projectNumber++) {
      samples.add(mockSample(projectNumber, 1));
      samples.add(mockSample(projectNumber, 2));
    }

    String result = Notification.makePunctuationAndList(samples);
    String[] lines = result.split("\n");
    // expecting starting punctuation (2 lines) + 1 summary line
    assertEquals(3, lines.length);
    assertTrue(result.startsWith(LIST_PREFIX));
    assertEquals("* 210 libraries in 105 projects", lines[2]);
  }

  private static Sample mockSample(int projectNumber, int libraryNumber) {
    Sample sample = mock(Sample.class);
    when(sample.getProject()).thenReturn("Project " + projectNumber);
    when(sample.getName()).thenReturn("Library " + libraryNumber);
    when(sample.getSequencingLane()).thenReturn("1");
    return sample;
  }

}
