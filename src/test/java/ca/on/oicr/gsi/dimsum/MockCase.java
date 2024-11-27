package ca.on.oicr.gsi.dimsum;

import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
import ca.on.oicr.gsi.cardea.data.DeliverableType;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;


// note: ensure that each mock case has at least one test added to it.

public class MockCase {

  public static List<Case> getCases() {
    return cases;
  }

  private static final List<Case> cases =
      Arrays.asList(makeCase0(), makeCase1(), makeCase2(), makeCase3(), makeCase4(), makeCase5(),
          makeCase6(), makeCase7(), makeCase8(), makeCase9(), makeCase10(), makeCase11(),
          makeCase12(), makeCase13(), makeCase14(), makeCase15(), makeCase16(), makeCase17(),
          makeCase18(), makeCase19(), makeCase20(), makeCase21(), makeCase22(), makeCase23(),
          makeCase24(), makeCase25(), makeCase26(), makeCase27(), makeCase28(), makeCase29(),
          makeCase30(), makeCase31(), makeCase32(), makeCase33(), makeCase34(), makeCase35());

  private static Case makeCase0() {
    final int caseNumber = 0;
    Case kase = makeCase("PRO1_0001", "WGTS assay 1", "PRO1", "REQ01", caseNumber, null, null);
    // Test 1 is pending extraction
    addTest(kase, 0, 1, "Normal WG", "WG");
    // Test 2 is pending extraction QC
    ca.on.oicr.gsi.cardea.data.Test test2 = addTest(kase, 0, 2, "Tumour WG", "WG");
    String test2ExtractionId = makeSampleId(caseNumber, 2, MetricCategory.EXTRACTION, 1);
    addSample(test2.getExtractions(), test2ExtractionId, null, null);
    // Test 3 is pending library prep and has an extra failed extraction
    ca.on.oicr.gsi.cardea.data.Test test3 =
        addTest(kase, caseNumber, 3, "Tumour WT", "WT", true, false, false, false);
    String test3ExtractionId = makeSampleId(caseNumber, 3, MetricCategory.EXTRACTION, 2);
    addSample(test3.getExtractions(), test3ExtractionId, false, "Bad");
    return kase;
  }

