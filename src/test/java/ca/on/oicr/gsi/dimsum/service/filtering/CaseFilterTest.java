package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import ca.on.oicr.gsi.dimsum.data.Assay;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.Donor;
import ca.on.oicr.gsi.dimsum.data.MetricCategory;
import ca.on.oicr.gsi.dimsum.data.Project;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.RequisitionQc;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;

public class CaseFilterTest {

  private static List<Case> cases =
      Arrays.asList(makeCase0(), makeCase1(), makeCase2(), makeCase3(), makeCase4(), makeCase5(),
          makeCase6(), makeCase7(), makeCase8(), makeCase9(), makeCase10(), makeCase11(),
          makeCase12(), makeCase13(), makeCase14(), makeCase15(), makeCase16(), makeCase17(),
          makeCase18(), makeCase19(), makeCase20(), makeCase21(), makeCase22());

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
        .flatMap(kase -> kase.getRequisitions().stream()
            .filter(filter.requisitionPredicate()))
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

  private static Case makeCase0() {
    final int caseNumber = 0;
    Case kase = makeCase("PRO1_0001", "WGTS assay 1", "PRO1", "REQ01", caseNumber);
    // Test 1 is pending extraction
    Test test1 = addTest(kase, 0, 1, "Normal WG");
    // Test 2 is pending extraction QC
    Test test2 = addTest(kase, 0, 2, "Tumour WG");
    String test2ExtractionId = makeSampleId(caseNumber, 2, MetricCategory.EXTRACTION, 1);
    addSample(test2.getExtractions(), test2ExtractionId, null, null);
    // Test 3 is pending library prep and has an extra failed extraction
    Test test3 = addTest(kase, caseNumber, 3, "Tumour WT", true, false, false, false);
    String test3ExtractionId = makeSampleId(caseNumber, 3, MetricCategory.EXTRACTION, 2);
    addSample(test3.getExtractions(), test3ExtractionId, false, "Bad");
    return kase;
  }

