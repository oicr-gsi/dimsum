package ca.on.oicr.gsi.dimsum;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.on.oicr.gsi.dimsum.data.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  private List<Case> cases = MockCase.getCases();

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

    Map<String, ProjectSummary> projectSummariesByName = data.getProjectSummariesByName();
    assertNotNull(projectSummariesByName);
    assertEquals(1, projectSummariesByName.size());
    ProjectSummary projectSummary = projectSummariesByName.get("PROJ");
    assertEquals(3, projectSummary.getTotalTestCount());
    assertEquals(3, projectSummary.getReceiptCompletedCount());
    assertEquals(3, projectSummary.getLibraryPrepCompletedCount());
    assertEquals(3, projectSummary.getLibraryQualCompletedCount());
    assertEquals(3, projectSummary.getFullDepthSeqCompletedCount());
    assertEquals(3, projectSummary.getInformaticsCompletedCount());
    assertEquals(3, projectSummary.getDraftReportPendingCount());
  }

  @Test
  public void testCalculateProjectSummary() throws Exception {
    Map<String, ProjectSummary> projectSummariesByName =
        CaseLoader.calculateProjectSummaries(cases);
    assertNotNull(projectSummariesByName);

    // PRO1
    assertEquals(5, projectSummariesByName.get("PRO1").getTotalTestCount());
    assertEquals(3, projectSummariesByName.get("PRO1").getExtractionCompletedCount());
    assertEquals(1, projectSummariesByName.get("PRO1").getExtractionPendingQcCount());
    assertEquals(1, projectSummariesByName.get("PRO1").getExtractionPendingCount());
    assertEquals(1, projectSummariesByName.get("PRO1").getLibraryPrepPendingCount());
    assertEquals(1, projectSummariesByName.get("PRO1").getLibraryQualPendingQcCount());
    assertEquals(1, projectSummariesByName.get("PRO1").getLibraryQualCompletedCount());
    assertEquals(1, projectSummariesByName.get("PRO1").getFullDepthSeqPendingCount());

    // PRO2
    assertEquals(1, projectSummariesByName.get("PRO2").getLibraryPrepPendingQcCount());
    assertEquals(1, projectSummariesByName.get("PRO2").getLibraryQualPendingCount());
    assertEquals(1, projectSummariesByName.get("PRO2").getLibraryQualPendingQcCount());
    assertEquals(2, projectSummariesByName.get("PRO2").getFullDepthSeqPendingQcCount());

    // PRO3
    assertEquals(1, projectSummariesByName.get("PRO3").getInformaticsPendingCount());

    // PRO4
    assertEquals(1, projectSummariesByName.get("PRO4").getDraftReportPendingCount());
    assertEquals(1, projectSummariesByName.get("PRO4").getInformaticsCompletedCount());

    // PRO5
    assertEquals(2, projectSummariesByName.get("PRO5").getFinalReportPendingCount());
    assertEquals(2, projectSummariesByName.get("PRO5").getDraftReportCompletedCount());
    assertEquals(2, projectSummariesByName.get("PRO5").getInformaticsCompletedCount());

    // PRO7
    assertEquals(1, projectSummariesByName.get("PRO7").getExtractionPendingCount());

    // PRO8
    assertEquals(1, projectSummariesByName.get("PRO8").getExtractionPendingQcCount());

    // PRO9
    assertEquals(1, projectSummariesByName.get("PRO9").getLibraryPrepPendingCount());

    // PRO10
    assertEquals(1, projectSummariesByName.get("PRO10").getLibraryPrepPendingQcCount());

    // PRO11
    assertEquals(1, projectSummariesByName.get("PRO11").getLibraryQualPendingCount());

    // PRO12
    assertEquals(1, projectSummariesByName.get("PRO12").getLibraryQualPendingCount());

    // PRO13
    assertEquals(1, projectSummariesByName.get("PRO13").getLibraryQualPendingQcCount());

    // PRO14
    assertEquals(1, projectSummariesByName.get("PRO14").getLibraryQualPendingQcCount());

    // PRO15
    assertEquals(1, projectSummariesByName.get("PRO15").getLibraryQualPendingQcCount());

    // PRO16
    assertEquals(1, projectSummariesByName.get("PRO16").getFullDepthSeqPendingCount());

    // PRO17
    assertEquals(1, projectSummariesByName.get("PRO17").getFullDepthSeqPendingCount());

    // PRO18
    assertEquals(1, projectSummariesByName.get("PRO18").getFullDepthSeqPendingQcCount());

    // PRO19
    assertEquals(1, projectSummariesByName.get("PRO19").getFullDepthSeqPendingQcCount());

    // PRO20
    assertEquals(1, projectSummariesByName.get("PRO20").getLibraryPrepPendingCount());

    // PRO21
    assertEquals(1, projectSummariesByName.get("PRO21").getLibraryPrepCompletedCount());

    // PRO22
    assertEquals(1, projectSummariesByName.get("PRO22").getTotalTestCount());
    assertEquals(1, projectSummariesByName.get("PRO22").getReceiptPendingQcCount());

  }

}
