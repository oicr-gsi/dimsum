package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.cardea.data.Sample;

public class SampleSortTest {

  private static final String[] namesOrdered = {"Sample A", "Sample B", "Sample C"};
  private static final String[] sampleNames = {namesOrdered[1], namesOrdered[0], namesOrdered[2]};

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

  private static List<Sample> getSamplesSorted(SampleSort sort, boolean descending) {
    Comparator<Sample> comparator = descending ? sort.comparator().reversed() : sort.comparator();
    List<Sample> samples = mockSamples().stream().sorted(comparator).toList();
    return samples;
  }

  private static List<Sample> mockSamples() {
    return IntStream.range(0, 3).mapToObj(SampleSortTest::mockSample).toList();
  }

  private static Sample mockSample(int sampleNumber) {
    Sample sample = mock(Sample.class);
    when(sample.getName()).thenReturn(sampleNames[sampleNumber]);
    return sample;
  }

  private static <T> void assertOrder(List<Sample> samples, Function<Sample, T> getter,
      T[] expectedOrder, boolean reversed) {
    assertNotNull(samples);
    assertEquals(samples.size(), expectedOrder.length);
    assertEquals(expectedOrder[reversed ? 2 : 0], getter.apply(samples.get(0)));
    assertEquals(expectedOrder[reversed ? 1 : 1], getter.apply(samples.get(1)));
    assertEquals(expectedOrder[reversed ? 0 : 2], getter.apply(samples.get(2)));
  }

}
