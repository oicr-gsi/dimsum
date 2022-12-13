package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.dimsum.data.Donor;
import ca.on.oicr.gsi.dimsum.data.OmittedSample;

public class OmittedSampleFilterTest {

  private static List<OmittedSample> samples = Arrays.asList(
      makeSample("SAM1", "PROJ1", "PROJ1_0001", "external11", "REQ1"),
      makeSample("SAM2", "PROJ1", "PROJ1_0002", "external12", null),
      makeSample("SAM3", "PROJ2", "PROJ2_0001", "external21", "REQ1"),
      makeSample("SAM4", "PROJ2", "PROJ2_0001", "external21", null),
      makeSample("SAM5", "PROJ2", "PROJ2_0002", "external22", "REQ2"));

  @Test
  public void testProjectFilter() {
    OmittedSampleFilter filter = new OmittedSampleFilter(OmittedSampleFilterKey.PROJECT, "PROJ1");
    testFilter(filter, Arrays.asList("SAM1", "SAM2"));
  }

  @Test
  public void testDonorFilterName() {
    OmittedSampleFilter filter =
        new OmittedSampleFilter(OmittedSampleFilterKey.DONOR, "PROJ2_0001");
    testFilter(filter, Arrays.asList("SAM3", "SAM4"));
  }

  @Test
  public void testDonorFilterExternalName() {
    OmittedSampleFilter filter =
        new OmittedSampleFilter(OmittedSampleFilterKey.DONOR, "external21");
    testFilter(filter, Arrays.asList("SAM3", "SAM4"));
  }

  @Test
  public void testRequisitionFilter() {
    OmittedSampleFilter filter =
        new OmittedSampleFilter(OmittedSampleFilterKey.REQUISITION, "REQ1");
    testFilter(filter, Arrays.asList("SAM1", "SAM3"));
  }

  private static void testFilter(OmittedSampleFilter filter, List<String> expectedIds) {
    List<OmittedSample> filtered = getSamplesFiltered(filter);
    for (String expectedId : expectedIds) {
      assertTrue(filtered.stream().anyMatch(x -> x.getId().equals(expectedId)),
          "Sample %s included".formatted(expectedId));
    }
    assertEquals(expectedIds.size(), filtered.size(), "Sample count");
  }

  private static List<OmittedSample> getSamplesFiltered(OmittedSampleFilter filter) {
    return samples.stream().filter(filter.predicate()).toList();
  }

  private static OmittedSample makeSample(String id, String projectName, String donorName,
      String externalName, String requisitionName) {
    OmittedSample sample = mock(OmittedSample.class);
    when(sample.getId()).thenReturn(id);
    when(sample.getProject()).thenReturn(projectName);
    Donor donor = mock(Donor.class);
    when(donor.getName()).thenReturn(donorName);
    when(donor.getExternalName()).thenReturn(externalName);
    when(sample.getDonor()).thenReturn(donor);
    when(sample.getRequisitionName()).thenReturn(requisitionName);
    return sample;
  }

}
