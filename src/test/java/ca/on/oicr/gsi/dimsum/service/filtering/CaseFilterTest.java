package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.Donor;
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
    testFilter(filter, Arrays.asList(0, 1));
  }

  @org.junit.jupiter.api.Test
  public void testDonorFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.DONOR, "PRO1_0001");
    testFilter(filter, Arrays.asList(0, 2));
  }

  @org.junit.jupiter.api.Test
  public void testProjectFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PROJECT, "PRO2");
    testFilter(filter, Arrays.asList(1, 3));
  }

  @org.junit.jupiter.api.Test
  public void testRequisitionFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.REQUISITION, "REQ02");
    testFilter(filter, Arrays.asList(1, 3));
  }

  @org.junit.jupiter.api.Test
  public void testPendingReceiptQcFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.RECEIPT_QC.getLabel());
    testFilter(filter, Arrays.asList(22));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION.getLabel());
    testFilter(filter, Arrays.asList(0, 7));
  }

  @org.junit.jupiter.api.Test
  public void testPendingExtractionQcFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.EXTRACTION_QC.getLabel());
    testFilter(filter, Arrays.asList(0, 8));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryPrepFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_PREPARATION.getLabel());
    testFilter(filter, Arrays.asList(0, 9, 20));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQcFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QC.getLabel());
    testFilter(filter, Arrays.asList(1, 10));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QUALIFICATION.getLabel());
    testFilter(filter, Arrays.asList(1, 11, 12, 21));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationQCFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.LIBRARY_QUALIFICATION_QC.getLabel());
    testFilter(filter, Arrays.asList(1, 13, 14));
  }

  @org.junit.jupiter.api.Test
  public void testPendingLibraryQualificationDataReviewFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING,
        PendingState.LIBRARY_QUALIFICATION_DATA_REVIEW.getLabel());
    testFilter(filter, Arrays.asList(2, 15));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_SEQUENCING.getLabel());
    testFilter(filter, Arrays.asList(2, 16, 17));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthQcFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_QC.getLabel());
    testFilter(filter, Arrays.asList(3, 18));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFullDepthDataReviewFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.FULL_DEPTH_DATA_REVIEW.getLabel());
    testFilter(filter, Arrays.asList(3, 19));
  }

  @org.junit.jupiter.api.Test
  public void testPendingInformaticsFilter() {
    CaseFilter filter =
        new CaseFilter(CaseFilterKey.PENDING, PendingState.INFORMATICS_REVIEW.getLabel());
    testFilter(filter, Arrays.asList(4));
  }

  @org.junit.jupiter.api.Test
  public void testPendingDraftReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.DRAFT_REPORT.getLabel());
    testFilter(filter, Arrays.asList(5));
  }

  @org.junit.jupiter.api.Test
  public void testPendingFinalReportFilter() {
    CaseFilter filter = new CaseFilter(CaseFilterKey.PENDING, PendingState.FINAL_REPORT.getLabel());
    testFilter(filter, Arrays.asList(6));
  }

  private static List<Case> getCasesFiltered(CaseFilter filter) {
    return cases.stream().filter(filter.predicate()).toList();
  }

  private static void testFilter(CaseFilter filter, List<Integer> expectedCases) {
    List<Case> filtered = getCasesFiltered(filter);
    for (int caseNumber : expectedCases) {
      assertTrue(filtered.contains(cases.get(caseNumber)),
          String.format("Case #%d included", caseNumber));
    }
    assertEquals(expectedCases.size(), filtered.size(), "Case count");
  }

  private static Case makeCase0() {
    Case kase = makeCase("PRO1_0001", "WGTS assay 1", "PRO1", "REQ01");
    // Test 1 is pending extraction
    Test test1 = addTest(kase, "Normal WG");
    // Test 2 is pending extraction QC
    Test test2 = addTest(kase, "Tumour WG");
    addSample(test2.getExtractions(), null, null);
    // Test 3 is pending library prep
    Test test3 = addTest(kase, "Tumour WT", true, false, false, false);
    addSample(test3.getExtractions(), false, "Bad");
    return kase;
  }

  private static Case makeCase1() {
    Case kase = makeCase("PRO2_0001", "WGTS assay 2", "PRO2", "REQ02");
    // Test 1 is pending library QC
    Test test1 = addTest(kase, "Normal WG", true, false, false, false);
    addSample(test1.getLibraryPreparations(), null, null);
    // Test 2 is pending library qualification
    Test test2 = addTest(kase, "Tumour WG", true, true, false, false);
    // Test 3 is pending library qualification QC
    Test test3 = addTest(kase, "Tumour WT", true, true, false, false);
    addSample(test3.getLibraryQualifications(), null, null);
    return kase;
  }

  private static Case makeCase2() {
    Case kase = makeCase("PRO1_0001", "WG assay 1", "PRO1", "REQ03");
    // Test 1 is pending library qualification data review
    Test test1 = addTest(kase, "Normal WG", true, true, false, false);
    addRunLibrary(test1.getLibraryQualifications(), true, "Good", null);
    // Test 2 is pending full depth sequencing
    Test test2 = addTest(kase, "Tumour WG", true, true, true, false);
    return kase;
  }

  private static Case makeCase3() {
    Case kase = makeCase("PRO2_0002", "WG assay 2", "PRO2", "REQ02");
    // Test 1 is pending full depth QC
    Test test1 = addTest(kase, "Normal WG", true, true, true, false);
    addRunLibrary(test1.getFullDepthSequencings(), null, null, null);
    // Test 2 is pending full depth data review
    Test test2 = addTest(kase, "Tumour WG", true, true, true, false);
    addRunLibrary(test2.getFullDepthSequencings(), true, "Good", null);
    return kase;
  }

  private static Case makeCase4() {
    // Case is pending informatics review
    Case kase = makeCase("PRO3_0001", "Single Test", "PRO3", "REQ04");
    addTest(kase, "Test", true, true, true, true);
    return kase;
  }

  private static Case makeCase5() {
    // Case is pending draft report
    Case kase = makeCase("PRO4_0001", "Single Test", "PRO4", "REQ04");
    addTest(kase, "Test", true, true, true, true);
    Requisition requisition = kase.getRequisitions().get(0);
    addRequisitionQc(requisition.getInformaticsReviews(), true);
    return kase;
  }

  private static Case makeCase6() {
    // Case is pending final report
    Case kase = makeCase("PRO5_0001", "Single Test", "PRO5", "REQ04");
    addTest(kase, "Test", true, true, true, true);
    Requisition requisition = kase.getRequisitions().get(0);
    addRequisitionQc(requisition.getInformaticsReviews(), true);
    addRequisitionQc(requisition.getDraftReports(), true);
    return kase;
  }

  private static Case makeCase7() {
    Case kase = makeCase("PRO7_0001", "Single Test", "PRO7", "REQ07");
    // Test 1 is pending extraction
    Test test1 = addTest(kase, "Test", false, false, false, false);
    return kase;
  }

  private static Case makeCase8() {
    Case kase = makeCase("PRO8_0001", "Single Test", "PRO8", "REQ08");
    // Test 1 is pending extraction QC
    Test test1 = addTest(kase, "Test", false, false, false, false);
    addSample(test1.getExtractions(), null, null);
    return kase;
  }

  private static Case makeCase9() {
    Case kase = makeCase("PRO9_0001", "Single Test", "PRO9", "REQ09");
    // Test 1 is pending library prep
    Test test1 = addTest(kase, "Test", true, false, false, false);
    return kase;
  }

  private static Case makeCase10() {
    Case kase = makeCase("PRO10_0001", "Single Test", "PRO10", "REQ10");
    // Test 1 is pending library QC
    Test test1 = addTest(kase, "Test", true, false, false, false);
    addSample(test1.getLibraryPreparations(), null, null);
    return kase;
  }

  private static Case makeCase11() {
    Case kase = makeCase("PRO11_0001", "Single Test", "PRO11", "REQ11");
    // Test 1 is pending library qualification
    Test test1 = addTest(kase, "Test", true, true, false, false);
    return kase;
  }

  private static Case makeCase12() {
    Case kase = makeCase("PRO12_0001", "Single Test", "PRO12", "REQ12");
    // Test 1 is pending library qualification (top-up required)
    Test test1 = addTest(kase, "Test", true, true, false, false);
    addRunLibrary(test1.getLibraryQualifications(), null, "Top-up Required", true);
    return kase;
  }

  private static Case makeCase13() {
    Case kase = makeCase("PRO13_0001", "Single Test", "PRO13", "REQ13");
    // Test 1 is pending library qualification QC (library aliquot)
    Test test1 = addTest(kase, "Test", true, true, false, false);
    addSample(test1.getLibraryQualifications(), null, null);
    return kase;
  }

  private static Case makeCase14() {
    Case kase = makeCase("PRO14_0001", "Single Test", "PRO14", "REQ14");
    // Test 1 is pending library qualification QC (run-library)
    Test test1 = addTest(kase, "Test", true, true, false, false);
    addRunLibrary(test1.getLibraryQualifications(), null, null, null);
    return kase;
  }

  private static Case makeCase15() {
    Case kase = makeCase("PRO15_0001", "Single Test", "PRO15", "REQ15");
    // Test 1 is pending library qualification data review
    Test test1 = addTest(kase, "Test", true, true, false, false);
    addRunLibrary(test1.getLibraryQualifications(), true, "Good", null);
    return kase;
  }

  private static Case makeCase16() {
    Case kase = makeCase("PRO16_0001", "Single Test", "PRO16", "REQ16");
    // Test 1 is pending full depth sequencing
    Test test1 = addTest(kase, "Test", true, true, true, false);
    return kase;
  }

  private static Case makeCase17() {
    Case kase = makeCase("PRO17_0001", "Single Test", "PRO17", "REQ17");
    // Test 1 is pending full depth sequencing (top-up required)
    Test test1 = addTest(kase, "Test", true, true, true, false);
    addRunLibrary(test1.getFullDepthSequencings(), null, "Top-up Required", true);
    return kase;
  }

  private static Case makeCase18() {
    Case kase = makeCase("PRO18_0001", "Single Test", "PRO18", "REQ18");
    // Test 1 is pending full depth sequencing QC
    Test test1 = addTest(kase, "Test", true, true, true, false);
    addRunLibrary(test1.getFullDepthSequencings(), null, null, null);
    return kase;
  }

  private static Case makeCase19() {
    Case kase = makeCase("PRO19_0001", "Single Test", "PRO19", "REQ19");
    // Test 1 is pending full depth sequencing data review
    Test test1 = addTest(kase, "Test", true, true, true, false);
    addRunLibrary(test1.getFullDepthSequencings(), true, "Good", null);
    return kase;
  }

  private static Case makeCase20() {
    Case kase = makeCase("PRO20_0001", "Single Test", "PRO20", "REQ20");
    // Test 1 is pending library prep (1 failed attempt already)
    Test test1 = addTest(kase, "Test", true, false, false, false);
    addSample(test1.getLibraryPreparations(), false, "Bad");
    return kase;
  }

  private static Case makeCase21() {
    Case kase = makeCase("PRO21_0001", "Single Test", "PRO21", "REQ21");
    // Test 1 is pending library qualification (with 1 failed and 1 passed library prep)
    Test test1 = addTest(kase, "Test", true, true, false, false);
    addSample(test1.getLibraryPreparations(), false, "Bad");
    return kase;
  }

  private static Case makeCase22() {
    Case kase = makeCase("PRO22_0001", "Single Test", "PRO22", "REQ22");
    // Case is pending receipt QC (replace default passed receipt)
    kase.getReceipts().remove(0);
    addSample(kase.getReceipts(), null, null);
    Test test1 = addTest(kase, "Test", false, false, false, false);
    return kase;
  }

  private static Case makeCase(String donorName, String assayName, String projectName,
      String requisitionName) {
    Case kase = mock(Case.class);
    Donor donor = mock(Donor.class);
    when(donor.getName()).thenReturn(donorName);
    when(donor.getExternalName()).thenReturn(donorName);
    when(kase.getDonor()).thenReturn(donor);
    when(kase.getAssayName()).thenReturn(assayName);
    when(kase.getAssayDescription()).thenReturn(assayName);
    when(kase.getProjects()).thenReturn(new HashSet<>());
    kase.getProjects().add(makeProject(projectName));
    when(kase.getReceipts()).thenReturn(new ArrayList<>());
    addSample(kase.getReceipts(), true, "Good");
    when(kase.getTests()).thenReturn(new ArrayList<>());
    when(kase.getRequisitions()).thenReturn(new ArrayList<>());
    addRequisition(kase, requisitionName);
    return kase;
  }

  private static Project makeProject(String name) {
    Project project = mock(Project.class);
    when(project.getName()).thenReturn(name);
    return project;
  }

  private static Requisition addRequisition(Case kase, String name) {
    Requisition requisition = mock(Requisition.class);
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

  private static Test addTest(Case kase, String name) {
    Test test = mock(Test.class);
    when(test.getName()).thenReturn(name);
    when(test.getExtractions()).thenReturn(new ArrayList<>());
    when(test.getLibraryPreparations()).thenReturn(new ArrayList<>());
    when(test.getLibraryQualifications()).thenReturn(new ArrayList<>());
    when(test.getFullDepthSequencings()).thenReturn(new ArrayList<>());
    kase.getTests().add(test);
    return test;
  }

  private static Test addTest(Case kase, String name, boolean extractionComplete,
      boolean libraryPrepComplete, boolean libraryQualificationComplete,
      boolean fullDepthComplete) {
    Test test = addTest(kase, name);
    if (extractionComplete) {
      addSample(test.getExtractions(), true, "Good");
      if (libraryPrepComplete) {
        addSample(test.getLibraryPreparations(), true, "Good");
        if (libraryQualificationComplete) {
          addRunLibrary(test.getLibraryQualifications(), true, "Good", true);
          if (fullDepthComplete) {
            addRunLibrary(test.getFullDepthSequencings(), true, "Good", true);
          }
        }
      }
    }
    return test;
  }

  private static Sample addSample(List<Sample> gateItems, Boolean qcPassed, String qcReason) {
    Sample sample = mock(Sample.class);
    when(sample.getQcPassed()).thenReturn(qcPassed);
    if (qcReason != null) {
      when(sample.getQcReason()).thenReturn(qcReason);
      when(sample.getQcUser()).thenReturn("User");
      when(sample.getQcDate()).thenReturn(LocalDate.now());
    }
    gateItems.add(sample);
    return sample;
  }

  private static Sample addRunLibrary(List<Sample> gateItems, Boolean qcPassed, String qcReason,
      Boolean dataReviewPassed) {
    Sample sample = addSample(gateItems, qcPassed, qcReason);
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