  private static Case makeCase1() {
    final int caseNumber = 1;
    Case kase = makeCase("PRO2_0001", "WGTS assay 2", "PRO2", "REQ02", caseNumber, null, null);
    // Test 1 is pending library QC
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Normal WG", "WG", true, false, false, false);
    String test1LibraryId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), test1LibraryId, null, null);
    // Test 2 is pending library qualification
    addTest(kase, caseNumber, 2, "Tumour WG", "WG", true, true, false, false);
    // Test 3 is pending library qualification QC
    ca.on.oicr.gsi.cardea.data.Test test3 =
        addTest(kase, caseNumber, 3, "Tumour WT", "WT", true, true, false, false);
    String test3LibQualId = makeSampleId(caseNumber, 3, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addSample(test3.getLibraryQualifications(), test3LibQualId, null, null);
    return kase;
  }

  private static Case makeCase2() {
    final int caseNumber = 2;
    Case kase =
        makeCase("PRO1_0001", "WG assay 1", "PRO1", "REQ03", caseNumber, null, null);
    // Test 1 is pending library qualification data review
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Normal WG", "WG", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, true, "Good", null);
    // Test 2 is pending full depth sequencing
    addTest(kase, caseNumber, 2, "Tumour WG", "WG", true, true, true, false);
    return kase;
  }

  private static Case makeCase3() {
    final int caseNumber = 3;
    Case kase =
        makeCase("PRO2_0002", "WG assay 2", "PRO2", "REQ02", caseNumber, null, null);
    // Test 1 is pending full depth QC
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Normal WG", "WG", true, true, true, false);
    String test1SampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), test1SampleId, null, null, null);
    // Test 2 is pending full depth data review
    ca.on.oicr.gsi.cardea.data.Test test2 =
        addTest(kase, caseNumber, 2, "Tumour WG", "WG", true, true, true, false);
    String test2SampleId = makeSampleId(caseNumber, 2, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test2.getFullDepthSequencings(), test2SampleId, true, "Good", null);
    return kase;
  }

  private static Case makeCase4() {
    final int caseNumber = 4;
    // Case is pending analysis review
    Case kase = makeCase("PRO3_0001", "Single Test", "PRO3", "REQ04", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    return kase;
  }

  private static Case makeCase5() {
    final int caseNumber = 5;
    // Case is pending release approval
    Case kase = makeCase("PRO4_0001", "Single Test", "PRO4", "REQ04", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    markAnalysisReview(kase.getDeliverables().get(0), AnalysisReviewQcStatus.PASSED);
    return kase;
  }

  private static Case makeCase6() {
    final int caseNumber = 6;
    // Case is pending release
    Case kase = makeCase("PRO5_0001", "Single Test", "PRO5", "REQ04", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    addTest(kase, caseNumber, 2, "Test", "WG", true, true, true, true);
    markAnalysisReview(kase.getDeliverables().get(0), AnalysisReviewQcStatus.PASSED);
    markReleaseApproval(kase.getDeliverables().get(0), ReleaseApprovalQcStatus.PASSED_PROCEED);
    return kase;
  }

  private static Case makeCase7() {
    final int caseNumber = 7;
    Case kase = makeCase("PRO7_0001", "Single Test", "PRO7", "REQ07", caseNumber, null, null);
    // Test 1 is pending extraction
    addTest(kase, caseNumber, 1, "Test", "WG", false, false, false, false);
    return kase;
  }

  private static Case makeCase8() {
    final int caseNumber = 8;
    Case kase = makeCase("PRO8_0001", "Single Test", "PRO8", "REQ08", caseNumber, null, null);
    // Test 1 is pending extraction QC
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", false, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.EXTRACTION, 1);
    addSample(test1.getExtractions(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase9() {
    final int caseNumber = 9;
    Case kase = makeCase("PRO9_0001", "Single Test", "PRO9", "REQ09", caseNumber, null, null);
    // Test 1 is pending library prep
    addTest(kase, caseNumber, 1, "Test", "WG", true, false, false, false);
    return kase;
  }

  private static Case makeCase10() {
    final int caseNumber = 10;
    Case kase = makeCase("PRO10_0001", "Single Test", "PRO10", "REQ10", caseNumber, null, null);
    // Test 1 is pending library QC
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase11() {
    final int caseNumber = 11;
    Case kase = makeCase("PRO11_0001", "Single Test", "PRO11", "REQ11", caseNumber, null, null);
    // Test 1 is pending library qualification
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, false, false);
    return kase;
  }

  private static Case makeCase12() {
    final int caseNumber = 12;
    Case kase = makeCase("PRO12_0001", "Single Test", "PRO12", "REQ12", caseNumber, null, null);
    // Test 1 is pending library qualification (top-up required)
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, null, "Top-up Required", true);
    return kase;
  }

  private static Case makeCase13() {
    final int caseNumber = 13;
    Case kase = makeCase("PRO13_0001", "Single Test", "PRO13", "REQ13", caseNumber, null, null);
    // Test 1 is pending library qualification QC (library aliquot)
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addSample(test1.getLibraryQualifications(), sampleId, null, null);
    return kase;
  }

  private static Case makeCase14() {
    final int caseNumber = 14;
    Case kase = makeCase("PRO14_0001", "Single Test", "PRO14", "REQ14", caseNumber, null, null);
    // Test 1 is pending library qualification QC (run-library)
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, null, null, null);
    return kase;
  }

  private static Case makeCase15() {
    final int caseNumber = 15;
    Case kase = makeCase("PRO15_0001", "Single Test", "PRO15", "REQ15", caseNumber, null, null);
    // Test 1 is pending library qualification data review
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_QUALIFICATION, 1);
    addRunLibrary(test1.getLibraryQualifications(), sampleId, true, "Good", null);
    return kase;
  }

  private static Case makeCase16() {
    final int caseNumber = 16;
    Case kase = makeCase("PRO16_0001", "Single Test", "PRO16", "REQ16", caseNumber, null, null);
    // Test 1 is pending full depth sequencing
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, false);
    return kase;
  }

  private static Case makeCase17() {
    final int caseNumber = 17;
    Case kase = makeCase("PRO17_0001", "Single Test", "PRO17", "REQ17", caseNumber, null, null);
    // Test 1 is pending full depth sequencing (top-up required)
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, null, "Top-up Required", true);
    return kase;
  }

  private static Case makeCase18() {
    final int caseNumber = 18;
    Case kase = makeCase("PRO18_0001", "Single Test", "PRO18", "REQ18", caseNumber, null, null);
    // Test 1 is pending full depth sequencing QC
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, null, null, null);
    return kase;
  }

  private static Case makeCase19() {
    final int caseNumber = 19;
    Case kase = makeCase("PRO19_0001", "Single Test", "PRO19", "REQ19", caseNumber, null, null);
    // Test 1 is pending full depth sequencing data review
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.FULL_DEPTH_SEQUENCING, 1);
    addRunLibrary(test1.getFullDepthSequencings(), sampleId, true, "Good", null);
    return kase;
  }

  private static Case makeCase20() {
    final int caseNumber = 20;
    Case kase = makeCase("PRO20_0001", "Single Test", "PRO20", "REQ20", caseNumber, null, null);
    // Test 1 is pending library prep (1 failed attempt already)
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 1);
    addSample(test1.getLibraryPreparations(), sampleId, false, "Bad");
    return kase;
  }

  private static Case makeCase21() {
    final int caseNumber = 21;
    Case kase = makeCase("PRO21_0001", "Single Test", "PRO21", "REQ21", caseNumber, null, null);
    // Test 1 is pending library qualification (with 1 failed and 1 passed library prep)
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", true, true, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.LIBRARY_PREP, 2);
    addSample(test1.getLibraryPreparations(), sampleId, false, "Bad");
    return kase;
  }

  private static Case makeCase22() {
    final int caseNumber = 22;
    Case kase = makeCase("PRO22_0001", "Single Test", "PRO22", "REQ22", caseNumber, null, null);
    // Case is pending receipt QC (replace default passed receipt)
    kase.getReceipts().remove(0);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.RECEIPT, 1);
    addSample(kase.getReceipts(), sampleId, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", false, false, false, false);
    return kase;
  }

  private static Case makeCase23() {
    // Case is stopped
    final int caseNumber = 23;
    Case kase = makeCase("PRO23_001", "Single Test", "PRO23", "REQ23", caseNumber, null, null);
    when(kase.isStopped()).thenReturn(true);
    when(kase.getRequisition().isStopped()).thenReturn(true);
    addTest(kase, caseNumber, 1, "Test", "WG", false, false, false, false);
    return kase;
  }

  private static Case makeCase24() {
    final int caseNumber = 24;
    Case kase = makeCase("PRO24_0001", "Paused Test", "PRO24", "REQ24", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", false, false, false, false);
    Requisition requisition = kase.getRequisition();
    when(requisition.isPaused()).thenReturn(true);
    return kase;
  }

  private static Case makeCase25() {
    final int caseNumber = 25;
    // Case is pending analysis review - clinical report
    Case kase = makeCase("PRO25_0001", "Single Test", "PRO25", "REQ25", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    markAnalysisReview(kase.getDeliverables().get(0), AnalysisReviewQcStatus.PASSED);
    markReleaseApproval(kase.getDeliverables().get(0), ReleaseApprovalQcStatus.PASSED_PROCEED);
    markRelease(kase.getDeliverables().get(0).getReleases().get(0), ReleaseQcStatus.PASSED_RELEASE,
        null);
    addDeliverable(kase, DeliverableType.CLINICAL_REPORT, "Clinical Report");
    return kase;
  }

  private static Case makeCase26() {
    final int caseNumber = 26;
    // Case is pending release approval - clinical report
    Case kase = makeCase("PRO26_0001", "Single Test", "PRO26", "REQ26", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    markAnalysisReview(kase.getDeliverables().get(0), AnalysisReviewQcStatus.PASSED);
    markReleaseApproval(kase.getDeliverables().get(0), ReleaseApprovalQcStatus.PASSED_PROCEED);
    markRelease(kase.getDeliverables().get(0).getReleases().get(0), ReleaseQcStatus.PASSED_RELEASE,
        null);
    CaseDeliverable deliverable =
        addDeliverable(kase, DeliverableType.CLINICAL_REPORT, "Clinical Report");
    markAnalysisReview(deliverable, AnalysisReviewQcStatus.PASSED);
    return kase;
  }

  private static Case makeCase27() {
    final int caseNumber = 27;
    // Case is pending release - clinical report
    Case kase = makeCase("PRO27_0001", "Single Test", "PRO27", "REQ27", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    markAnalysisReview(kase.getDeliverables().get(0), AnalysisReviewQcStatus.PASSED);
    markReleaseApproval(kase.getDeliverables().get(0), ReleaseApprovalQcStatus.PASSED_PROCEED);
    markRelease(kase.getDeliverables().get(0).getReleases().get(0), ReleaseQcStatus.PASSED_RELEASE,
        null);
    CaseDeliverable deliverable =
        addDeliverable(kase, DeliverableType.CLINICAL_REPORT, "Clinical Report");
    markAnalysisReview(deliverable, AnalysisReviewQcStatus.PASSED);
    markReleaseApproval(deliverable, ReleaseApprovalQcStatus.PASSED_PROCEED);
    return kase;
  }

  private static Case makeCase28() {
    final int caseNumber = 28;
    // Case is completed
    Case kase = makeCase("PRO28_0001", "Single Test", "PRO28", "REQ28", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    markAnalysisReview(kase.getDeliverables().get(0), AnalysisReviewQcStatus.PASSED);
    markReleaseApproval(kase.getDeliverables().get(0), ReleaseApprovalQcStatus.PASSED_PROCEED);
    markRelease(kase.getDeliverables().get(0).getReleases().get(0), ReleaseQcStatus.PASSED_RELEASE,
        null);
    return kase;
  }

  private static Case makeCase29() {
    final int caseNumber = 29;
    // Case is pending release approval - data release, and pending release - clinical report
    Case kase =
        makeCase("PRO29_0001", "Single Test", "PRO29", "REQ29", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    markAnalysisReview(kase.getDeliverables().get(0), AnalysisReviewQcStatus.PASSED);
    CaseDeliverable deliverable =
        addDeliverable(kase, DeliverableType.CLINICAL_REPORT, "Clinical Report");
    markAnalysisReview(deliverable, AnalysisReviewQcStatus.PASSED);
    markReleaseApproval(deliverable, ReleaseApprovalQcStatus.PASSED_PROCEED);
    return kase;
  }

  private static Case makeCase30() {
    final int caseNumber = 30;
    // Case is pending release approval - data release, and pending analysis review - clinical
    // report
    Case kase = makeCase("PRO30_0001", "Single Test", "PRO30", "REQ30", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    markAnalysisReview(kase.getDeliverables().get(0), AnalysisReviewQcStatus.PASSED);
    addDeliverable(kase, DeliverableType.CLINICAL_REPORT, "Clinical Report");
    return kase;
  }

  private static Case makeCase31() {
    final int caseNumber = 31;
    // Case is completed release - clinical report, but pending analysis review - data release
    Case kase = makeCase("PRO31_0001", "Single Test", "PRO31", "REQ31", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "Test", "WG", true, true, true, true);
    CaseDeliverable deliverable =
        addDeliverable(kase, DeliverableType.CLINICAL_REPORT, "Clinical Report");
    markAnalysisReview(deliverable, AnalysisReviewQcStatus.PASSED);
    markReleaseApproval(deliverable, ReleaseApprovalQcStatus.PASSED_PROCEED);
    markRelease(deliverable.getReleases().get(0), ReleaseQcStatus.PASSED_RELEASE, null);
    return kase;
  }

  private static Case makeCase32() {
    // Analysis review skipped, but case is not there yet anyway
    final int caseNumber = 32;
    Case kase = makeCase("PRO32_0001", "Single Test", "PRO32", "REQ32", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "test", "WG", false, false, false, false);
    addDeliverable(kase, DeliverableType.DATA_RELEASE, "FastQ");
    Project project = kase.getProjects().iterator().next();
    when(project.isAnalysisReviewSkipped()).thenReturn(true);
    return kase;
  }

  private static Case makeCase33() {
    // Full-depth completed, analysis review skipped
    final int caseNumber = 33;
    Case kase = makeCase("PRO33_0001", "Single Test", "PRO33", "REQ33", caseNumber, null, null);
    addTest(kase, caseNumber, 1, "test", "WG", true, true, true, true);
    addDeliverable(kase, DeliverableType.DATA_RELEASE, "FastQ");
    Project project = kase.getProjects().iterator().next();
    when(project.isAnalysisReviewSkipped()).thenReturn(true);
    return kase;
  }

  private static Case makeCase34() {
    final int caseNumber = 34;
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate completionDate = LocalDate.of(2024, 1, 15);
    Case kase =
        makeCase("PRO34_0001", "Single Test", "PRO34", "REQ34", caseNumber, startDate,
            completionDate);

    // Mocking the completion logic based on deliverable states
    List<CaseDeliverable> deliverables = new ArrayList<>();
    CaseDeliverable deliverable = mock(CaseDeliverable.class);
    when(deliverable.getReleaseApprovalQcStatus())
        .thenReturn(ReleaseApprovalQcStatus.PASSED_PROCEED);
    when(deliverable.getAnalysisReviewQcStatus()).thenReturn(AnalysisReviewQcStatus.PASSED);
    CaseRelease release = mock(CaseRelease.class);
    when(release.getQcStatus()).thenReturn(ReleaseQcStatus.PASSED_RELEASE);
    when(release.getQcDate()).thenReturn(completionDate);
    when(deliverable.getReleases()).thenReturn(Arrays.asList(release));
    deliverables.add(deliverable);

    when(kase.getDeliverables()).thenReturn(deliverables);

    addCompletionDate(kase, completionDate);

    return kase;
  }

  private static Case makeCase35() {
    final int caseNumber = 35;
    Case kase = makeCase("PRO35_0001", "Single Test", "PRO35", "REQ35", caseNumber, null, null);
    // Test 1 is pending extraction transfer
    ca.on.oicr.gsi.cardea.data.Test test1 =
        addTest(kase, caseNumber, 1, "Test", "WG", false, false, false, false);
    String sampleId = makeSampleId(caseNumber, 1, MetricCategory.EXTRACTION, 1);
    addSample(test1.getExtractions(), sampleId, true, "Good");
    return kase;
  }

  private static Case makeCase(String donorName, String assayName, String projectName,
      String requisitionName, int caseNumber, LocalDate startDate,
      LocalDate completionDate) {
    if (startDate == null) {
      startDate = LocalDate.now();
    }
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
    String receiptSampleId = makeSampleId(caseNumber, 0, MetricCategory.RECEIPT, 1);
    addSample(kase.getReceipts(), receiptSampleId, true, "Good");
    when(kase.getTests()).thenReturn(new ArrayList<>());
    addRequisition(kase, caseNumber, requisitionName);
    when(kase.getDeliverables()).thenReturn(new ArrayList<>());
    addDeliverable(kase, DeliverableType.DATA_RELEASE, "Full Pipeline");
    when(kase.getStartDate()).thenReturn(startDate);
    addCompletionDate(kase, completionDate);
    return kase;
  }

  private static LocalDate addCompletionDate(Case kase, LocalDate completionDate) {
    if (kase.isStopped()) {
      return null;
    }
    List<CaseDeliverable> deliverables = kase.getDeliverables();
    if (deliverables.isEmpty()) {
      return null;
    }
    List<CaseRelease> releases = deliverables.stream()
        .flatMap(deliverable -> deliverable.getReleases().stream())
        .collect(Collectors.toList());
    if (releases.isEmpty()) {
      return null;
    }
    if (releases.stream()
        .anyMatch(release -> release.getQcStatus() == null || release.getQcStatus().isPending())) {
      return null;
    }
    return releases.stream()
        .map(CaseRelease::getQcDate)
        .max(LocalDate::compareTo)
        .orElse(null);
  }

  private static CaseDeliverable addDeliverable(Case kase, DeliverableType deliverableType,
      String deliverableName) {
    CaseDeliverable deliverable = kase.getDeliverables().stream()
        .filter(x -> x.getDeliverableType() == deliverableType).findFirst().orElse(null);
    if (deliverable == null) {
      deliverable = mock(CaseDeliverable.class);
      when(deliverable.getDeliverableType()).thenReturn(deliverableType);
      when(deliverable.getReleases()).thenReturn(new ArrayList<>());
      kase.getDeliverables().add(deliverable);
    }
    CaseRelease release = mock(CaseRelease.class);
    when(release.getDeliverable()).thenReturn(deliverableName);
    deliverable.getReleases().add(release);
    return deliverable;
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
    when(requisition.isPaused()).thenReturn(caseNumber == 24);
    when(requisition.getName()).thenReturn(name);
    when(kase.getRequisition()).thenReturn(requisition);
    return requisition;
  }

  private static void markAnalysisReview(CaseDeliverable deliverable,
      AnalysisReviewQcStatus qcStatus) {
    when(deliverable.getAnalysisReviewQcStatus()).thenReturn(qcStatus);
    when(deliverable.getAnalysisReviewQcUser()).thenReturn("User");
    when(deliverable.getAnalysisReviewQcDate()).thenReturn(LocalDate.now());
  }

  private static void markReleaseApproval(CaseDeliverable deliverable,
      ReleaseApprovalQcStatus qcStatus) {
    when(deliverable.getReleaseApprovalQcStatus()).thenReturn(qcStatus);
    when(deliverable.getReleaseApprovalQcUser()).thenReturn("User");
    when(deliverable.getReleaseApprovalQcDate()).thenReturn(LocalDate.now());
  }

  private static void markRelease(CaseRelease release, ReleaseQcStatus qcStatus, LocalDate qcDate) {
    when(release.getQcStatus()).thenReturn(qcStatus);
    when(release.getQcUser()).thenReturn("User");
    if (qcDate != null) {
      when(release.getQcDate()).thenReturn(qcDate);
    } else {
      when(release.getQcDate()).thenReturn(LocalDate.now());
    }
  }

  private static ca.on.oicr.gsi.cardea.data.Test addTest(Case kase, int caseNumber, int testNumber,
      String name, String libraryDesignCode) {
    ca.on.oicr.gsi.cardea.data.Test test = mock(ca.on.oicr.gsi.cardea.data.Test.class);
    when(test.getName()).thenReturn(name);
    when(test.getLibraryDesignCode()).thenReturn(libraryDesignCode);
    when(test.getGroupId()).thenReturn(makeTestGroupId(caseNumber, testNumber));
    when(test.getExtractions()).thenReturn(new ArrayList<>());
    when(test.getLibraryPreparations()).thenReturn(new ArrayList<>());
    when(test.getLibraryQualifications()).thenReturn(new ArrayList<>());
    when(test.getFullDepthSequencings()).thenReturn(new ArrayList<>());
    kase.getTests().add(test);
    return test;
  }

  private static ca.on.oicr.gsi.cardea.data.Test addTest(Case kase, int caseNumber, int testNumber,
      String name, String libraryDesignCode,
      boolean extractionComplete, boolean libraryPrepComplete, boolean libraryQualificationComplete,
      boolean fullDepthComplete) {
    ca.on.oicr.gsi.cardea.data.Test test =
        addTest(kase, caseNumber, testNumber, name, libraryDesignCode);
    if (extractionComplete) {
      String extractionId = makeSampleId(caseNumber, testNumber, MetricCategory.EXTRACTION, 1);
      Sample extraction = addSample(test.getExtractions(), extractionId, true, "Good");
      when(extraction.getTransferDate()).thenReturn(LocalDate.now());
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
    return addRunLibrary(gateItems, id, qcPassed, qcReason, dataReviewPassed, qcPassed,
        dataReviewPassed);
  }

  private static Sample addRunLibrary(List<Sample> gateItems, String id, Boolean qcPassed,
      String qcReason, Boolean dataReviewPassed, Boolean runQcPassed, Boolean runDataReviewPassed) {
    Sample sample = addSample(gateItems, id, qcPassed, qcReason);
    Run run = mock(Run.class);
    when(sample.getRun()).thenReturn(run);
    when(sample.getDataReviewPassed()).thenReturn(dataReviewPassed);
    if (dataReviewPassed != null) {
      when(sample.getDataReviewUser()).thenReturn("User");
      when(sample.getDataReviewDate()).thenReturn(LocalDate.now());
    }
    if (runQcPassed != null) {
      when(run.getQcPassed()).thenReturn(runQcPassed);
      when(run.getQcUser()).thenReturn("User");
      when(run.getQcDate()).thenReturn(LocalDate.now());
    }
    if (runDataReviewPassed != null) {
      when(run.getDataReviewPassed()).thenReturn(runDataReviewPassed);
      when(run.getDataReviewUser()).thenReturn("User");
      when(run.getDataReviewDate()).thenReturn(LocalDate.now());
    }
    return sample;
  }

}
