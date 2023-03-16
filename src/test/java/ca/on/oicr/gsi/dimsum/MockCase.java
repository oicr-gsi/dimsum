package ca.on.oicr.gsi.dimsum;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.on.oicr.gsi.dimsum.data.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.time.*;
import java.util.List;

public class MockCase {

  public static List<Case> getCases() {
    return cases;
  }

  private static final List<Case> cases =
      Arrays.asList(makeCase0(), makeCase1(), makeCase2(), makeCase3(), makeCase4(), makeCase5(),
          makeCase6(), makeCase7(), makeCase8(), makeCase9(), makeCase10(), makeCase11(),
          makeCase12(), makeCase13(), makeCase14(), makeCase15(), makeCase16(), makeCase17(),
          makeCase18(), makeCase19(), makeCase20(), makeCase21(), makeCase22(), makeCase23());

  private static Case makeCase0() {
    final int caseNumber = 0;
    Case kase = makeCase("PRO1_0001", "WGTS assay 1", "PRO1", "REQ01", caseNumber);
    // Test 1 is pending extraction
    ca.on.oicr.gsi.dimsum.data.Test test1 = addTest(kase, 0, 1, "Normal WG");
    // Test 2 is pending extraction QC
    ca.on.oicr.gsi.dimsum.data.Test test2 = addTest(kase, 0, 2, "Tumour WG");
    String test2ExtractionId = makeSampleId(caseNumber, 2, MetricCategory.EXTRACTION, 1);
    addSample(test2.getExtractions(), test2ExtractionId, null, null);
    // Test 3 is pending library prep and has an extra failed extraction
    ca.on.oicr.gsi.dimsum.data.Test test3 =
        addTest(kase, caseNumber, 3, "Tumour WT", true, false, false, false);
    String test3ExtractionId = makeSampleId(caseNumber, 3, MetricCategory.EXTRACTION, 2);
    addSample(test3.getExtractions(), test3ExtractionId, false, "Bad");
    return kase;
  }

