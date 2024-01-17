package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.dimsum.util.SampleUtils;

public class SampleSortTest {

  private static final String[] namesOrdered =
      {"Sample A", "Sample B", "Sample C", "Sample D", "Sample E"};
  private static final String[] sampleNames =
      {namesOrdered[4], namesOrdered[1], namesOrdered[3], namesOrdered[0], namesOrdered[2]};
  private static final Integer[] qcStatusOrdered = {1, 2, 3, 4, 5};

  @Test
  public void testSortByNameAscending() {
    List<Sample> samples = getSamplesSorted(SampleSort.NAME, false);
    assertOrder(samples, Sample::getName, namesOrdered, false);
  }

  @Test
  public void testSortByNameDescending() {
    List<Sample> samples = getSamplesSorted(SampleSort.NAME, true);
    assertOrder(samples, Sample::getName, namesOrdered, true);
  }

  @Test
  public void testSortByQcStatusAscending() {
    List<Sample> samples = getSamplesSorted(SampleSort.QC_STATUS, false);
    assertOrder(samples, SampleSort::getSampleQcStatus, qcStatusOrdered, false);
  }

  @Test
  public void testSortByQcStatusDescending() {
    List<Sample> samples = getSamplesSorted(SampleSort.QC_STATUS, true);
    assertOrder(samples, SampleSort::getSampleQcStatus, qcStatusOrdered, true);
  }

  private static List<Sample> getSamplesSorted(SampleSort sort, boolean descending) {
    Comparator<Sample> comparator = descending ? sort.comparator().reversed() : sort.comparator();
    List<Sample> samples = mockSamples().stream().sorted(comparator).toList();
    return samples;
  }

  private static List<Sample> mockSamples() {
    return IntStream.range(0, 5).mapToObj(SampleSortTest::mockSample).toList();
  }

  private static Sample mockSample(int sampleNumber) {
    Sample sample = mock(Sample.class);
    when(sample.getName()).thenReturn(sampleNames[sampleNumber]);

    switch (sampleNumber) {
      case 0:
        when(sample.getQcPassed()).thenReturn(null);
        when(sample.getDataReviewPassed()).thenReturn(null);
        when(sample.getQcUser()).thenReturn(null);
        when(sample.getQcReason()).thenReturn(null);
        when(sample.getRun()).thenReturn(null);
        break;
      case 1:
        when(sample.getQcPassed()).thenReturn(true);
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user1");
        when(sample.getQcReason()).thenReturn(null);
        when(sample.getRun()).thenReturn(mock(Run.class));
        break;
      case 2:
        when(sample.getQcPassed()).thenReturn(false);
        when(sample.getDataReviewPassed()).thenReturn(false);
        when(sample.getQcUser()).thenReturn("user2");
        when(sample.getQcReason()).thenReturn(SampleUtils.TOP_UP_REASON);
        when(sample.getRun()).thenReturn(mock(Run.class));
        break;
      case 3:
        when(sample.getQcPassed()).thenReturn(true);
        when(sample.getDataReviewPassed()).thenReturn(null);
        when(sample.getQcUser()).thenReturn("user3");
        when(sample.getQcReason()).thenReturn(null);
        when(sample.getRun()).thenReturn(mock(Run.class));
        break;
      case 4:
        when(sample.getQcPassed()).thenReturn(false);
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user4");
        when(sample.getQcReason()).thenReturn(null);
        when(sample.getRun()).thenReturn(mock(Run.class));
        break;
    }
    return sample;
  }

  private static <T> void assertOrder(List<Sample> samples, Function<Sample, T> getter,
      T[] expectedOrder, boolean reversed) {
    assertNotNull(samples);
    assertEquals(samples.size(), expectedOrder.length);
    for (int i = 0; i < samples.size(); i++) {
      int index = reversed ? samples.size() - 1 - i : i;
      assertEquals(expectedOrder[index], getter.apply(samples.get(i)),
          "The sample at index " + i + " is not in the correct order.");
    }
  }

}
