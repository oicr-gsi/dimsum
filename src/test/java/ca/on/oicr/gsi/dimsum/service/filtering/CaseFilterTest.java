package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.MetricCategory;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;
import ca.on.oicr.gsi.dimsum.MockCase;

public class CaseFilterTest {


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
    testFilterCases(filter, Arrays.asList(0, 7));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionTestFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(0, 1),
        makeTestGroupId(7, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionSampleFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION.getLabel());
    testFilterSamples(filter, MetricCategory.RECEIPT, Arrays.asList(
        makeSampleId(0, 0, MetricCategory.RECEIPT, 1),
        makeSampleId(7, 0, MetricCategory.RECEIPT, 1)));
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
  public void testPendingInformaticsFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.INFORMATICS_REVIEW.getLabel());
    testFilterCases(filter, Arrays.asList(4));
  }

  @org.junit.jupiter.api.Test
  public void testPendingInformaticsRequisitionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.INFORMATICS_REVIEW.getLabel());
    testFilterRequisitions(filter, Arrays.asList(4L));
  }

  @org.junit.jupiter.api.Test
  public void testPendingDraftReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.DRAFT_REPORT.getLabel());
    testFilterCases(filter, Arrays.asList(5));
  }

  @org.junit.jupiter.api.Test
  public void testPendingDraftReportRequisitionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.DRAFT_REPORT.getLabel());
    testFilterRequisitions(filter, Arrays.asList(5L));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFinalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.FINAL_REPORT.getLabel());
    testFilterCases(filter, Arrays.asList(6));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFinalReportRequisitionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FINAL_REPORT.getLabel());
    testFilterRequisitions(filter, Arrays.asList(6L));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedReceiptFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.RECEIPT.getLabel());
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21));
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
        makeTestGroupId(21, 1)));
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
            makeSampleId(21, 0, MetricCategory.RECEIPT, 1)));
  }


  @org.junit.jupiter.api.Test
  public void testCompletedExtractionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.EXTRACTION.getLabel());
    testFilterCases(filter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedExtractionTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.EXTRACTION.getLabel());
    testFilterTests(filter, Arrays.asList(
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
        makeTestGroupId(21, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedExtractionSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.EXTRACTION.getLabel());
    testFilterSamples(filter, MetricCategory.EXTRACTION, Arrays.asList(
        makeSampleId(0, 3, MetricCategory.EXTRACTION, 1),
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
        makeSampleId(21, 1, MetricCategory.EXTRACTION, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryPrepFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.LIBRARY_PREPARATION.getLabel());
    testFilterCases(filter, Arrays.asList(1, 2, 3, 4, 5, 6, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 21));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryPrepTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_PREPARATION.getLabel());
    testFilterTests(filter, Arrays.asList(
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
        makeTestGroupId(11, 1),
        makeTestGroupId(12, 1),
        makeTestGroupId(13, 1),
        makeTestGroupId(14, 1),
        makeTestGroupId(15, 1),
        makeTestGroupId(16, 1),
        makeTestGroupId(17, 1),
        makeTestGroupId(18, 1),
        makeTestGroupId(19, 1),
        makeTestGroupId(21, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryPrepSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_PREPARATION.getLabel());
    testFilterSamples(filter, MetricCategory.LIBRARY_PREP, Arrays.asList(
        makeSampleId(1, 2, MetricCategory.LIBRARY_PREP, 1),
        makeSampleId(1, 3, MetricCategory.LIBRARY_PREP, 1),
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
        makeSampleId(21, 1, MetricCategory.LIBRARY_PREP, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryQualFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_QUALIFICATION.getLabel());
    testFilterCases(filter, Arrays.asList(2, 3, 4, 5, 6, 16, 17, 18, 19));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryQualTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_QUALIFICATION.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(2, 2),
        makeTestGroupId(3, 1),
        makeTestGroupId(3, 2),
        makeTestGroupId(4, 1),
        makeTestGroupId(5, 1),
        makeTestGroupId(6, 1),
        makeTestGroupId(6, 2),
        makeTestGroupId(16, 1),
        makeTestGroupId(17, 1),
        makeTestGroupId(18, 1),
        makeTestGroupId(19, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedLibraryQualSampleFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.LIBRARY_QUALIFICATION.getLabel());
    testFilterSamples(filter, MetricCategory.LIBRARY_QUALIFICATION, Arrays.asList(
        makeSampleId(2, 2, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(3, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(3, 2, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(4, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(5, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(6, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(6, 2, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(16, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(17, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(18, 1, MetricCategory.LIBRARY_QUALIFICATION, 1),
        makeSampleId(19, 1, MetricCategory.LIBRARY_QUALIFICATION, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedFullDepthFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterCases(filter, Arrays.asList(4, 5, 6));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedFullDepthTestFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.FULL_DEPTH_SEQUENCING.getLabel());
    testFilterTests(filter, Arrays.asList(
        makeTestGroupId(4, 1),
        makeTestGroupId(5, 1),
        makeTestGroupId(6, 1),
        makeTestGroupId(6, 2)));
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
        makeSampleId(6, 2, MetricCategory.FULL_DEPTH_SEQUENCING, 1)));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedInformaticsFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.INFORMATICS_REVIEW.getLabel());
    testFilterCases(filter, Arrays.asList(5, 6));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedInformaticsRequisitionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.INFORMATICS_REVIEW.getLabel());
    testFilterRequisitions(filter, Arrays.asList(5L, 6L));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedDraftReportFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.DRAFT_REPORT.getLabel());
    testFilterCases(filter, Arrays.asList(6));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedDraftReportRequisitionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.DRAFT_REPORT.getLabel());
    testFilterRequisitions(filter, Arrays.asList(6L));
  }

  @org.junit.jupiter.api.Test
  public void testCompletedFinalReportFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED,
            CompletedGate.FINAL_REPORT.getLabel());
    testFilterCases(filter, Collections.emptyList());
  }

  @org.junit.jupiter.api.Test
  public void testCompletedFinalReportRequisitionFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.COMPLETED, CompletedGate.FINAL_REPORT.getLabel());
    testFilterRequisitions(filter, Collections.emptyList());
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
        17, 18, 19, 20, 21, 22));
  }


  private static List<Case> getCasesFiltered(CaseFilter filter) {
    return cases.stream().filter(filter.casePredicate()).toList();
  }

  private static void testFilterCases(CaseFilter filter, List<Integer> expectedCases) {
    List<Case> filtered = getCasesFiltered(filter);
    for (int caseNumber : expectedCases) {
      assertTrue(filtered.contains(cases.get(caseNumber)),
          String.format("Case #%d included", caseNumber));
    }
    assertEquals(expectedCases.size(), filtered.size(), "Case count");
  }

  private static void testFilterTests(CaseFilter filter, List<String> expectedTestGroupIds) {
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
      List<String> expectedSamples) {
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
          "Sample %s included".formatted(sampleId));
    }
    assertEquals(expectedSamples.size(), samples.size(), "Sample count");
  }

  private static void testFilterRequisitions(CaseFilter filter,
      List<Long> expectedRequisitionIds) {
    List<Requisition> requisitions = cases.stream()
        .filter(filter.casePredicate())
        .map(kase -> kase.getRequisition())
        .filter(filter.requisitionPredicate())
        .toList();
    for (Long id : expectedRequisitionIds) {
      assertTrue(
          requisitions.stream().anyMatch(requisition -> Objects.equals(requisition.getId(), id)),
          "Requisition %s included".formatted(id));
    }
    assertEquals(expectedRequisitionIds.size(), requisitions.size(), "Requisition count");
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

  private static String makeSampleId(int caseNumber, int testNumber, MetricCategory gate,
      int sampleNumber) {
    return "C%dT%dG%dS%d".formatted(caseNumber, testNumber, gate.ordinal(), sampleNumber);
  }
}
