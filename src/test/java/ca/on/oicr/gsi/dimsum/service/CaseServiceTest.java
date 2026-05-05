package ca.on.oicr.gsi.dimsum.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.SampleImpl;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.data.CacheUpdatedCase;
import ca.on.oicr.gsi.dimsum.data.CacheUpdatedRelease;
import ca.on.oicr.gsi.dimsum.data.CaseData;
import ca.on.oicr.gsi.dimsum.data.NabuBulkSignoff;
import ca.on.oicr.gsi.dimsum.data.NabuSignoff.NabuSignoffStep;
import ca.on.oicr.gsi.dimsum.security.DimsumPrincipal;
import ca.on.oicr.gsi.dimsum.security.SecurityManager;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

public class CaseServiceTest {

  private static final String DELIVERABLE_CATEGORY_RESEARCH = "Research";
  private static final String DELIVERABLE_FASTQ = "FastQ";
  private static final String DELIVERABLE_REPORT = "Report";

  private CaseService sut;
  private CaseData caseData;
  private SecurityManager securityManager;

  @BeforeEach
  public void setup() {
    sut = new CaseService(null);
    File datadir = new File(System.getProperty("basedir"), "/target/test-data");
    if (!datadir.exists()) {
      datadir.mkdir();
    }
    sut.setDataDirectory(datadir.getAbsolutePath());
    securityManager = mock(SecurityManager.class);
    DimsumPrincipal principal = makeInternalPrincipal();
    when(securityManager.getPrincipal()).thenReturn(principal);
    sut.setSecurityManager(securityManager);
    sut.setObjectMapper(new ObjectMapper());
    caseData = mock(CaseData.class);
    when(caseData.getCases()).thenReturn(new ArrayList<>());
    Case case1 = addCase(caseData, 1, 1);
    addDeliverables(case1, DELIVERABLE_CATEGORY_RESEARCH, DELIVERABLE_FASTQ, DELIVERABLE_REPORT);
    Case case2 = addCase(caseData, 2, 2);
    addDeliverables(case2, DELIVERABLE_CATEGORY_RESEARCH, DELIVERABLE_FASTQ, DELIVERABLE_REPORT);
    when(caseData.getTimestamp()).thenReturn(ZonedDateTime.now());
    sut.setCaseData(caseData);
  }

  @org.junit.jupiter.api.Test
  public void testGetCases() {
    TableData<Case> data = sut.getCases(10, 1, CaseSort.LAST_ACTIVITY, true, null, null);
    assertNotNull(data);
    assertEquals(2, data.getTotalCount());
    assertEquals(2, data.getFilteredCount());
    assertNotNull(data.getItems());
    assertEquals(2, data.getItems().size());
  }

  @org.junit.jupiter.api.Test
  public void testGetReceipts() {
    TableData<Sample> data = sut.getReceipts(10, 1, SampleSort.NAME, true, null, null);
    assertContainsSamples(data, "1N-A1", "1N-A2", "2N-A1", "2N-A2");
  }

  @org.junit.jupiter.api.Test
  public void testGetReceiptsDistinct() {
    // Add duplicates and ensure they get removed from results
    addCase(caseData, 1, 1);
    TableData<Sample> data = sut.getReceipts(10, 1, SampleSort.NAME, true, null, null);
    assertContainsSamples(data, "1N-A1", "1N-A2", "2N-A1", "2N-A2");
  }

  @org.junit.jupiter.api.Test
  public void testGetExtractions() {
    TableData<Sample> data = sut.getExtractions(20, 1, SampleSort.NAME, true, null, null);
    assertContainsSamples(data, "1A-B1", "1A-B2", "1B-B1", "1B-B2", "2A-B1", "2A-B2", "2B-B1",
        "2B-B2");
  }

