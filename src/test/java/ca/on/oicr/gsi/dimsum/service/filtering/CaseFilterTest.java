package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.MockCase;

public class CaseFilterTest {

  private static final String DELIVERABLE_CLINICAL = "Clinical Report";
  private static final String DELIVERABLE_DATA = "Data Release";

  private static final List<MetricCategory> SAMPLE_CATEGORIES = Arrays.asList(
      MetricCategory.RECEIPT,
      MetricCategory.EXTRACTION, MetricCategory.LIBRARY_PREP, MetricCategory.FULL_DEPTH_SEQUENCING);

  private static List<Case> cases = MockCase.getCases();

  @org.junit.jupiter.api.Test
  public void testAssayFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.ASSAY, "WGTS");
    testFilterCases(filter, Arrays.asList(0, 1));
  }

  @org.junit.jupiter.api.Test
  public void testDonorFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.DONOR, "PRO1_0001");
    testFilterCases(filter, Arrays.asList(0, 2));
  }

  @org.junit.jupiter.api.Test
  public void testProjectFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PROJECT, "PRO2");
    testFilterCases(filter, Arrays.asList(1, 3));
  }

  @org.junit.jupiter.api.Test
  public void testProjectSampleFilter() {
    List<Sample> samples = new ArrayList<>();
    samples.add(makeSample("PRO1", "WG"));
    samples.add(makeSample("PRO2", "WG"));

    CaseFilter filter = new CaseFilter(CaseFilterKey.PROJECT, "PRO1");
    List<Sample> filtered = samples.stream()
        .filter(filter.samplePredicate(null))
        .toList();

    assertEquals(1, filtered.size());
    assertEquals("PRO1", filtered.get(0).getProject());
  }

  private static Sample makeSample(String project, String designCode) {
    Sample sample = mock(Sample.class);
    when(sample.getProject()).thenReturn(project);
    when(sample.getLibraryDesignCode()).thenReturn(designCode);
    return sample;
  }

  @org.junit.jupiter.api.Test
  public void testRequisitionFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.REQUISITION, "REQ02");
    testFilterCases(filter, Arrays.asList(1, 3));
  }

  @org.junit.jupiter.api.Test
  public void testTestFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.TEST, "Tumour WT");
    testFilterCases(filter, Arrays.asList(0, 1));
  }

  @org.junit.jupiter.api.Test
  public void testLibraryDesignCaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.LIBRARY_DESIGN, "WT");
    testFilterCases(filter, Arrays.asList(0, 1));
  }

  @org.junit.jupiter.api.Test
  public void testLibraryDesignTestFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.LIBRARY_DESIGN, "WT");
    testFilterTests(filter, Arrays.asList(makeTestGroupId(0, 3), makeTestGroupId(1, 3)));
  }

  @org.junit.jupiter.api.Test
  public void testLibraryDesignSampleFilter() {
    List<Sample> samples = new ArrayList<>();
    samples.add(makeSample("PRO1", "WG"));
    samples.add(makeSample("PRO1", "WT"));

    CaseFilter filter = new CaseFilter(CaseFilterKey.LIBRARY_DESIGN, "WT");
    List<Sample> filtered = samples.stream()
        .filter(filter.samplePredicate(null))
        .toList();

    assertEquals(1, filtered.size());
    assertEquals("WT", filtered.get(0).getLibraryDesignCode());
  }

  @org.junit.jupiter.api.Test
  public void testPendingReceiptQcFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.RECEIPT_QC.getLabel());
    testFilterCases(filter, Arrays.asList(22));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReceiptQcTestFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.RECEIPT_QC.getLabel());
    testFilterTests(filter, Arrays.asList(makeTestGroupId(22, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReceiptQcSampleFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.RECEIPT_QC.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(22, 1, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION.getLabel());
    testFilterCases(filter, Arrays.asList(0, 7, 32));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionTestFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 1),
        makeTestGroupId(7, 1),
        makeTestGroupId(32, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionSampleFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(0, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(7, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(32, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionQcFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION_QC.getLabel());
    testFilterCases(filter, Arrays.asList(0, 8));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionQcTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION_QC.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 2),
        makeTestGroupId(8, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionQcSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION_QC.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(0, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(8, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(0, 2, MetricCategory.EXTRACTION, 1),
        makeSampleId(8, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionTransferFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION_TRANSFER.getLabel());
    testFilterCases(filter, Arrays.asList(35));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionTransferTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION_TRANSFER.getLabel());
    testFilterTests(filter, Arrays.asList(makeTestGroupId(35, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionTransferSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION_TRANSFER.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT,
        Arrays.asList(makeSampleId(35, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION,
        Arrays.asList(makeSampleId(35, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryPrepFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_PREPARATION.getLabel());
    testFilterCases(filter, Arrays.asList(0, 9, 20));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryPrepTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_PREPARATION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 3),
        makeTestGroupId(9, 1),
        makeTestGroupId(20, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryPrepSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_PREPARATION.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(0, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(9, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(20, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(0, 3, MetricCategory.EXTRACTION, 1),
        makeSampleId(9, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(20, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(20, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQcFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QC.getLabel());
    testFilterCases(filter, Arrays.asList(1, 10));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQcTestFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QC.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(1, 1),
        makeTestGroupId(10, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQcSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QC.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(1, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(10, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(1, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(10, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(1, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(10, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Collections.emptyList());
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QUALIFICATION.getLabel());
    testFilterCases(filter, Arrays.asList(1, 11, 12, 21));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QUALIFICATION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(1, 2),
        makeTestGroupId(11, 1),
        makeTestGroupId(12, 1),
        makeTestGroupId(21, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QUALIFICATION.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(1, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(11, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(12, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(21, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(1, 2, MetricCategory.EXTRACTION, 1),
        makeSampleId(11, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(12, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(21, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(1, 2, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(11, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(12, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(21, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(12, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationQCFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QUALIFICATION_QC.getLabel());
    testFilterCases(filter, Arrays.asList(1, 13, 14));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationQcTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QUALIFICATION_QC.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(1, 3),
        makeTestGroupId(13, 1),
        makeTestGroupId(14, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationQcSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QUALIFICATION_QC.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(1, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(13, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(14, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(1, 3, MetricCategory.EXTRACTION, 1),
        makeSampleId(13, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(14, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(1, 3, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(13, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(14, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(1, 3, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(13, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(14, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationDataReviewFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING,
        PendingState.LIBRARY_QUALIFICATION_DATA_REVIEW.getLabel());
    testFilterCases(filter, Arrays.asList(2, 15));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationDataReviewTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING,
            PendingState.LIBRARY_QUALIFICATION_DATA_REVIEW.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(2, 1),
        makeTestGroupId(15, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationDataReviewSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING,
            PendingState.LIBRARY_QUALIFICATION_DATA_REVIEW.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(2, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(15, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(2, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(15, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(2, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(15, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(2, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(15, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthSequencingFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterCases(filter, Arrays.asList(2, 16, 17));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthSequencingTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(2, 2),
        makeTestGroupId(16, 1),
        makeTestGroupId(17, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthSequencingSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(2, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(16, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(17, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(2, 2, MetricCategory.EXTRACTION, 1),
        makeSampleId(16, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(17, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(2, 2, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(16, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(17, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(2, 2, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(16, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(17, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Arrays.asList(
        makeSampleId(17, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthSequencingTopUpFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_TOP_UP.getLabel());
    testFilterCases(filter, Arrays.asList(17));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthSequencingTopUpTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_TOP_UP.getLabel());
    testFilterTests(filter, Arrays.asList(makeTestGroupId(17, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthSequencingTopUpSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_TOP_UP.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(17, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(17, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(17, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(17, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Arrays.asList(
        makeSampleId(17, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthQcFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_QC.getLabel());
    testFilterCases(filter, Arrays.asList(3, 18));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthQcTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_QC.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(3, 1),
        makeTestGroupId(18, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthQcSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_QC.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(3, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(18, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(3, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(18, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(3, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(18, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(3, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(18, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Arrays.asList(
        makeSampleId(3, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(18, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthDataReviewFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_DATA_REVIEW.getLabel());
    testFilterCases(filter, Arrays.asList(3, 19));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthDataReviewTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_DATA_REVIEW.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(3, 2),
        makeTestGroupId(19, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthDataReviewSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_DATA_REVIEW.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(3, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(19, 0, MetricCategory.RECEIPT, 1)));
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(3, 2, MetricCategory.EXTRACTION, 1),
        makeSampleId(19, 1, MetricCategory.EXTRACTION, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(3, 2, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(19, 1, MetricCategory.LIBRARY_PREP, 1)));
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(3, 2, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(19, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Arrays.asList(
        makeSampleId(3, 2, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(19, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingAnalysisReviewFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.ANALYSIS_REVIEW.getLabel());
    testFilterCases(filter, Arrays.asList(4, 25, 30, 31));
  }

  @org.junit.jupiter.api.Test
  public void testPendingAnalysisReviewDataReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING,
        concatStateAndDeliverable(PendingState.ANALYSIS_REVIEW, DELIVERABLE_DATA));
    testFilterCases(filter, Arrays.asList(4, 31));
  }

  @org.junit.jupiter.api.Test
  public void testPendingAnalysisReviewClinicalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING,
        concatStateAndDeliverable(PendingState.ANALYSIS_REVIEW, DELIVERABLE_CLINICAL));
    testFilterCases(filter, Arrays.asList(25, 30));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReleaseApprovalFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.RELEASE_APPROVAL.getLabel());
    testFilterCases(filter, Arrays.asList(5, 23, 26, 29, 30, 33));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReleaseApprovalDataReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING,
        concatStateAndDeliverable(PendingState.RELEASE_APPROVAL, DELIVERABLE_DATA));
    testFilterCases(filter, Arrays.asList(5, 23, 29, 30, 33));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReleaseApprovalClinicalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING,
        concatStateAndDeliverable(PendingState.RELEASE_APPROVAL, DELIVERABLE_CLINICAL));
    testFilterCases(filter, Arrays.asList(26));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.RELEASE.getLabel());
    testFilterCases(filter, Arrays.asList(6, 27, 29));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReleaseDataReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING,
        concatStateAndDeliverable(PendingState.RELEASE, DELIVERABLE_DATA));
    testFilterCases(filter, Arrays.asList(6));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReleaseClinicalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING,
        concatStateAndDeliverable(PendingState.RELEASE, DELIVERABLE_CLINICAL));
    testFilterCases(filter, Arrays.asList(27, 29));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReceiptFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.RECEIPT.getLabel());
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReceiptTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.RECEIPT.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 1),
        makeTestGroupId(0, 2),
        makeTestGroupId(0, 3),
        makeTestGroupId(1, 1),
        makeTestGroupId(1, 2),
        makeTestGroupId(1, 3),
        makeTestGroupId(2, 1),
        makeTestGroupId(2, 2),
        makeTestGroupId(3, 1),
        makeTestGroupId(3, 2),
        makeTestGroupId(4, 1),
        makeTestGroupId(5, 1),
        makeTestGroupId(6, 1),
        makeTestGroupId(6, 2),
        makeTestGroupId(7, 1),
        makeTestGroupId(8, 1),
        makeTestGroupId(9, 1),
        makeTestGroupId(10, 1),
        makeTestGroupId(11, 1),
        makeTestGroupId(12, 1),
        makeTestGroupId(13, 1),
        makeTestGroupId(14, 1),
        makeTestGroupId(15, 1),
        makeTestGroupId(16, 1),
        makeTestGroupId(17, 1),
        makeTestGroupId(18, 1),
        makeTestGroupId(19, 1),
        makeTestGroupId(20, 1),
        makeTestGroupId(21, 1),
        makeTestGroupId(23, 1),
        makeTestGroupId(24, 1),
        makeTestGroupId(25, 1),
        makeTestGroupId(26, 1),
        makeTestGroupId(27, 1),
        makeTestGroupId(28, 1),
        makeTestGroupId(29, 1),
        makeTestGroupId(30, 1),
        makeTestGroupId(31, 1),
        makeTestGroupId(32, 1),
        makeTestGroupId(33, 1),
        makeTestGroupId(35, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReceiptSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.RECEIPT.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT,
        Arrays.asList(
            makeSampleId(0, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(1, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(2, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(3, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(4, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(5, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(6, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(7, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(8, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(9, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(10, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(11, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(12, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(13, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(14, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(15, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(16, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(17, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(18, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(19, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(20, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(21, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(23, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(24, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(25, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(26, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(27, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(28, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(29, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(30, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(31, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(32, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(33, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(34, 0, MetricCategory.RECEIPT, 1),
            makeSampleId(35, 0, MetricCategory.RECEIPT, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedExtractionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.EXTRACTION.getLabel());
    testFilterCases(filter, Arrays.asList(1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 25, 26, 27, 28, 29, 30, 31, 33, 34));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedExtractionTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.EXTRACTION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(1, 1),
        makeTestGroupId(1, 2),
        makeTestGroupId(1, 3),
        makeTestGroupId(2, 1),
        makeTestGroupId(2, 2),
        makeTestGroupId(3, 1),
        makeTestGroupId(3, 2),
        makeTestGroupId(4, 1),
        makeTestGroupId(5, 1),
        makeTestGroupId(6, 1),
        makeTestGroupId(6, 2),
        makeTestGroupId(9, 1),
        makeTestGroupId(10, 1),
        makeTestGroupId(11, 1),
        makeTestGroupId(12, 1),
        makeTestGroupId(13, 1),
        makeTestGroupId(14, 1),
        makeTestGroupId(15, 1),
        makeTestGroupId(16, 1),
        makeTestGroupId(17, 1),
        makeTestGroupId(18, 1),
        makeTestGroupId(19, 1),
        makeTestGroupId(20, 1),
        makeTestGroupId(21, 1),
        makeTestGroupId(25, 1),
        makeTestGroupId(26, 1),
        makeTestGroupId(27, 1),
        makeTestGroupId(28, 1),
        makeTestGroupId(29, 1),
        makeTestGroupId(30, 1),
        makeTestGroupId(31, 1),
        makeTestGroupId(33, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedExtractionSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.EXTRACTION.getLabel());
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(1, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(1, 2, MetricCategory.EXTRACTION, 1),
        makeSampleId(1, 3, MetricCategory.EXTRACTION, 1),
        makeSampleId(2, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(2, 2, MetricCategory.EXTRACTION, 1),
        makeSampleId(3, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(3, 2, MetricCategory.EXTRACTION, 1),
        makeSampleId(4, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(5, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(6, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(6, 2, MetricCategory.EXTRACTION, 1),
        makeSampleId(9, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(10, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(11, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(12, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(13, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(14, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(15, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(16, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(17, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(18, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(19, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(20, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(21, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(25, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(26, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(27, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(28, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(29, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(30, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(31, 1, MetricCategory.EXTRACTION, 1),
        makeSampleId(33, 1, MetricCategory.EXTRACTION, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryPrepFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.LIBRARY_PREPARATION.getLabel());
    testFilterCases(filter, Arrays.asList(2, 3, 4, 5, 6, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 21, 25, 26, 27, 28, 29, 30, 31, 33, 34));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryPrepTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_PREPARATION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(2, 1),
        makeTestGroupId(2, 2),
        makeTestGroupId(3, 1),
        makeTestGroupId(3, 2),
        makeTestGroupId(4, 1),
        makeTestGroupId(5, 1),
        makeTestGroupId(6, 1),
        makeTestGroupId(6, 2),
        makeTestGroupId(11, 1),
        makeTestGroupId(12, 1),
        makeTestGroupId(13, 1),
        makeTestGroupId(14, 1),
        makeTestGroupId(15, 1),
        makeTestGroupId(16, 1),
        makeTestGroupId(17, 1),
        makeTestGroupId(18, 1),
        makeTestGroupId(19, 1),
        makeTestGroupId(21, 1),
        makeTestGroupId(25, 1),
        makeTestGroupId(26, 1),
        makeTestGroupId(27, 1),
        makeTestGroupId(28, 1),
        makeTestGroupId(29, 1),
        makeTestGroupId(30, 1),
        makeTestGroupId(31, 1),
        makeTestGroupId(33, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryPrepSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_PREPARATION.getLabel());
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(2, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(2, 2, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(3, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(3, 2, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(4, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(5, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(6, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(6, 2, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(11, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(12, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(13, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(14, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(15, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(16, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(17, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(18, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(19, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(21, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(25, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(26, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(27, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(28, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(29, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(30, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(31, 1, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(33, 1, MetricCategory.LIBRARY_PREP, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryQualFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_QUALIFICATION.getLabel());
    testFilterCases(filter,
        Arrays.asList(3, 4, 5, 6, 16, 17, 18, 19, 25, 26, 27, 28, 29, 30, 31, 33, 34));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryQualTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_QUALIFICATION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(3, 1),
        makeTestGroupId(3, 2),
        makeTestGroupId(4, 1),
        makeTestGroupId(5, 1),
        makeTestGroupId(6, 1),
        makeTestGroupId(6, 2),
        makeTestGroupId(16, 1),
        makeTestGroupId(17, 1),
        makeTestGroupId(18, 1),
        makeTestGroupId(19, 1),
        makeTestGroupId(25, 1),
        makeTestGroupId(26, 1),
        makeTestGroupId(27, 1),
        makeTestGroupId(28, 1),
        makeTestGroupId(29, 1),
        makeTestGroupId(30, 1),
        makeTestGroupId(31, 1),
        makeTestGroupId(33, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryQualSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_QUALIFICATION.getLabel());
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(3, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(3, 2, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(4, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(5, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(6, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(6, 2, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(16, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(17, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(18, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(19, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(25, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(26, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(27, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(28, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(29, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(30, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(31, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(33, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedFullDepthFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterCases(filter, Arrays.asList(4, 5, 6, 25, 26, 27, 28, 29, 30, 31, 33, 34));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedFullDepthTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(4, 1),
        makeTestGroupId(5, 1),
        makeTestGroupId(6, 1),
        makeTestGroupId(6, 2),
        makeTestGroupId(25, 1),
        makeTestGroupId(26, 1),
        makeTestGroupId(27, 1),
        makeTestGroupId(28, 1),
        makeTestGroupId(29, 1),
        makeTestGroupId(30, 1),
        makeTestGroupId(31, 1),
        makeTestGroupId(33, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedFullDepthSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterSamples(filter, MetricCategory.FULL_DEPTH_SEQUENCING, Arrays.asList(
        makeSampleId(4, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(5, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(6, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(6, 2, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(25, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(26, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(27, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(28, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(29, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(30, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(31, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1),
        makeSampleId(33, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedAnalysisReviewFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.ANALYSIS_REVIEW.getLabel());
    testFilterCases(filter, Arrays.asList(5, 6, 26, 27, 28, 29, 33, 34));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedAnalysisReviewDataReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED,
        concatGateAndDeliverable(CompletedGate.ANALYSIS_REVIEW, DELIVERABLE_DATA));
    testFilterCases(filter, Arrays.asList(5, 6, 25, 26, 27, 28, 29, 30, 33));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedAnalysisReviewClinicalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED,
        concatGateAndDeliverable(CompletedGate.ANALYSIS_REVIEW, DELIVERABLE_CLINICAL));
    testFilterCases(filter, Arrays.asList(26, 27, 29, 31));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReleaseApprovalFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.RELEASE_APPROVAL.getLabel());
    testFilterCases(filter, Arrays.asList(6, 27, 28, 34));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReleaseApprovalDataReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED,
        concatGateAndDeliverable(CompletedGate.RELEASE_APPROVAL, DELIVERABLE_DATA));
    testFilterCases(filter, Arrays.asList(6, 25, 26, 27, 28));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReleaseApprovalClinicalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED,
        concatGateAndDeliverable(CompletedGate.RELEASE_APPROVAL, DELIVERABLE_CLINICAL));
    testFilterCases(filter, Arrays.asList(27, 29, 31));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.RELEASE.getLabel());
    testFilterCases(filter, Arrays.asList(28, 34));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReleaseDataReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED,
        concatGateAndDeliverable(CompletedGate.RELEASE, DELIVERABLE_DATA));
    testFilterCases(filter, Arrays.asList(25, 26, 27, 28));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReleaseClinicalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED,
        concatGateAndDeliverable(CompletedGate.RELEASE, DELIVERABLE_CLINICAL));
    testFilterCases(filter, Collections.singletonList(31));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReceiptFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.RECEIPT.getLabel());
    testFilterCases(filter, Arrays.asList(22));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReceiptTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            CompletedGate.RECEIPT.getLabel());
    testFilterTests(filter, makeAllTestGroupIdsForCases(22));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReceiptSampleFilters() {
    testIncompleteSampleFilters(CompletedGate.RECEIPT.getLabel(), 22);
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteExtractionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.EXTRACTION.getLabel());
    testFilterCases(filter, Arrays.asList(0, 7, 8, 22, 23, 24, 32, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteExtractionTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            CompletedGate.EXTRACTION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 1),
        makeTestGroupId(0, 2),
        makeTestGroupId(7, 1),
        makeTestGroupId(8, 1),
        makeTestGroupId(22, 1),
        makeTestGroupId(23, 1),
        makeTestGroupId(24, 1),
        makeTestGroupId(32, 1),
        makeTestGroupId(35, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteExtractionSampleFilters() {
    testIncompleteSampleFiltersIncludingTests(CompletedGate.EXTRACTION.getLabel(),
        new TestGroupId(0, 1),
        new TestGroupId(0, 2),
        new TestGroupId(7, 1),
        new TestGroupId(8, 1),
        new TestGroupId(22, 1),
        new TestGroupId(23, 1),
        new TestGroupId(24, 1),
        new TestGroupId(32, 1),
        new TestGroupId(35, 1));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteLibraryPrepFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.LIBRARY_PREPARATION.getLabel());
    testFilterCases(filter, Arrays.asList(0, 1, 7, 8, 9, 10, 20, 22, 23, 24, 32, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteLibraryPrepTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            CompletedGate.LIBRARY_PREPARATION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 1),
        makeTestGroupId(0, 2),
        makeTestGroupId(0, 3),
        makeTestGroupId(1, 1),
        makeTestGroupId(7, 1),
        makeTestGroupId(8, 1),
        makeTestGroupId(9, 1),
        makeTestGroupId(10, 1),
        makeTestGroupId(20, 1),
        makeTestGroupId(22, 1),
        makeTestGroupId(23, 1),
        makeTestGroupId(24, 1),
        makeTestGroupId(32, 1),
        makeTestGroupId(35, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteLibraryPrepSampleFilters() {
    testIncompleteSampleFiltersIncludingTests(CompletedGate.LIBRARY_PREPARATION.getLabel(),
        new TestGroupId(0, 1),
        new TestGroupId(0, 2),
        new TestGroupId(0, 3),
        new TestGroupId(1, 1),
        new TestGroupId(7, 1),
        new TestGroupId(8, 1),
        new TestGroupId(9, 1),
        new TestGroupId(10, 1),
        new TestGroupId(20, 1),
        new TestGroupId(22, 1),
        new TestGroupId(23, 1),
        new TestGroupId(24, 1),
        new TestGroupId(32, 1),
        new TestGroupId(35, 1));;
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteLibraryQualFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            CompletedGate.LIBRARY_QUALIFICATION.getLabel());
    testFilterCases(filter,
        Arrays.asList(0, 1, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 32, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteLibraryQualTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            CompletedGate.LIBRARY_QUALIFICATION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 1),
        makeTestGroupId(0, 2),
        makeTestGroupId(0, 3),
        makeTestGroupId(1, 1),
        makeTestGroupId(1, 2),
        makeTestGroupId(1, 3),
        makeTestGroupId(2, 1),
        makeTestGroupId(7, 1),
        makeTestGroupId(8, 1),
        makeTestGroupId(9, 1),
        makeTestGroupId(10, 1),
        makeTestGroupId(11, 1),
        makeTestGroupId(12, 1),
        makeTestGroupId(13, 1),
        makeTestGroupId(14, 1),
        makeTestGroupId(15, 1),
        makeTestGroupId(20, 1),
        makeTestGroupId(21, 1),
        makeTestGroupId(22, 1),
        makeTestGroupId(23, 1),
        makeTestGroupId(24, 1),
        makeTestGroupId(32, 1),
        makeTestGroupId(35, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteLibraryQualSampleFilters() {
    testIncompleteSampleFiltersIncludingTests(CompletedGate.LIBRARY_QUALIFICATION.getLabel(),
        new TestGroupId(0, 1),
        new TestGroupId(0, 2),
        new TestGroupId(0, 3),
        new TestGroupId(1, 1),
        new TestGroupId(1, 2),
        new TestGroupId(1, 3),
        new TestGroupId(2, 1),
        new TestGroupId(7, 1),
        new TestGroupId(8, 1),
        new TestGroupId(9, 1),
        new TestGroupId(10, 1),
        new TestGroupId(11, 1),
        new TestGroupId(12, 1),
        new TestGroupId(13, 1),
        new TestGroupId(14, 1),
        new TestGroupId(15, 1),
        new TestGroupId(20, 1),
        new TestGroupId(21, 1),
        new TestGroupId(22, 1),
        new TestGroupId(23, 1),
        new TestGroupId(24, 1),
        new TestGroupId(32, 1),
        new TestGroupId(35, 1));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteFullDepthFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            CompletedGate.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterCases(filter,
        Arrays.asList(0, 1, 2, 3, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
            24, 32, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteFullDepthTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            CompletedGate.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 1),
        makeTestGroupId(0, 2),
        makeTestGroupId(0, 3),
        makeTestGroupId(1, 1),
        makeTestGroupId(1, 2),
        makeTestGroupId(1, 3),
        makeTestGroupId(2, 1),
        makeTestGroupId(2, 2),
        makeTestGroupId(3, 1),
        makeTestGroupId(3, 2),
        makeTestGroupId(7, 1),
        makeTestGroupId(8, 1),
        makeTestGroupId(9, 1),
        makeTestGroupId(10, 1),
        makeTestGroupId(11, 1),
        makeTestGroupId(12, 1),
        makeTestGroupId(13, 1),
        makeTestGroupId(14, 1),
        makeTestGroupId(15, 1),
        makeTestGroupId(16, 1),
        makeTestGroupId(17, 1),
        makeTestGroupId(18, 1),
        makeTestGroupId(19, 1),
        makeTestGroupId(20, 1),
        makeTestGroupId(21, 1),
        makeTestGroupId(22, 1),
        makeTestGroupId(23, 1),
        makeTestGroupId(24, 1),
        makeTestGroupId(32, 1),
        makeTestGroupId(35, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteFullDepthSampleFilters() {
    testIncompleteSampleFiltersIncludingTests(CompletedGate.FULL_DEPTH_SEQUENCING.getLabel(),
        new TestGroupId(0, 1),
        new TestGroupId(0, 2),
        new TestGroupId(0, 3),
        new TestGroupId(1, 1),
        new TestGroupId(1, 2),
        new TestGroupId(1, 3),
        new TestGroupId(2, 1),
        new TestGroupId(2, 2),
        new TestGroupId(3, 1),
        new TestGroupId(3, 2),
        new TestGroupId(7, 1),
        new TestGroupId(8, 1),
        new TestGroupId(9, 1),
        new TestGroupId(10, 1),
        new TestGroupId(11, 1),
        new TestGroupId(12, 1),
        new TestGroupId(13, 1),
        new TestGroupId(14, 1),
        new TestGroupId(15, 1),
        new TestGroupId(16, 1),
        new TestGroupId(17, 1),
        new TestGroupId(18, 1),
        new TestGroupId(19, 1),
        new TestGroupId(20, 1),
        new TestGroupId(21, 1),
        new TestGroupId(22, 1),
        new TestGroupId(23, 1),
        new TestGroupId(24, 1),
        new TestGroupId(32, 1),
        new TestGroupId(35, 1));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteAnalysisReviewFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.ANALYSIS_REVIEW.getLabel());
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 25, 30, 31, 32, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteAnalysisReviewTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.ANALYSIS_REVIEW.getLabel());
    testFilterTests(filter,
        makeAllTestGroupIdsForCases(0, 1, 2, 3, 4, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 30, 31, 32, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteAnalysisReviewSampleFilters() {
    testIncompleteSampleFilters(CompletedGate.ANALYSIS_REVIEW.getLabel(), 0, 1, 2, 3, 4, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 30, 31, 32, 35);
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteAnalysisReviewDataReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.INCOMPLETE,
        concatGateAndDeliverable(CompletedGate.ANALYSIS_REVIEW, DELIVERABLE_DATA));
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 31, 32, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteAnalysisReviewDataReleaseTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            concatGateAndDeliverable(CompletedGate.ANALYSIS_REVIEW, DELIVERABLE_DATA));
    testFilterTests(filter,
        makeAllTestGroupIdsForCases(0, 1, 2, 3, 4, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 31, 32, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteAnalysisReviewDataReleaseSampleFilters() {
    testIncompleteSampleFilters(
        concatGateAndDeliverable(CompletedGate.ANALYSIS_REVIEW, DELIVERABLE_DATA), 0, 1, 2, 3, 4, 7,
        8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 31, 32, 35);
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseApprovalFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.RELEASE_APPROVAL.getLabel());
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseApprovalTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.RELEASE_APPROVAL.getLabel());
    testFilterTests(filter,
        makeAllTestGroupIdsForCases(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseApprovalSampleFilters() {
    testIncompleteSampleFilters(CompletedGate.RELEASE_APPROVAL.getLabel(), 0, 1, 2, 3, 4, 5, 7, 8,
        9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33,
        35);
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseApprovalClinicalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.INCOMPLETE,
        concatGateAndDeliverable(CompletedGate.RELEASE_APPROVAL, DELIVERABLE_CLINICAL));
    testFilterCases(filter, Arrays.asList(25, 26, 30));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseApprovalClinicalReportTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            concatGateAndDeliverable(CompletedGate.RELEASE_APPROVAL, DELIVERABLE_CLINICAL));
    testFilterTests(filter, makeAllTestGroupIdsForCases(25, 26, 30));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseApprovalClinicalReportSampleFilters() {
    testIncompleteSampleFilters(
        concatGateAndDeliverable(CompletedGate.RELEASE_APPROVAL, DELIVERABLE_CLINICAL), 25, 26, 30);
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.RELEASE.getLabel());
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29, 30, 31, 32, 33, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseTestFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.INCOMPLETE, CompletedGate.RELEASE.getLabel());
    testFilterTests(filter,
        makeAllTestGroupIdsForCases(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29, 30, 31, 32, 33, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseSampleFilters() {
    testIncompleteSampleFilters(CompletedGate.RELEASE.getLabel(), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29, 30, 31, 32, 33, 35);
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseDataReleaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.INCOMPLETE,
        concatGateAndDeliverable(CompletedGate.RELEASE, DELIVERABLE_DATA));
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseDataReleaseTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.INCOMPLETE,
            concatGateAndDeliverable(CompletedGate.RELEASE, DELIVERABLE_DATA));
    testFilterTests(filter,
        makeAllTestGroupIdsForCases(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 35));
  }

  @org.junit.jupiter.api.Test
  public void testIncompleteReleaseDataReleaseSampleFilters() {
    testIncompleteSampleFilters(concatGateAndDeliverable(CompletedGate.RELEASE, DELIVERABLE_DATA),
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 35);
  }

  @org.junit.jupiter.api.Test
  public void testStoppedCaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.STOPPED, "Yes");
    testFilterCases(filter, Arrays.asList(23));
  }

  @org.junit.jupiter.api.Test
  public void testNonStoppedCaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.STOPPED, "No");
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35));
  }

  @org.junit.jupiter.api.Test
  public void testPausedCaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PAUSED, "Yes");
    testFilterCases(filter, Arrays.asList(24));
  }

  @org.junit.jupiter.api.Test
  public void testNonPausedCaseFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PAUSED, "No");
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35));
  }

  @org.junit.jupiter.api.Test
  public void testDeliverableFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.DELIVERABLE, "Clinical Report");
    testFilterCases(filter, Arrays.asList(25, 26, 27, 29, 30, 31));
  }

  @org.junit.jupiter.api.Test
  public void testStartedBeforeFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.STARTED_BEFORE, "2024-01-02");
    testFilterCases(filter, Arrays.asList(34));
  }

  @org.junit.jupiter.api.Test
  public void testStartedAfterFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.STARTED_AFTER, "2023-12-31");
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedBeforeFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED_BEFORE, "2024-01-18");
    testFilterCases(filter, Arrays.asList(34));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedAfterFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.COMPLETED_AFTER, "2024-01-10");
    testFilterCases(filter, Arrays.asList(28, 34));
  }

  private static List<Case> getCasesFiltered(CaseFilter filter) {
    return cases.stream().filter(filter.casePredicate()).toList();
  }

  private static void testFilterCases(CaseFilter filter, Collection<Integer> expectedCases) {
    List<Case> filtered = getCasesFiltered(filter);
    for (int caseNumber : expectedCases) {
      assertTrue(filtered.contains(cases.get(caseNumber)),
          String.format("Case #%d included", caseNumber));
    }
    assertEquals(expectedCases.size(), filtered.size(),
        "Case count (%s)".formatted(filtered.stream()
            .map(kase -> String.valueOf(cases.indexOf(kase))).collect(Collectors.joining(", "))));
  }

  private static void testFilterTests(CaseFilter filter, Collection<String> expectedTestGroupIds) {
    List<Test> tests = cases.stream()
        .filter(filter.casePredicate())
        .flatMap(kase -> kase.getTests().stream())
        .filter(filter.testPredicate())
        .toList();
    for (String groupId : expectedTestGroupIds) {
      assertTrue(tests.stream().anyMatch(test -> Objects.equals(test.getGroupId(), groupId)),
          "Test %s included".formatted(groupId));
    }
    assertEquals(expectedTestGroupIds.size(), tests.size(), "Test count");
  }

  private static void testFilterSamples(CaseFilter filter, MetricCategory requestCategory,
      Collection<String> expectedSamples) {
    List<Sample> samples = cases.stream()
        .filter(filter.casePredicate())
        .flatMap(kase -> {
          if (requestCategory == MetricCategory.RECEIPT) {
            return kase.getReceipts().stream().filter(filter.samplePredicate(requestCategory));
          } else {
            return kase.getTests().stream()
                .filter(filter.testPredicate())
                .flatMap(test -> getSamples(test, requestCategory).stream()
                    .filter(filter.samplePredicate(requestCategory)));
          }
        })
        .toList();
    for (String sampleId : expectedSamples) {
      assertTrue(samples.stream().anyMatch(sample -> sample.getId().equals(sampleId)),
          "Sample %s included for %s filter %s: %s".formatted(sampleId, requestCategory.name(),
              filter.getKey().name(),
              filter.getValue()));
    }
    assertEquals(expectedSamples.size(), samples.size(),
        "Sample count for %s filter %s: %s".formatted(requestCategory.name(),
            filter.getKey().name(), filter.getValue()));
  }

  private static void testIncompleteSampleFilters(String filterValue, long... caseNumbers) {
    CaseFilter filter = new CaseFilter(CaseFilterKey.INCOMPLETE, filterValue);
    for (MetricCategory category : SAMPLE_CATEGORIES) {
      testFilterSamples(filter, category, makeAllSampleIdsForCases(category, caseNumbers));
    }
  }

  private static void testIncompleteSampleFiltersIncludingTests(String filterValue,
      TestGroupId... testGroupIds) {
    CaseFilter filter = new CaseFilter(CaseFilterKey.INCOMPLETE, filterValue);
    for (MetricCategory category : SAMPLE_CATEGORIES) {
      testFilterSamples(filter, category, makeAllSampleIdsForTests(category, testGroupIds));
    }
  }

  private static List<Sample> getSamples(Test test, MetricCategory requestCategory) {
    switch (requestCategory) {
      case EXTRACTION:
        return test.getExtractions();
      case LIBRARY_PREP:
        return test.getLibraryPreparations();
      case LIBRARY_QUALIFICATION:
        return test.getLibraryQualifications();
      case FULL_DEPTH_SEQUENCING:
        return test.getFullDepthSequencings();
      default:
        throw new IllegalArgumentException(
            "Unhandled metric category: %s".formatted(requestCategory));
    }
  }

  private static String makeTestGroupId(int caseNumber, int testNumber) {
    return "C%dT%d".formatted(caseNumber, testNumber);
  }

  private static List<String> makeAllTestGroupIdsForCases(long... caseNumbers) {
    return streamTestsFromCaseNumbers(caseNumbers)
        .map(Test::getGroupId)
        .toList();
  }

  private static Stream<Case> streamCasesFromCaseNumbers(long... caseNumbers) {
    return Arrays.stream(caseNumbers)
        .mapToObj(CaseFilterTest::getCaseByNumber);
  }

  private static Case getCaseByNumber(long caseNumber) {
    return cases.stream()
        .filter(kase -> Objects.equals(caseNumber, kase.getRequisition().getId()))
        .findFirst().orElseThrow();
  }

  private static Stream<Test> streamTestsFromCaseNumbers(long... caseNumbers) {
    return streamCasesFromCaseNumbers(caseNumbers)
        .flatMap(kase -> kase.getTests().stream());
  }

  private static List<String> makeAllSampleIdsForCases(MetricCategory category,
      long... caseNumbers) {
    if (category == MetricCategory.RECEIPT) {
      return streamCasesFromCaseNumbers(caseNumbers)
          .flatMap(kase -> kase.getReceipts().stream())
          .map(Sample::getId)
          .toList();
    }
    return streamTestsFromCaseNumbers(caseNumbers)
        .flatMap(getAllSamplesFromTest(category))
        .map(Sample::getId)
        .toList();
  }

  private static Function<Test, Stream<Sample>> getAllSamplesFromTest(MetricCategory category) {
    return test -> {
      switch (category) {
        case EXTRACTION:
          return test.getExtractions().stream();
        case LIBRARY_PREP:
          return test.getLibraryPreparations().stream();
        case LIBRARY_QUALIFICATION:
          return test.getLibraryQualifications().stream();
        case FULL_DEPTH_SEQUENCING:
          return test.getFullDepthSequencings().stream();
        default:
          throw new IllegalArgumentException("Unexpected category: %s".formatted(category));
      }
    };
  }

  private static record TestGroupId(int caseNumber, int testNumber) {
  };

  private static Set<String> makeAllSampleIdsForTests(MetricCategory category,
      TestGroupId... testGroupIds) {
    return Arrays.stream(testGroupIds)
        .flatMap(testGroupId -> {
          Case kase = getCaseByNumber(testGroupId.caseNumber());
          if (category == MetricCategory.RECEIPT) {
            return kase.getReceipts().stream();
          }
          String testGroupIdString =
              makeTestGroupId(testGroupId.caseNumber(), testGroupId.testNumber());
          Test test = kase.getTests().stream()
              .filter(x -> Objects.equals(testGroupIdString, x.getGroupId()))
              .findFirst().orElseThrow();
          return getAllSamplesFromTest(category).apply(test);
        })
        .map(Sample::getId)
        .collect(Collectors.toSet());
  }

  private static String makeSampleId(int caseNumber, int testNumber, MetricCategory gate,
      int sampleNumber) {
    return "C%dT%dG%dS%d".formatted(caseNumber, testNumber, gate.ordinal(), sampleNumber);
  }

  private static final String concatGateAndDeliverable(CompletedGate gate,
      String deliverableCategory) {
    return gate.getLabel() + " - " + deliverableCategory;
  }

  private static final String concatStateAndDeliverable(PendingState state,
      String deliverableCategory) {
    return state.getLabel() + " - " + deliverableCategory;
  }
}