  private static Case makeCase1() {
    final int caseNumber = 1;
    Case kase = makeCase("PRO2_0001", "WGTS assay 2", "PRO2", "REQ02", caseNumber);
    // Test 1 is pending library QC
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Normal WG", true, false, false, false);
    String test1LibraryId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), test1LibraryId, null, null);
    // Test 2 is pending library qualification
    ca.on.oicr.gsi.dimsum.data.Test test2 =
        addTest(kase, caseNumber, 2, "Tumour WG", true, true, false, false);
    // Test 3 is pending library qualification QC
    ca.on.oicr.gsi.dimsum.data.Test test3 =
        addTest(kase, caseNumber, 3, "Tumour WT", true, true, false, false);
    String test3LibQualId = makeSampleId(caseNumber, 3, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addSample(test3.getLibraryQualifications(), test3LibQualId, null, null);
    return kase;
  }

  private static Case makeCase2() {
    final int caseNumber = 2;
    Case kase = makeCase("PRO1_0001", "WG assay 1", "PRO1", "REQ03", caseNumber);
    // Test 1 is pending library qualification data review
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Normal WG", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, true, "Good", null);
    // Test 2 is pending full depth sequencing
    ca.on.oicr.gsi.dimsum.data.Test test2 =
        addTest(kase, caseNumber, 2, "Tumour WG", true, true, true, false);
    return kase;
  }

  private static Case makeCase3() {
    final int caseNumber = 3;
    Case kase = makeCase("PRO2_0002", "WG assay 2", "PRO2", "REQ02", caseNumber);
    // Test 1 is pending full depth QC
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Normal WG", true, true, true, false);
    String test1SampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), test1SampleId, null, null, null);
    // Test 2 is pending full depth data review
    ca.on.oicr.gsi.dimsum.data.Test test2 =
        addTest(kase, caseNumber, 2, "Tumour WG", true, true, true, false);
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
    Requisition requisition = kase.getRequisition();
    addRequisitionQc(requisition.getInformaticsReviews(), true);
    return kase;
  }

  private static Case makeCase6() {
    final int caseNumber = 6;
    // Case is pending final report
    Case kase = makeCase("PRO5_0001", "Single Test", "PRO5", "REQ04", caseNumber);
    addTest(kase, caseNumber, 1, "Test", true, true, true, true);
    addTest(kase, caseNumber, 2, "Test", true, true, true, true);
    Requisition requisition = kase.getRequisition();
    addRequisitionQc(requisition.getInformaticsReviews(), true);
    addRequisitionQc(requisition.getDraftReports(), true);
    return kase;
  }

  private static Case makeCase7() {
    final int caseNumber = 7;
    Case kase = makeCase("PRO7_0001", "Single Test", "PRO7", "REQ07", caseNumber);
    // Test 1 is pending extraction
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", false, false, false, false);
    return kase;
  }

  private static Case makeCase8() {
    final int caseNumber = 8;
    Case kase = makeCase("PRO8_0001", "Single Test", "PRO8", "REQ08", caseNumber);
    // Test 1 is pending extraction QC
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", false, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.EXTRACTION, 1);
    addSample(test1.getExtractions(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase9() {
    final int caseNumber = 9;
    Case kase = makeCase("PRO9_0001", "Single Test", "PRO9", "REQ09", caseNumber);
    // Test 1 is pending library prep
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, false, false, false);
    return kase;
  }

  private static Case makeCase10() {
    final int caseNumber = 10;
    Case kase = makeCase("PRO10_0001", "Single Test", "PRO10", "REQ10", caseNumber);
    // Test 1 is pending library QC
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase11() {
    final int caseNumber = 11;
    Case kase = makeCase("PRO11_0001", "Single Test", "PRO11", "REQ11", caseNumber);
    // Test 1 is pending library qualification
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    return kase;
  }

  private static Case makeCase12() {
    final int caseNumber = 12;
    Case kase = makeCase("PRO12_0001", "Single Test", "PRO12", "REQ12", caseNumber);
    // Test 1 is pending library qualification (top-up required)
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, null, "Top-up Required", true);
    return kase;
  }

  private static Case makeCase13() {
    final int caseNumber = 13;
    Case kase = makeCase("PRO13_0001", "Single Test", "PRO13", "REQ13", caseNumber);
    // Test 1 is pending library qualification QC (library aliquot)
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addSample(test1.getLibraryQualifications(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase14() {
    final int caseNumber = 14;
    Case kase = makeCase("PRO14_0001", "Single Test", "PRO14", "REQ14", caseNumber);
    // Test 1 is pending library qualification QC (run-library)
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, null, null, null);
    return kase;
  }

  private static Case makeCase15() {
    final int caseNumber = 15;
    Case kase = makeCase("PRO15_0001", "Single Test", "PRO15", "REQ15", caseNumber);
    // Test 1 is pending library qualification data review
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, true, "Good", null);
    return kase;
  }

  private static Case makeCase16() {
    final int caseNumber = 16;
    Case kase = makeCase("PRO16_0001", "Single Test", "PRO16", "REQ16", caseNumber);
    // Test 1 is pending full depth sequencing
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, true, false);
    return kase;
  }

  private static Case makeCase17() {
    final int caseNumber = 17;
    Case kase = makeCase("PRO17_0001", "Single Test", "PRO17", "REQ17", caseNumber);
    // Test 1 is pending full depth sequencing (top-up required)
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, null, "Top-up Required", true);
    return kase;
  }

  private static Case makeCase18() {
    final int caseNumber = 18;
    Case kase = makeCase("PRO18_0001", "Single Test", "PRO18", "REQ18", caseNumber);
    // Test 1 is pending full depth sequencing QC
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, null, null, null);
    return kase;
  }

  private static Case makeCase19() {
    final int caseNumber = 19;
    Case kase = makeCase("PRO19_0001", "Single Test", "PRO19", "REQ19", caseNumber);
    // Test 1 is pending full depth sequencing data review
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, true, "Good", null);
    return kase;
  }

  private static Case makeCase20() {
    final int caseNumber = 20;
    Case kase = makeCase("PRO20_0001", "Single Test", "PRO20", "REQ20", caseNumber);
    // Test 1 is pending library prep (1 failed attempt already)
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), sampleId, false, "Bad");
    return kase;
  }

  private static Case makeCase21() {
    final int caseNumber = 21;
    Case kase = makeCase("PRO21_0001", "Single Test", "PRO21", "REQ21", caseNumber);
    // Test 1 is pending library qualification (with 1 failed and 1 passed library prep)
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", true, true, false, false);
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
    ca.on.oicr.gsi.dimsum.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", false, false, false, false);
    return kase;
  }

  private static Case makeCase23() {
    final int caseNumber = 23;
    Case kase = makeCase("PRO23_001", "Single Test", "PRO23", "REQ23", caseNumber);
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
    when(requisition.isStopped()).thenReturn(caseNumber == 23);
    when(requisition.getName()).thenReturn(name);
    when(requisition.getInformaticsReviews()).thenReturn(new ArrayList<>());
    when(requisition.getDraftReports()).thenReturn(new ArrayList<>());
    when(requisition.getFinalReports()).thenReturn(new ArrayList<>());
    when(kase.getRequisition()).thenReturn(requisition);
    return requisition;
  }

  private static void addRequisitionQc(List<RequisitionQc> qcs, boolean qcPassed) {
    RequisitionQc qc = mock(RequisitionQc.class);
    when(qc.isQcPassed()).thenReturn(qcPassed);
    when(qc.getQcUser()).thenReturn("User");
    when(qc.getQcDate()).thenReturn(LocalDate.now());
    qcs.add(qc);
  }

  private static ca.on.oicr.gsi.dimsum.data.Test addTest(Case kase, int caseNumber, int testNumber,
      String name) {
    ca.on.oicr.gsi.dimsum.data.Test test = mock(ca.on.oicr.gsi.dimsum.data.Test.class);
    when(test.getName()).thenReturn(name);
    when(test.getGroupId()).thenReturn(makeTestGroupId(caseNumber, testNumber));
    when(test.getExtractions()).thenReturn(new ArrayList<>());
    when(test.getLibraryPreparations()).thenReturn(new ArrayList<>());
    when(test.getLibraryQualifications()).thenReturn(new ArrayList<>());
    when(test.getFullDepthSequencings()).thenReturn(new ArrayList<>());
    kase.getTests().add(test);
    return test;
  }

  private static ca.on.oicr.gsi.dimsum.data.Test addTest(Case kase, int caseNumber, int testNumber,
      String name,
      boolean extractionComplete, boolean libraryPrepComplete, boolean libraryQualificationComplete,
      boolean fullDepthComplete) {
    ca.on.oicr.gsi.dimsum.data.Test test = addTest(kase, caseNumber, testNumber, name);
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

  public static String makeTestGroupId(int caseNumber, int testNumber) {
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
