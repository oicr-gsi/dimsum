package ca.on.oicr.gsi.dimsum;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.on.oicr.gsi.dimsum.data.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.net.URL;
import java.time.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CaseLoaderTest {

  private static final String testProjectName = "PROJ";
  private static final String testDonorId = "SAM413576";
  private static final String testSampleId = "SAM413577";
  private static List<Case> cases =
      Arrays.asList(makeCase0(), makeCase1(), makeCase2(), makeCase3(), makeCase4(), makeCase5(),
          makeCase6(), makeCase7(), makeCase8(), makeCase9(), makeCase10(), makeCase11(),
          makeCase12(), makeCase13(), makeCase14(), makeCase15(), makeCase16(), makeCase17(),
          makeCase18(), makeCase19(), makeCase20(), makeCase21(), makeCase22());

  private static File dataDirectory;

  private CaseLoader sut;

  @BeforeAll
  public static void setupClass() {
    ClassLoader classLoader = CaseLoaderTest.class.getClassLoader();
    URL caseFileUrl = classLoader.getResource("testdata/cases.json");
    File caseFile = new File(caseFileUrl.getFile());
    dataDirectory = caseFile.getParentFile();
  }

  @BeforeEach
  public void setup() {
    sut = new CaseLoader(dataDirectory, null);
  }

  @Test
  public void testLoadProjects() throws Exception {
    try (FileReader reader = sut.getProjectReader()) {
      Map<String, Project> projectsByName = sut.loadProjects(reader);
      assertEquals(1, projectsByName.size());
      assertProject(projectsByName.get(testProjectName));
    }
  }

  private void assertProject(Project project) {
    assertNotNull(project);
    assertEquals(testProjectName, project.getName());
    assertEquals("Research", project.getPipeline());
  }

  @Test
  public void testLoadSamples() throws Exception {
    try (FileReader reader = sut.getSampleReader()) {
      Donor donor = mock(Donor.class);
      when(donor.getId()).thenReturn("SAM413576");
      Map<String, Donor> donorsById = Map.of(donor.getId(), donor);

      Map<Long, Run> runsById = new HashMap<>();
      runsById.put(5459L, mock(Run.class));
      runsById.put(5460L, mock(Run.class));
      runsById.put(5467L, mock(Run.class));
      runsById.put(5476L, mock(Run.class));
      runsById.put(5481L, mock(Run.class));
      runsById.put(5540L, mock(Run.class));

      Requisition requisition = mock(Requisition.class);
      when(requisition.getName()).thenReturn("REQ-1");
      Map<Long, Requisition> requisitionsById = Collections.singletonMap(512L, requisition);

      Map<String, Sample> samplesById =
          sut.loadSamples(reader, donorsById, runsById, requisitionsById);
      assertEquals(20, samplesById.size());
      assertSample(samplesById.get(testSampleId));
    }
  }

  private void assertSample(Sample sample) {
    assertNotNull(sample);
    assertEquals(testSampleId, sample.getId());
    assertEquals("PROJ_1289_Ly_R_nn_1-1", sample.getName());
    assertEquals(Boolean.TRUE, sample.getQcPassed());
    assertEquals(LocalDate.of(2021, 7, 19), sample.getQcDate());
  }

  @Test
  public void testLoadDonors() throws Exception {
    try (FileReader reader = sut.getDonorReader()) {
      Map<String, Donor> donorsById = sut.loadDonors(reader);
      assertEquals(2, donorsById.size());
      Donor donor = donorsById.get(testDonorId);
      assertDonor(donor);
    }
  }

  private void assertDonor(Donor donor) {
    assertNotNull(donor);
    assertEquals(testDonorId, donor.getId());
    assertEquals("PROJ_1289", donor.getName());
  }

  @Test
  public void testLoadRequisitions() throws Exception {
    try (FileReader reader = sut.getRequisitionReader()) {
      Map<Long, Requisition> requisitionsById = sut.loadRequisitions(reader);
      assertEquals(2, requisitionsById.size());
      Long requisitionId = 512L;
      Requisition requisition = requisitionsById.get(requisitionId);
      assertNotNull(requisition);
      assertEquals(requisitionId, requisition.getId());
      assertEquals("REQ-1", requisition.getName());
      List<RequisitionQc> qcs = requisition.getInformaticsReviews();
      assertEquals(1, qcs.size());
      RequisitionQc qc = qcs.get(0);
      assertTrue(qc.isQcPassed());
      assertNotNull(requisition.getQcGroups());
      assertEquals(3, requisition.getQcGroups().size());
      RequisitionQcGroup qcGroup = requisition.getQcGroups().stream()
          .filter(x -> "M".equals(x.getTissueType()) && "WG".equals(x.getLibraryDesignCode()))
          .findAny().orElse(null);
      assertNotNull(qcGroup);
      assertEquals(new BigDecimal("87.6189"), qcGroup.getCallability());
    }
  }

  @Test
  public void testLoadAssays() throws Exception {
    try (FileReader reader = sut.getAssayReader()) {
      Map<Long, Assay> assaysById = sut.loadAssays(reader);
      assertEquals(1, assaysById.size());
      Long assayId = 2L;
      Assay assay = assaysById.get(assayId);
      assertNotNull(assay);
      assertEquals(assayId, assay.getId());
      assertEquals("WGTS - 40XT/30XN", assay.getName());
      assertEquals(6, assay.getMetricCategories().size());
      assertNotNull(assay.getMetricCategories());
      List<MetricSubcategory> subcategories =
          assay.getMetricCategories().get(MetricCategory.LIBRARY_PREP);
      assertNotNull(subcategories);
      assertEquals(2, subcategories.size());
      MetricSubcategory subcategory = subcategories.stream()
          .filter(x -> "WT Library QC (Qubit, TS, FA)".equals(x.getName()))
          .findAny()
          .orElse(null);
      assertNotNull(subcategory);
      assertEquals("WT", subcategory.getLibraryDesignCode());
      Metric metric = subcategory.getMetrics().stream()
          .filter(x -> "Concentration (Qubit)".equals(x.getName()))
          .findAny()
          .orElse(null);
      assertNotNull(metric);
      assertEquals(new BigDecimal("0.7"), metric.getMinimum());
    }
  }

  @Test
  public void testLoadOmittedSamples() throws Exception {
    Donor donor = mock(Donor.class);
    when(donor.getId()).thenReturn("SAM123456");
    Map<String, Donor> donorsById = Map.of(donor.getId(), donor);

    Requisition requisition = mock(Requisition.class);
    when(requisition.getId()).thenReturn(999L);
    when(requisition.getName()).thenReturn("REQ-X");
    Map<Long, Requisition> requisitionsById = Collections.singletonMap(999L, requisition);

    try (FileReader reader = sut.getNoCaseReader()) {
      List<OmittedSample> samples = sut.loadOmittedSamples(reader, donorsById, requisitionsById);
      assertEquals(2, samples.size());
      OmittedSample sample = samples.stream()
          .filter(x -> Objects.equals("SAM123457", x.getId()))
          .findFirst().orElse(null);
      assertOmittedSample(sample);
    }
  }

  private void assertOmittedSample(OmittedSample sample) {
    assertNotNull(sample);
    assertEquals("SAM123457", sample.getId());
    assertEquals("NOCASE_0001_01", sample.getName());
    assertEquals("NOCASE", sample.getProject());
    assertNotNull(sample.getDonor());
    assertEquals("SAM123456", sample.getDonor().getId());
    assertEquals(999L, sample.getRequisitionId());
    assertEquals("REQ-X", sample.getRequisitionName());
    assertEquals(LocalDate.of(2022, 12, 13), sample.getCreatedDate());
  }

  @Test
  public void testLoad() throws Exception {
    CaseData data = sut.load(null);
    assertNotNull(data);

    ZonedDateTime timestamp = data.getTimestamp();
    assertNotNull(timestamp);
    assertEquals(ZonedDateTime.of(2022, 5, 26, 14, 33, 28, 0, ZoneOffset.UTC), timestamp);

    List<Case> cases = data.getCases();
    assertNotNull(cases);
    assertEquals(1, cases.size());
    Case kase = cases.get(0);
    assertDonor(kase.getDonor());

    assertNotNull(kase.getProjects());
    assertEquals(1, kase.getProjects().size());
    Project project = kase.getProjects().iterator().next();
    assertProject(project);

    assertEquals("WGTS - 40XT/30XN", kase.getAssay().getName());

    assertNotNull(kase.getReceipts());
    assertEquals(5, kase.getReceipts().size());
    Sample sample = kase.getReceipts().stream().filter(x -> x.getId().equals(testSampleId))
        .findAny().orElse(null);
    assertSample(sample);

    assertNotNull(kase.getTests());
    assertEquals(3, kase.getTests().size());
    ca.on.oicr.gsi.dimsum.data.Test test = kase.getTests().stream()
        .filter(x -> x.getName().equals("Normal WG")).findAny().orElse(null);
    assertNotNull(test);
    assertEquals("Normal WG", test.getName());
    assertNotNull(test.getExtractions());
    assertEquals(1, test.getExtractions().size());
    assertNotNull(test.getLibraryPreparations());
    assertEquals(1, test.getLibraryPreparations().size());
    assertNotNull(test.getLibraryQualifications());
    assertEquals(2, test.getLibraryQualifications().size());
    assertNotNull(test.getFullDepthSequencings());
    assertEquals(1, test.getFullDepthSequencings().size());
    Sample fullDepth = test.getFullDepthSequencings().get(0);
    assertEquals("5476_1_LDI73620", fullDepth.getId());
    assertEquals("PROJ_1289_Ly_R_PE_567_WG", fullDepth.getName());
    assertEquals(Boolean.TRUE, fullDepth.getQcPassed());

    Map<String, ProjectSummary> projectsBySummary = data.getProjectsBySummary();
    assertNotNull(projectsBySummary);
    assertEquals(1, projectsBySummary.size());
    ProjectSummary projectSummary = projectsBySummary.get("PROJ");
    assertEquals(3, projectSummary.getCounts().get("TOTAL_TESTS"));
    assertEquals(3, projectSummary.getCounts().get("RECEIPT_COMPLETE"));
    assertEquals(3, projectSummary.getCounts().get("LIBRARY_PREP_COMPLETE"));
    assertEquals(3, projectSummary.getCounts().get("LIBRARY_PREP_COMPLETE"));
    assertEquals(3, projectSummary.getCounts().get("LIBRARY_PREP_COMPLETE"));
    assertEquals(3, projectSummary.getCounts().get("LIBRARY_QUAL_COMPLETE"));
    assertEquals(3, projectSummary.getCounts().get("FULL_DEPTH_COMPLETE"));
    assertEquals(3, projectSummary.getCounts().get("INFORMATICS_COMPLETE"));
    assertEquals(3, projectSummary.getCounts().get("DRAFT_REPORT_PEND_WORK"));
  }

  @Test
  public void testCalculateProjectSummary() throws Exception {
    Map<String, ProjectSummary> projectsBySummary = CaseLoader.calculateProjectSummaries(cases);
    assertNotNull(projectsBySummary);

    // PRO1
    assertEquals(5, projectsBySummary.get("PRO1").getCounts().get("TOTAL_TESTS"));
    assertEquals(3, projectsBySummary.get("PRO1").getCounts().get("EXTRACTION_COMPLETE"));
    assertEquals(1, projectsBySummary.get("PRO1").getCounts().get("EXTRACTION_PEND_QC"));
    assertEquals(1, projectsBySummary.get("PRO1").getCounts().get("EXTRACTION_PEND_WORK"));
    assertEquals(1, projectsBySummary.get("PRO1").getCounts().get("LIBRARY_PREP_PEND_WORK"));
    assertEquals(1, projectsBySummary.get("PRO1").getCounts().get("LIBRARY_QUAL_PEND_QC"));
    assertEquals(1, projectsBySummary.get("PRO1").getCounts().get("LIBRARY_QUAL_COMPLETE"));
    assertEquals(1, projectsBySummary.get("PRO1").getCounts().get("FULL_DEPTH_PEND_WORK"));

    // PRO2
    assertEquals(1, projectsBySummary.get("PRO2").getCounts().get("LIBRARY_PREP_PEND_QC"));
    assertEquals(1, projectsBySummary.get("PRO2").getCounts().get("LIBRARY_QUAL_PEND_WORK"));
    assertEquals(1, projectsBySummary.get("PRO2").getCounts().get("LIBRARY_QUAL_PEND_QC"));
    assertEquals(2, projectsBySummary.get("PRO2").getCounts().get("FULL_DEPTH_PEND_QC"));

    // PRO3
    assertEquals(1, projectsBySummary.get("PRO3").getCounts().get("INFORMATICS_PEND_WORK"));

    // PRO4
    assertEquals(1, projectsBySummary.get("PRO4").getCounts().get("DRAFT_REPORT_PEND_WORK"));
    assertEquals(1, projectsBySummary.get("PRO4").getCounts().get("INFORMATICS_COMPLETE"));

    // PRO5
    assertEquals(2, projectsBySummary.get("PRO5").getCounts().get("FINAL_REPORT_PEND_WORK"));
    assertEquals(2, projectsBySummary.get("PRO5").getCounts().get("DRAFT_REPORT_COMPLETE"));
    assertEquals(2, projectsBySummary.get("PRO5").getCounts().get("INFORMATICS_COMPLETE"));

    // PRO7
    assertEquals(1, projectsBySummary.get("PRO7").getCounts().get("EXTRACTION_PEND_WORK"));

    // PRO8
    assertEquals(1, projectsBySummary.get("PRO8").getCounts().get("EXTRACTION_PEND_QC"));

    // PRO9
    assertEquals(1, projectsBySummary.get("PRO9").getCounts().get("LIBRARY_PREP_PEND_WORK"));

    // PRO10
    assertEquals(1, projectsBySummary.get("PRO10").getCounts().get("LIBRARY_PREP_PEND_QC"));

    // PRO11
    assertEquals(1, projectsBySummary.get("PRO11").getCounts().get("LIBRARY_QUAL_PEND_WORK"));

    // PRO12
    assertEquals(1, projectsBySummary.get("PRO12").getCounts().get("LIBRARY_QUAL_PEND_WORK"));

    // PRO13
    assertEquals(1, projectsBySummary.get("PRO13").getCounts().get("LIBRARY_QUAL_PEND_QC"));

    // PRO14
    assertEquals(1, projectsBySummary.get("PRO14").getCounts().get("LIBRARY_QUAL_PEND_QC"));

    // PRO15
    assertEquals(1, projectsBySummary.get("PRO15").getCounts().get("LIBRARY_QUAL_PEND_QC"));

    // PRO16
    assertEquals(1, projectsBySummary.get("PRO16").getCounts().get("FULL_DEPTH_PEND_WORK"));

    // PRO17
    assertEquals(1, projectsBySummary.get("PRO17").getCounts().get("FULL_DEPTH_PEND_WORK"));

    // PRO18
    assertEquals(1, projectsBySummary.get("PRO18").getCounts().get("FULL_DEPTH_PEND_QC"));

    // PRO19
    assertEquals(1, projectsBySummary.get("PRO19").getCounts().get("FULL_DEPTH_PEND_QC"));

    // PRO20
    assertEquals(1, projectsBySummary.get("PRO20").getCounts().get("LIBRARY_PREP_PEND_WORK"));

    // PRO21
    assertEquals(1, projectsBySummary.get("PRO21").getCounts().get("LIBRARY_PREP_COMPLETE"));

    // PRO22
    assertEquals(1, projectsBySummary.get("PRO22").getCounts().get("TOTAL_TESTS"));
    assertEquals(1, projectsBySummary.get("PRO22").getCounts().get("RECEIPT_PEND_QC"));

  }


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
