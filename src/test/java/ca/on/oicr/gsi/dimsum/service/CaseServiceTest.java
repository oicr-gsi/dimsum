package ca.on.oicr.gsi.dimsum.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.data.CaseData;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

public class CaseServiceTest {

  private CaseService sut;
  private CaseData caseData;

  @BeforeEach
  public void setup() {
    sut = new CaseService(null);
    caseData = mock(CaseData.class);
    when(caseData.getCases()).thenReturn(new ArrayList<>());
    addCase(caseData, 1, 1);
    addCase(caseData, 2, 2);
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

  private void addCase(CaseData data, int caseNumber, int requisitionNumber) {
    Case kase = mock(Case.class);
    when(kase.getLatestActivityDate()).thenReturn(LocalDate.now().minusDays(caseNumber));
    when(kase.getReceipts()).thenReturn(new ArrayList<>());
    addSample(kase.getReceipts(), makeSampleName(caseNumber, "N", "A", 1));
    addSample(kase.getReceipts(), makeSampleName(caseNumber, "N", "A", 2));
    when(kase.getTests()).thenReturn(new ArrayList<>());
    addTest(kase, caseNumber, "A");
    addTest(kase, caseNumber, "B");
    when(kase.getRequisition()).thenReturn(makeRequisition(requisitionNumber));
    data.getCases().add(kase);
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

    collection.add(new Sample.Builder().id(name).name(name).donor(mock(Donor.class)).project("PROJ")
        .tissueOrigin("To").tissueType("T")
        .createdDate(LocalDate.now()).build());
  }

  private Requisition makeRequisition(int requisitionNumber) {
    // Not using mocks because we're kind-of testing hashcode for distinct filters here too
    return new Requisition.Builder()
        .id(requisitionNumber)
        .name(String.format("REQ_%d", requisitionNumber))
        .assayId(2L)
        .build();
  }

}
