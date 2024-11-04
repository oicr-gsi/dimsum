package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.cardea.data.OmittedRunSample;
import ca.on.oicr.gsi.dimsum.util.DataUtils;

public class OmittedRunSampleSortTest {

  private static final String[] namesOrdered =
      {"Sample A", "Sample B", "Sample C", "Sample D", "Sample E"};
  private static final String[] sampleNames =
      {namesOrdered[4], namesOrdered[1], namesOrdered[3], namesOrdered[0], namesOrdered[2]};
  private static final Integer[] qcStatusOrdered = {1, 2, 3, 4, 5};
  private static final Integer[] qcStatuses = {5, 1, 4, 2, 3};

  @Test
  public void testSortByNameAscending() {
    List<OmittedRunSample> samples = getSamplesSorted(OmittedRunSampleSort.NAME, false);
    assertOrder(samples, OmittedRunSample::getName, namesOrdered, false);
  }

  @Test
  public void testSortByNameDescending() {
    List<OmittedRunSample> samples = getSamplesSorted(OmittedRunSampleSort.NAME, true);
    assertOrder(samples, OmittedRunSample::getName, namesOrdered, true);
  }

  @Test
  public void testSortByQcStatusAscending() {
    List<OmittedRunSample> samples = getSamplesSorted(OmittedRunSampleSort.QC_STATUS, false);
    assertOrder(samples, OmittedRunSampleSort::getQcStatusSortPriority, qcStatusOrdered, false);
  }

  @Test
  public void testSortByQcStatusDescending() {
    List<OmittedRunSample> samples = getSamplesSorted(OmittedRunSampleSort.QC_STATUS, true);
    assertOrder(samples, OmittedRunSampleSort::getQcStatusSortPriority, qcStatusOrdered, true);
  }

  private static List<OmittedRunSample> getSamplesSorted(OmittedRunSampleSort sort,
      boolean descending) {
    Comparator<OmittedRunSample> comparator =
        descending ? sort.comparator().reversed() : sort.comparator();
    List<OmittedRunSample> samples = mockSamples().stream().sorted(comparator).toList();
    return samples;
  }

  private static List<OmittedRunSample> mockSamples() {
    return IntStream.range(0, 5).mapToObj(OmittedRunSampleSortTest::mockSample).toList();
  }

  private static OmittedRunSample mockSample(int sampleNumber) {
    OmittedRunSample sample = mock(OmittedRunSample.class);
    when(sample.getName()).thenReturn(sampleNames[sampleNumber]);

    switch (qcStatuses[sampleNumber]) {
      case 1: // Pending QC
        // nothing needs set
        break;
      case 2: // Pending Data Review
        when(sample.getQcPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user1");
        when(sample.getQcDate()).thenReturn(LocalDate.now());
        break;
      case 3: // Top-Up Required
        when(sample.getQcPassed()).thenReturn(null);
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user2");
        when(sample.getQcReason()).thenReturn(DataUtils.TOP_UP_REASON);
        when(sample.getQcDate()).thenReturn(LocalDate.now());
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getDataReviewUser()).thenReturn("user3");
        when(sample.getDataReviewDate()).thenReturn(LocalDate.now());
        break;
      case 4: // Passed
        when(sample.getQcPassed()).thenReturn(true);
        when(sample.getQcUser()).thenReturn("user3");
        when(sample.getQcDate()).thenReturn(LocalDate.now());
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getDataReviewUser()).thenReturn("user3");
        when(sample.getDataReviewDate()).thenReturn(LocalDate.now());
        break;
      case 5: // Other (failed)
        when(sample.getQcPassed()).thenReturn(false);
        when(sample.getQcUser()).thenReturn("user4");
        when(sample.getQcDate()).thenReturn(LocalDate.now());
        when(sample.getDataReviewPassed()).thenReturn(true);
        when(sample.getDataReviewUser()).thenReturn("user3");
        when(sample.getDataReviewDate()).thenReturn(LocalDate.now());
        break;
    }
    assertEquals(qcStatuses[sampleNumber], OmittedRunSampleSort.getQcStatusSortPriority(sample));
    return sample;
  }

  private static <T> void assertOrder(List<OmittedRunSample> samples,
      Function<OmittedRunSample, T> getter,
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