  @org.junit.jupiter.api.Test
  public void testGetExtractionsDistinct() {
    // Add duplicates and ensure they get removed from results
    addCase(caseData, 1, 1);
    TableData<Sample> data = sut.getExtractions(20, 1, SampleSort.NAME, true, null, null);
    assertContainsSamples(data, "1A-B1", "1A-B2", "1B-B1", "1B-B2", "2A-B1", "2A-B2", "2B-B1",
        "2B-B2");
  }

  @org.junit.jupiter.api.Test
  public void testGetLibraryPreparations() {
    TableData<Sample> data = sut.getLibraryPreparations(20, 1, SampleSort.NAME, true, null, null);
    assertContainsSamples(data, "1A-C1", "1A-C2", "1B-C1", "1B-C2", "2A-C1", "2A-C2", "2B-C1",
        "2B-C2");
  }

  @org.junit.jupiter.api.Test
  public void testGetLibraryQualifications() {
    TableData<Sample> data = sut.getLibraryQualifications(20, 1, SampleSort.NAME, true, null, null);
    assertContainsSamples(data, "1A-D1", "1A-D2", "1B-D1", "1B-D2", "2A-D1", "2A-D2", "2B-D1",
        "2B-D2");
  }

  @org.junit.jupiter.api.Test
  public void testGetFullDepthSequencings() {
    TableData<Sample> data = sut.getFullDepthSequencings(20, 1, SampleSort.NAME, true, null, null);
    assertContainsSamples(data, "1A-E1", "1A-E2", "1B-E1", "1B-E2", "2A-E1", "2A-E2", "2B-E1",
        "2B-E2");
  }

  @org.junit.jupiter.api.Test
  public void testCacheReleaseAssignments() throws Exception {
    final String caseId = "R1_C1";
    final String user = "Test User";
    NabuBulkSignoff signoff = mock(NabuBulkSignoff.class);
    when(signoff.getCaseIdentifiers()).thenReturn(Collections.singletonList(caseId));
    when(signoff.getSignoffStepName()).thenReturn(NabuSignoffStep.RELEASE);
    when(signoff.getDeliverableType()).thenReturn(DELIVERABLE_CATEGORY_RESEARCH);
    when(signoff.getDeliverable()).thenReturn(DELIVERABLE_REPORT);
    when(signoff.getQcPassed()).thenReturn(null);
    when(signoff.getRelease()).thenReturn(null);
    when(signoff.getUsername()).thenReturn(user);
    List<NabuBulkSignoff> signoffs = Collections.singletonList(signoff);
    sut.cacheReleaseAssignments(signoffs);
    Case kase = sut.getCase(caseId);
    assertInstanceOf(CacheUpdatedCase.class, kase);
    CaseRelease release = kase.getDeliverables().get(0).getReleases().stream()
        .filter(x -> Objects.equals(DELIVERABLE_REPORT, x.getDeliverable())).findFirst()
        .orElse(null);
    assertInstanceOf(CacheUpdatedRelease.class, release);
    CacheUpdatedRelease assignedRelease = (CacheUpdatedRelease) release;
    assertEquals(user, assignedRelease.getAssignee());
  }

  private void assertContainsSamples(TableData<Sample> data, String... sampleNames) {
    assertNotNull(data);
    assertEquals(sampleNames.length, data.getTotalCount());
    assertEquals(sampleNames.length, data.getFilteredCount());
    assertNotNull(data.getItems());
    assertEquals(sampleNames.length, data.getItems().size());
    for (String sampleName : sampleNames) {
      assertTrue(data.getItems().stream().anyMatch(sample -> sampleName.equals(sample.getName())));
    }
  }

