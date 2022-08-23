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

public class CaseLoaderTest {

  private static final String testProjectName = "PROJ";
  private static final String testDonorId = "SAM413576";
  private static final String testSampleId = "SAM413577";

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
      assertEquals(1, donorsById.size());
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
      assertEquals(1, requisitionsById.size());
      Long requisitionId = 512L;
      Requisition requisition = requisitionsById.get(requisitionId);
      assertNotNull(requisition);
      assertEquals(requisitionId, requisition.getId());
      assertEquals("REQ-1", requisition.getName());
      List<RequisitionQc> qcs = requisition.getInformaticsReviews();
      assertEquals(1, qcs.size());
      RequisitionQc qc = qcs.get(0);
      assertTrue(qc.isQcPassed());
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
  }

}
