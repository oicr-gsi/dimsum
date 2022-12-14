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
import ca.on.oicr.gsi.dimsum.data.Donor;
import ca.on.oicr.gsi.dimsum.data.OmittedSample;

public class OmittedSampleSortTest {

  private static final String[] namesOrdered = {"Sample_A", "Sample_B", "Sample_C"};
  private static final String[] names = {namesOrdered[1], namesOrdered[2], namesOrdered[0]};
  private static final String[] projectsOrdered = {"APROJ", "BPROJ", "CPROJ"};
  private static final String[] projects =
      {projectsOrdered[2], projectsOrdered[0], projectsOrdered[1]};
  private static final String[] donorsOrdered = {"APROJ_0001", "APROJ_0002", "BPROJ_0001"};
  private static final String[] donors = {donorsOrdered[1], donorsOrdered[0], donorsOrdered[2]};
  private static final String[] requisitionsOrdered = {"REQ A", "REQ B", "REQ C"};
  private static final String[] requisitions =
      {requisitionsOrdered[0], requisitionsOrdered[2], requisitionsOrdered[1]};

  @Test
  public void testSortByNameAscending() {
    List<OmittedSample> samples = getSamplesSorted(OmittedSampleSort.NAME, false);
    assertOrder(samples, OmittedSample::getName, namesOrdered, false);
  }

  @Test
  public void testSortByNameDescending() {
    List<OmittedSample> samples = getSamplesSorted(OmittedSampleSort.NAME, true);
    assertOrder(samples, OmittedSample::getName, namesOrdered, true);
  }

  @Test
  public void testSortByProjectAscending() {
    List<OmittedSample> samples = getSamplesSorted(OmittedSampleSort.PROJECT, false);
    assertOrder(samples, OmittedSample::getProject, projectsOrdered, false);
  }

  @Test
  public void testSortByProjectDescending() {
    List<OmittedSample> samples = getSamplesSorted(OmittedSampleSort.PROJECT, true);
    assertOrder(samples, OmittedSample::getProject, projectsOrdered, true);
  }

  @Test
  public void testSortByDonorAscending() {
    List<OmittedSample> samples = getSamplesSorted(OmittedSampleSort.DONOR, false);
    assertOrder(samples, sample -> sample.getDonor().getName(), donorsOrdered, false);
  }

  @Test
  public void testSortByDonorDescending() {
    List<OmittedSample> samples = getSamplesSorted(OmittedSampleSort.DONOR, true);
    assertOrder(samples, sample -> sample.getDonor().getName(), donorsOrdered, true);
  }

  @Test
  public void testSortByRequisitionAscending() {
    List<OmittedSample> samples = getSamplesSorted(OmittedSampleSort.REQUISITION, false);
    assertOrder(samples, OmittedSample::getRequisitionName, requisitionsOrdered, false);
  }

  @Test
  public void testSortByRequisitionDescending() {
    List<OmittedSample> samples = getSamplesSorted(OmittedSampleSort.REQUISITION, true);
    assertOrder(samples, OmittedSample::getRequisitionName, requisitionsOrdered, true);
  }

  private static <T> void assertOrder(List<OmittedSample> samples,
      Function<OmittedSample, T> getter, T[] expectedOrder, boolean reversed) {
    assertNotNull(samples);
    assertEquals(samples.size(), expectedOrder.length);
    assertEquals(expectedOrder[reversed ? 2 : 0], getter.apply(samples.get(0)));
    assertEquals(expectedOrder[reversed ? 1 : 1], getter.apply(samples.get(1)));
    assertEquals(expectedOrder[reversed ? 0 : 2], getter.apply(samples.get(2)));
  }

  private static List<OmittedSample> getSamplesSorted(OmittedSampleSort sort, boolean descending) {
    Comparator<OmittedSample> comparator =
        descending ? sort.comparator().reversed() : sort.comparator();
    return IntStream.range(0, 3)
        .mapToObj(OmittedSampleSortTest::mockSample)
        .sorted(comparator)
        .toList();
  }

  private static OmittedSample mockSample(int sampleNumber) {
    OmittedSample sample = mock(OmittedSample.class);
    when(sample.getName()).thenReturn(names[sampleNumber]);
    when(sample.getProject()).thenReturn(projects[sampleNumber]);
    Donor donor = mock(Donor.class);
    when(donor.getName()).thenReturn(donors[sampleNumber]);
    when(sample.getDonor()).thenReturn(donor);
    when(sample.getRequisitionName()).thenReturn(requisitions[sampleNumber]);
    return sample;
  }

}
