package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import ca.on.oicr.gsi.dimsum.data.RequisitionQc;
import ca.on.oicr.gsi.dimsum.data.Sample;

public class DateFilterTest {
  private static List<Sample> samples = Arrays.asList(
      makeSample("SAM1", LocalDate.of(2023, 2, 1)),
      makeSample("SAM2", LocalDate.of(2023, 1, 1)),
      makeSample("SAM3", LocalDate.of(2023, 3, 31)),
      makeSample("SAM4", LocalDate.of(2023, 2, 2)),
      makeSample("SAM5", LocalDate.of(2022, 2, 1)));

  private static List<RequisitionQc> requisitonQcs = Arrays.asList(
      makeRequisitionQc("USER1", LocalDate.of(2023, 2, 1)),
      makeRequisitionQc("USER2", LocalDate.of(2023, 1, 1)),
      makeRequisitionQc("USER3", LocalDate.of(2023, 3, 31)),
      makeRequisitionQc("USER4", LocalDate.of(2023, 2, 2)),
      makeRequisitionQc("USER5", LocalDate.of(2022, 2, 1)));

  @Test
  public void testSampleAfterDateFilter() {
    DateFilter filter = new DateFilter(DateFilterKey.AFTER_DATE, "2023-02-01");
    testSampleFilter(filter, Arrays.asList("SAM1", "SAM3", "SAM4"));
  }

  @Test
  public void testSampleBeforeDateFilter() {
    DateFilter filter = new DateFilter(DateFilterKey.BEFORE_DATE, "2023-02-01");
    testSampleFilter(filter, Arrays.asList("SAM1", "SAM2", "SAM5"));
  }

  @Test
  public void testRequisitionQcAfterDateFilter() {
    DateFilter filter = new DateFilter(DateFilterKey.AFTER_DATE, "2023-02-01");
    testRequisitionQcFilter(filter, Arrays.asList("USER1", "USER3", "USER4"));
  }

  @Test
  public void testRequisitionQcBeforeDateFilter() {
    DateFilter filter = new DateFilter(DateFilterKey.BEFORE_DATE, "2023-02-01");
    testRequisitionQcFilter(filter, Arrays.asList("USER1", "USER2", "USER5"));
  }

  private static void testSampleFilter(DateFilter filter, List<String> expectedIds) {
    List<Sample> filtered = getSamplesFiltered(filter);
    for (String expectedId : expectedIds) {
      assertTrue(filtered.stream().anyMatch(x -> x.getId().equals(expectedId)),
          "Sample %s included".formatted(expectedId));
    }
    assertEquals(expectedIds.size(), filtered.size(), "Sample count");
  }

  private static List<Sample> getSamplesFiltered(DateFilter filter) {
    return samples.stream().filter(filter.samplePredicate()).toList();
  }

  private static void testRequisitionQcFilter(DateFilter filter, List<String> expectedIds) {
    List<RequisitionQc> filtered = getRequisitionQcsFiltered(filter);
    for (String expectedId : expectedIds) {
      assertTrue(filtered.stream().anyMatch(x -> x.getQcUser().equals(expectedId)),
          "Sample %s included".formatted(expectedId));
    }
    assertEquals(expectedIds.size(), filtered.size(), "Sample count");
  }

  private static List<RequisitionQc> getRequisitionQcsFiltered(DateFilter filter) {
    return requisitonQcs.stream().filter(filter.requisitionQcPredicate()).toList();
  }

  private static Sample makeSample(String id, LocalDate qcDate) {
    Sample sample = mock(Sample.class);
    when(sample.getId()).thenReturn(id);
    when(sample.getQcDate()).thenReturn(qcDate);
    return sample;
  }

  private static RequisitionQc makeRequisitionQc(String qcUser, LocalDate qcDate) {
    RequisitionQc requisitionQc = mock(RequisitionQc.class);
    when(requisitionQc.getQcUser()).thenReturn(qcUser);
    when(requisitionQc.getQcDate()).thenReturn(qcDate);
    return requisitionQc;
  }
}