  private static Case makeCase1() {
    final int caseNumber = 1;
    Case kase = makeCase("PRO2_0001", "WGTS assay 2", "PRO2", "REQ02", caseNumber);
    // Test 1 is pending library QC
    Test test1 = addTest(kase, caseNumber, 1, "Normal WG", true, false, false, false);
    String test1LibraryId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), test1LibraryId, null, null);
    // Test 2 is pending library qualification
    Test test2 = addTest(kase, caseNumber, 2, "Tumour WG", true, true, false, false);
    // Test 3 is pending library qualification QC
    Test test3 = addTest(kase, caseNumber, 3, "Tumour WT", true, true, false, false);
    String test3LibQualId = makeSampleId(caseNumber, 3, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addSample(test3.getLibraryQualifications(), test3LibQualId, null, null);
    return kase;
  }

  private static Case makeCase2() {
    final int caseNumber = 2;
    Case kase = makeCase("PRO1_0001", "WG assay 1", "PRO1", "REQ03", caseNumber);
    // Test 1 is pending library qualification data review
    Test test1 = addTest(kase, caseNumber, 1, "Normal WG", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, true, "Good", null);
    // Test 2 is pending full depth sequencing
    Test test2 = addTest(kase, caseNumber, 2, "Tumour WG", true, true, true, false);
    return kase;
  }

  private static Case makeCase3() {
    final int caseNumber = 3;
    Case kase = makeCase("PRO2_0002", "WG assay 2", "PRO2", "REQ02", caseNumber);
    // Test 1 is pending full depth QC
    Test test1 = addTest(kase, caseNumber, 1, "Normal WG", true, true, true, false);
    String test1SampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), test1SampleId, null, null, null);
    // Test 2 is pending full depth data review
    Test test2 = addTest(kase, caseNumber, 2, "Tumour WG", true, true, true, false);
    String test2SampleId = makeSampleId(caseNumber, 2, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test2.getFullDepthSequencings(), test2SampleId, true, "Good", null);
    return kase;
  }

  private static Case makeCase4() {
    final int caseNumber = 4;
    // Case is pending informatics review
    Case kase = makeCase("PRO3_0001", "Single Test", "PRO3", "REQ04", caseNumber);
    addTest(kase, caseNumber, 1, "Test", true, true, true, true);
    return kase;
  }

  private static Case makeCase5() {
    final int caseNumber = 5;
    // Case is pending draft report
    Case kase = makeCase("PRO4_0001", "Single Test", "PRO4", "REQ04", caseNumber);
    addTest(kase, caseNumber, 1, "Test", true, true, true, true);
    Requisition requisition = kase.getRequisitions().get(0);
    addRequisitionQc(requisition.getInformaticsReviews(), true);
    return kase;
  }

  private static Case makeCase6() {
    final int caseNumber = 6;
    // Case is pending final report
    Case kase = makeCase("PRO5_0001", "Single Test", "PRO5", "REQ04", caseNumber);
    addTest(kase, caseNumber, 1, "Test", true, true, true, true);
    Requisition requisition = kase.getRequisitions().get(0);
    addRequisitionQc(requisition.getInformaticsReviews(), true);
    addRequisitionQc(requisition.getDraftReports(), true);
    return kase;
  }

  private static Case makeCase7() {
    final int caseNumber = 7;
    Case kase = makeCase("PRO7_0001", "Single Test", "PRO7", "REQ07", caseNumber);
    // Test 1 is pending extraction
    Test test1 = addTest(kase, caseNumber, 1, "Test", false, false, false, false);
    return kase;
  }

  private static Case makeCase8() {
    final int caseNumber = 8;
    Case kase = makeCase("PRO8_0001", "Single Test", "PRO8", "REQ08", caseNumber);
    // Test 1 is pending extraction QC
    Test test1 = addTest(kase, caseNumber, 1, "Test", false, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.EXTRACTION, 1);
    addSample(test1.getExtractions(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase9() {
    final int caseNumber = 9;
    Case kase = makeCase("PRO9_0001", "Single Test", "PRO9", "REQ09", caseNumber);
    // Test 1 is pending library prep
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, false, false, false);
    return kase;
  }

  private static Case makeCase10() {
    final int caseNumber = 10;
    Case kase = makeCase("PRO10_0001", "Single Test", "PRO10", "REQ10", caseNumber);
    // Test 1 is pending library QC
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase11() {
    final int caseNumber = 11;
    Case kase = makeCase("PRO11_0001", "Single Test", "PRO11", "REQ11", caseNumber);
    // Test 1 is pending library qualification
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    return kase;
  }

  private static Case makeCase12() {
    final int caseNumber = 12;
    Case kase = makeCase("PRO12_0001", "Single Test", "PRO12", "REQ12", caseNumber);
    // Test 1 is pending library qualification (top-up required)
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, null, "Top-up Required", true);
    return kase;
  }

  private static Case makeCase13() {
    final int caseNumber = 13;
    Case kase = makeCase("PRO13_0001", "Single Test", "PRO13", "REQ13", caseNumber);
    // Test 1 is pending library qualification QC (library aliquot)
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addSample(test1.getLibraryQualifications(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase14() {
    final int caseNumber = 14;
    Case kase = makeCase("PRO14_0001", "Single Test", "PRO14", "REQ14", caseNumber);
    // Test 1 is pending library qualification QC (run-library)
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, null, null, null);
    return kase;
  }

  private static Case makeCase15() {
    final int caseNumber = 15;
    Case kase = makeCase("PRO15_0001", "Single Test", "PRO15", "REQ15", caseNumber);
    // Test 1 is pending library qualification data review
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, true, "Good", null);
    return kase;
  }

  private static Case makeCase16() {
    final int caseNumber = 16;
    Case kase = makeCase("PRO16_0001", "Single Test", "PRO16", "REQ16", caseNumber);
    // Test 1 is pending full depth sequencing
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, true, false);
    return kase;
  }

  private static Case makeCase17() {
    final int caseNumber = 17;
    Case kase = makeCase("PRO17_0001", "Single Test", "PRO17", "REQ17", caseNumber);
    // Test 1 is pending full depth sequencing (top-up required)
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, null, "Top-up Required", true);
    return kase;
  }

  private static Case makeCase18() {
    final int caseNumber = 18;
    Case kase = makeCase("PRO18_0001", "Single Test", "PRO18", "REQ18", caseNumber);
    // Test 1 is pending full depth sequencing QC
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, null, null, null);
    return kase;
  }

  private static Case makeCase19() {
    final int caseNumber = 19;
    Case kase = makeCase("PRO19_0001", "Single Test", "PRO19", "REQ19", caseNumber);
    // Test 1 is pending full depth sequencing data review
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, true, "Good", null);
    return kase;
  }

  private static Case makeCase20() {
    final int caseNumber = 20;
    Case kase = makeCase("PRO20_0001", "Single Test", "PRO20", "REQ20", caseNumber);
    // Test 1 is pending library prep (1 failed attempt already)
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), sampleId, false, "Bad");
    return kase;
  }

  private static Case makeCase21() {
    final int caseNumber = 21;
    Case kase = makeCase("PRO21_0001", "Single Test", "PRO21", "REQ21", caseNumber);
    // Test 1 is pending library qualification (with 1 failed and 1 passed library prep)
    Test test1 = addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 2);
    addSample(test1.getLibraryPreparations(), sampleId, false, "Bad");
    return kase;
  }

  private static Case makeCase22() {
    final int caseNumber = 22;
    Case kase = makeCase("PRO22_0001", "Single Test", "PRO22", "REQ22", caseNumber);
    // Case is pending receipt QC (replace default passed receipt)
    kase.getReceipts().remove(0);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.RECEIPT, 1);
    addSample(kase.getReceipts(), sampleId, null, null);
    Test test1 = addTest(kase, caseNumber, 1, "Test", false, false, false, false);
    return kase;
  }

  private static Case makeCase(String donorName, String assayName, String projectName,
      String requisitionName, int caseNumber) {
    Case kase = mock(Case.class);
    Donor donor = mock(Donor.class);
    when(donor.getName()).thenReturn(donorName);
    when(donor.getExternalName()).thenReturn(donorName);
    when(kase.getDonor()).thenReturn(donor);
    Assay assay = mock(Assay.class);
    when(assay.getName()).thenReturn(assayName);
    when(assay.getDescription()).thenReturn(assayName);
    when(kase.getAssay()).thenReturn(assay);
    when(kase.getProjects()).thenReturn(new HashSet<>());
    kase.getProjects().add(makeProject(projectName));
    when(kase.getReceipts()).thenReturn(new ArrayList<>());
    String receiptSampleId = makeSampleId(caseNumber, 0, MetricCategory.RECEIPT, 1);
    addSample(kase.getReceipts(), receiptSampleId, true, "Good");
    when(kase.getTests()).thenReturn(new ArrayList<>());
    when(kase.getRequisitions()).thenReturn(new ArrayList<>());
    addRequisition(kase, caseNumber, requisitionName);
    return kase;
  }

  private static String makeSampleId(int caseNumber, int testNumber, MetricCategory gate,
      int sampleNumber) {
    return "C%dT%dG%dS%d".formatted(caseNumber, testNumber, gate.ordinal(), sampleNumber);
  }

  private static Project makeProject(String name) {
    Project project = mock(Project.class);
    when(project.getName()).thenReturn(name);
    return project;
  }

  private static Requisition addRequisition(Case kase, int caseNumber, String name) {
    Requisition requisition = mock(Requisition.class);
    when(requisition.getId()).thenReturn(Long.valueOf(caseNumber));
    when(requisition.getName()).thenReturn(name);
    when(requisition.getInformaticsReviews()).thenReturn(new ArrayList<>());
    when(requisition.getDraftReports()).thenReturn(new ArrayList<>());
    when(requisition.getFinalReports()).thenReturn(new ArrayList<>());
    kase.getRequisitions().add(requisition);
    return requisition;
  }

  private static void addRequisitionQc(List<RequisitionQc> qcs, boolean qcPassed) {
    RequisitionQc qc = mock(RequisitionQc.class);
    when(qc.isQcPassed()).thenReturn(qcPassed);
    when(qc.getQcUser()).thenReturn("User");
    when(qc.getQcDate()).thenReturn(LocalDate.now());
    qcs.add(qc);
  }

  private static Test addTest(Case kase, int caseNumber, int testNumber, String name) {
    Test test = mock(Test.class);
    when(test.getName()).thenReturn(name);
    when(test.getGroupId()).thenReturn(makeTestGroupId(caseNumber, testNumber));
    when(test.getExtractions()).thenReturn(new ArrayList<>());
    when(test.getLibraryPreparations()).thenReturn(new ArrayList<>());
    when(test.getLibraryQualifications()).thenReturn(new ArrayList<>());
    when(test.getFullDepthSequencings()).thenReturn(new ArrayList<>());
    kase.getTests().add(test);
    return test;
  }

  private static Test addTest(Case kase, int caseNumber, int testNumber, String name,
      boolean extractionComplete, boolean libraryPrepComplete, boolean libraryQualificationComplete,
      boolean fullDepthComplete) {
    Test test = addTest(kase, caseNumber, testNumber, name);
    if (extractionComplete) {
      String extractionId = makeSampleId(caseNumber, testNumber, MetricCategory.EXTRACTION, 1);
      addSample(test.getExtractions(), extractionId, true, "Good");
      if (libraryPrepComplete) {
        String libraryId = makeSampleId(caseNumber, testNumber, MetricCategory.LIBRARY_PREP, 1);
        addSample(test.getLibraryPreparations(), libraryId, true, "Good");
        if (libraryQualificationComplete) {
          String libQualId =
              makeSampleId(caseNumber, testNumber, MetricCategory.LIBRARY_QUALIFICATION, 1);
          addRunLibrary(test.getLibraryQualifications(), libQualId, true, "Good", true);
          if (fullDepthComplete) {
            String fullDepthId =
                makeSampleId(caseNumber, testNumber, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
            addRunLibrary(test.getFullDepthSequencings(), fullDepthId, true, "Good", true);
          }
        }
      }
    }
    return test;
  }

  private static String makeTestGroupId(int caseNumber, int testNumber) {
    return "C%dT%d".formatted(caseNumber, testNumber);
  }

  private static Sample addSample(List<Sample> gateItems, String id, Boolean qcPassed,
      String qcReason) {
    Sample sample = mock(Sample.class);
    when(sample.getId()).thenReturn(id);
    when(sample.getQcPassed()).thenReturn(qcPassed);
    when(sample.getQcReason()).thenReturn(qcReason);
    if (qcPassed != null || qcReason != null) {
      when(sample.getQcUser()).thenReturn("User");
      when(sample.getQcDate()).thenReturn(LocalDate.now());
    }
    gateItems.add(sample);
    return sample;
  }

  private static Sample addRunLibrary(List<Sample> gateItems, String id, Boolean qcPassed,
      String qcReason, Boolean dataReviewPassed) {
    Sample sample = addSample(gateItems, id, qcPassed, qcReason);
    Run run = mock(Run.class);
    when(sample.getRun()).thenReturn(run);
    when(sample.getDataReviewPassed()).thenReturn(dataReviewPassed);
    if (dataReviewPassed != null) {
      when(sample.getDataReviewUser()).thenReturn("User");
      when(sample.getDataReviewDate()).thenReturn(LocalDate.now());
    }
    return sample;
  }

}