  private Case addCase(CaseData data, int caseNumber, int requisitionNumber) {
    Case kase = mock(Case.class);
    when(kase.getId()).thenReturn("R%d_C%d".formatted(requisitionNumber, caseNumber));
    when(kase.getLatestActivityDate()).thenReturn(LocalDate.now().minusDays(caseNumber));
    when(kase.getReceipts()).thenReturn(new ArrayList<>());
    addSample(kase.getReceipts(), makeSampleName(caseNumber, "N", "A", 1));
    addSample(kase.getReceipts(), makeSampleName(caseNumber, "N", "A", 2));
    when(kase.getTests()).thenReturn(new ArrayList<>());
    addTest(kase, caseNumber, "A");
    addTest(kase, caseNumber, "B");
    when(kase.getRequisition()).thenReturn(makeRequisition(requisitionNumber));
    when(kase.getDeliverables()).thenReturn(new ArrayList<>());
    data.getCases().add(kase);
    return kase;
  }

  private void addTest(Case kase, int caseNumber, String testLetter) {
    Test test = mock(Test.class);
    when(test.getName()).thenReturn(testLetter);
    when(test.getExtractions()).thenReturn(new ArrayList<>());
    addSample(test.getExtractions(), makeSampleName(caseNumber, testLetter, "B", 1));
    addSample(test.getExtractions(), makeSampleName(caseNumber, testLetter, "B", 2));
    when(test.getLibraryPreparations()).thenReturn(new ArrayList<>());
    addSample(test.getLibraryPreparations(), makeSampleName(caseNumber, testLetter, "C", 1));
    addSample(test.getLibraryPreparations(), makeSampleName(caseNumber, testLetter, "C", 2));
    when(test.getLibraryQualifications()).thenReturn(new ArrayList<>());
    addSample(test.getLibraryQualifications(), makeSampleName(caseNumber, testLetter, "D", 1));
    addSample(test.getLibraryQualifications(), makeSampleName(caseNumber, testLetter, "D", 2));
    when(test.getFullDepthSequencings()).thenReturn(new ArrayList<>());
    addSample(test.getFullDepthSequencings(), makeSampleName(caseNumber, testLetter, "E", 1));
    addSample(test.getFullDepthSequencings(), makeSampleName(caseNumber, testLetter, "E", 2));
    kase.getTests().add(test);
  }

  // Sample names are case number + test letter (N if n/a) + gate letter (receipt=A, extract=B,
  // etc.) + sample number
  private String makeSampleName(int caseNumber, String testLetter, String gateLetter,
      int sampleNumber) {
    return caseNumber + testLetter + "-" + gateLetter + sampleNumber;
  }

  private void addSample(Collection<Sample> collection, String name) {
    // Not using mocks because we're kind-of testing hashcode for distinct filters here too

    collection.add(new SampleImpl.Builder()
        .id(name)
        .name(name)
        .donor(mock(Donor.class))
        .project("PROJ")
        .tissueOrigin("To").tissueType("T")
        .createdDate(LocalDate.now())
        .metrics(new ArrayList<>()).build());
  }

  private void addDeliverables(Case kase, String deliverableCategory, String... deliverables) {
    CaseDeliverable category = mock(CaseDeliverable.class);
    when(category.getDeliverableCategory()).thenReturn(deliverableCategory);
    when(category.getReleases()).thenReturn(new ArrayList<>());
    for (String deliverable : deliverables) {
      CaseRelease release = mock(CaseRelease.class);
      when(release.getDeliverable()).thenReturn(deliverable);
      category.getReleases().add(release);
    }
    kase.getDeliverables().add(category);
  }

  private Requisition makeRequisition(int requisitionNumber) {
    // Not using mocks because we're kind-of testing hashcode for distinct filters here too
    return new Requisition.Builder()
        .id(requisitionNumber)
        .name(String.format("REQ_%d", requisitionNumber))
        .assayIds(Collections.singleton(2L))
        .build();
  }

  private DimsumPrincipal makeInternalPrincipal() {
    DimsumPrincipal principal = mock(DimsumPrincipal.class);
    when(principal.getDisplayName()).thenReturn("Internal Test");
    when(principal.isInternal()).thenReturn(true);
    when(principal.getProjects()).thenReturn(Collections.emptySet());
    return principal;
  }

}
