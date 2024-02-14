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

  @Test
  public void testGetSampleQcStatusPending() {
    Sample sample = mockSampleWithStatus(0); // pending QC
    assertEquals(1, SampleSort.getSampleQcStatus(sample),
        "Sample QC Status should be 1 for pending QC");
  }

  @Test
  public void testGetSampleQcStatusPendingDataReview() {
    Sample sample = mockSampleWithStatus(1); // pending data review
    assertEquals(2, SampleSort.getSampleQcStatus(sample),
        "Sample QC Status should be 2 for pending data review");
  }

  @Test
  public void testGetSampleQcStatusTopUpRequired() {
    Sample sample = mockSampleWithStatus(2); // top-up required
    assertEquals(3, SampleSort.getSampleQcStatus(sample),
        "Sample QC Status should be 3 for top-up required");
  }

  @Test
  public void testGetSampleQcStatusPassed() {
    Sample sample = mockSampleWithStatus(3); // passed
    assertEquals(4, SampleSort.getSampleQcStatus(sample),
        "Sample QC Status should be 4 for passed");
  }

  @Test
  public void testGetSampleQcStatusOther() {
    Sample sample = mockSampleWithStatus(4); // other
    assertEquals(5, SampleSort.getSampleQcStatus(sample),
        "Sample QC Status should be 5 for other statuses");
  }

  private Sample mockSampleWithStatus(int status) {
    Sample sample = mock(Sample.class);
    switch (status) {
      case 0: // Pending QC
        when(sample.getQcPassed()).thenReturn(null);
        when(sample.getQcReason()).thenReturn(null);
        when(sample.getRun()).thenReturn(null);
        break;
      case 1: {// Pending Data Review
        when(sample.getQcPassed()).thenReturn(true);
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user1");
        when(sample.getQcReason()).thenReturn(null);
        Run run = mock(Run.class);
        when(run.getQcPassed()).thenReturn(true);
        when(run.getDataReviewPassed()).thenReturn(null);
        when(sample.getRun()).thenReturn(run);
        break;
      }
      case 2: {// Top-Up Required
        when(sample.getQcPassed()).thenReturn(null);
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user2");
        when(sample.getQcReason()).thenReturn(SampleUtils.TOP_UP_REASON);
        Run run = mock(Run.class);
        when(run.getQcPassed()).thenReturn(true);
        when(run.getDataReviewPassed()).thenReturn(true);
        when(sample.getRun()).thenReturn(run);
        break;
      }
      case 3: // Passed
        when(sample.getQcPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user3");
        when(sample.getQcReason()).thenReturn(null);
        break;
      case 4: // Other (failed)
        when(sample.getQcPassed()).thenReturn(false);
        when(sample.getQcUser()).thenReturn("user4");
        when(sample.getQcReason()).thenReturn(null);
        break;
    }
    return sample;
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
      case 0: // Pending QC
        when(sample.getQcPassed()).thenReturn(null);
        when(sample.getQcUser()).thenReturn(null);
        when(sample.getQcReason()).thenReturn(null);
        break;
      case 1: {// Pending Data Review
        when(sample.getQcPassed()).thenReturn(true);
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user1");
        when(sample.getQcReason()).thenReturn(null);
        Run run = mock(Run.class);
        when(run.getQcPassed()).thenReturn(true);
        when(run.getDataReviewPassed()).thenReturn(null);
        when(sample.getRun()).thenReturn(run);
        break;
      }
      case 2: {// Top-Up Required
        when(sample.getQcPassed()).thenReturn(null);
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user2");
        when(sample.getQcReason()).thenReturn(SampleUtils.TOP_UP_REASON);
        Run run = mock(Run.class);
        when(run.getQcPassed()).thenReturn(true);
        when(run.getDataReviewPassed()).thenReturn(true);
        when(sample.getRun()).thenReturn(run);
        break;
      }
      case 3: // Passed
        when(sample.getQcPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user3");
        when(sample.getQcReason()).thenReturn(null);
        break;
      case 4: // Other (failed)
        when(sample.getQcPassed()).thenReturn(false);
        when(sample.getQcUser()).thenReturn("user4");
        when(sample.getQcReason()).thenReturn(null);
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
