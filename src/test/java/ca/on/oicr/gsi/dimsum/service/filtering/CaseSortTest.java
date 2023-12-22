package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.AssayTargets;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.RequisitionQc;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;

public class CaseSortTest {

  private static final String[] assaysOrdered = {"A", "B", "C", "D", "E"};
  private static final String[] caseAssays =
      {assaysOrdered[1], assaysOrdered[4], assaysOrdered[2], assaysOrdered[0], assaysOrdered[3]};

  private static String[] donorsOrdered =
      {"APROJ_0001", "APROJ_0002", "BPROJ_0001", "BPROJ_0002", "BPROJ_0003"};
  private static String[] caseDonors =
      {donorsOrdered[2], donorsOrdered[1], donorsOrdered[0], donorsOrdered[4], donorsOrdered[3]};
  private static LocalDate[] datesOrdered = {LocalDate.of(2021, 03, 13), LocalDate.of(2021, 06, 14),
      LocalDate.of(2022, 01, 01), LocalDate.of(2022, 06, 10), LocalDate.of(2022, 06, 13)};
  private static LocalDate[] caseReceiptDates =
      {datesOrdered[0], datesOrdered[4], datesOrdered[1], datesOrdered[2], datesOrdered[3]};
  private static LocalDate[] caseActivityDates =
      {datesOrdered[4], datesOrdered[1], datesOrdered[2], datesOrdered[3], datesOrdered[0]};

  private static Map<Long, Assay> mockAssaysById = makeAssays();

  @org.junit.jupiter.api.Test
  public void testSortByAssayAscending() {
    List<Case> cases = getCasesSorted(CaseSort.ASSAY, false);
    assertOrder(cases, Case::getAssayName, assaysOrdered, false);
  }

  @org.junit.jupiter.api.Test
  public void testSortByAssayDescending() {
    List<Case> cases = getCasesSorted(CaseSort.ASSAY, true);
    assertOrder(cases, Case::getAssayName, assaysOrdered, true);
  }

  @org.junit.jupiter.api.Test
  public void testSortByDonorAscending() {
    List<Case> cases = getCasesSorted(CaseSort.DONOR, false);
    assertOrder(cases, kase -> kase.getDonor().getName(), donorsOrdered, false);
  }

  @org.junit.jupiter.api.Test
  public void testSortByDonorDescending() {
    List<Case> cases = getCasesSorted(CaseSort.DONOR, true);
    assertOrder(cases, kase -> kase.getDonor().getName(), donorsOrdered, true);
  }

  @org.junit.jupiter.api.Test
  public void testSortByReceiptDateAscending() {
    List<Case> cases = getCasesSorted(CaseSort.START_DATE, false);
    assertOrder(cases, Case::getStartDate, datesOrdered, false);
  }

  @org.junit.jupiter.api.Test
  public void testSortByReceiptDateDescending() {
    List<Case> cases = getCasesSorted(CaseSort.START_DATE, true);
    assertOrder(cases, Case::getStartDate, datesOrdered, true);
  }

  @org.junit.jupiter.api.Test
  public void testSortByLastActivityAscending() {
    List<Case> cases = getCasesSorted(CaseSort.LAST_ACTIVITY, false);
    assertOrder(cases, Case::getLatestActivityDate, datesOrdered, false);
  }

  @org.junit.jupiter.api.Test
  public void testSortByLastActivityDescending() {
    List<Case> cases = getCasesSorted(CaseSort.LAST_ACTIVITY, true);
    assertOrder(cases, Case::getLatestActivityDate, datesOrdered, true);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyBothCompleted() {
    Case a = mockEmptyCase(1L);
    a.getRequisition().getReleases().add(mockRequisitionQc(true, 2023, 1, 1));

    Case b = mockEmptyCase(1L);
    b.getRequisition().getReleases().add(mockRequisitionQc(true, 2023, 1, 1));

    // Both completed, should be considered equivalent
    testUrgencyComparator(a, b, true);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyOneCompleted() {
    Case a = mockEmptyCase(1L);

    Case b = mockEmptyCase(1L);
    b.getRequisition().getReleases().add(mockRequisitionQc(true, 2023, 1, 1));

    // B completed. A is more urgent
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyBothPaused() {
    Case a = mockEmptyCase(1L);
    when(a.getRequisition().isPaused()).thenReturn(true);

    Case b = mockEmptyCase(1L);
    when(b.getRequisition().isPaused()).thenReturn(true);

    // Both paused, should be considered equivalent
    testUrgencyComparator(a, b, true);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyOnePaused() {
    Case a = mockEmptyCase(1L);

    Case b = mockEmptyCase(1L);
    when(b.getRequisition().isPaused()).thenReturn(true);

    // B paused. A is more urgent
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyBothOverdue() {
    Case a = mockEmptyCase(1L);
    when(a.getCaseDaysSpent()).thenReturn(100);

    Case b = mockEmptyCase(1L);
    when(b.getCaseDaysSpent()).thenReturn(99);

    // Case A is further overdue
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyEquallyOverdue() {
    Case a = mockEmptyCase(1L);
    when(a.getCaseDaysSpent()).thenReturn(100);

    Case b = mockEmptyCase(1L);
    when(b.getCaseDaysSpent()).thenReturn(100);

    // Both overdue by the same amount
    testUrgencyComparator(a, b, true);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyOneOverdue() {
    Case a = mockEmptyCase(1L);
    when(a.getCaseDaysSpent()).thenReturn(100);

    Case b = mockEmptyCase(1L);
    when(b.getCaseDaysSpent()).thenReturn(12);

    // Case A is overdue, case B isn't. A is more urgent
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyOneBehind() {
    Case a = mockEmptyCase(1L);
    when(a.getCaseDaysSpent()).thenReturn(10);

    Case b = mockEmptyCase(1L);
    when(b.getCaseDaysSpent()).thenReturn(1);

    // Case A is behind on receipt, B isn't
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyBothBehind() {
    Case a = mockEmptyCase(1L);
    when(a.getCaseDaysSpent()).thenReturn(12);

    Case b = mockEmptyCase(1L);
    when(b.getCaseDaysSpent()).thenReturn(10);

    // Both behind on receipt, but A is more behind
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyBothBehindDifferentGates() {
    // A 7 days behind on library prep (15d target)
    Case a = mockEmptyCase(1L);
    a.getReceipts().add(mockPassedSample());
    a.getTests().get(0).getExtractions().add(mockPassedSample());
    when(a.getCaseDaysSpent()).thenReturn(19);

    // B 2 days behind on receipt (5d target)
    Case b = mockEmptyCase(1L);
    when(b.getCaseDaysSpent()).thenReturn(7);

    // A is more behind on its current step
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyBothBehindOneMultipleGates() {
    // A 8 days behind on receipt (5d target), also 3 days behind on extraction (10d target)
    Case a = mockEmptyCase(1L);
    when(a.getCaseDaysSpent()).thenReturn(13);

    // B 4 days behind on extraction (10d target)
    Case b = mockEmptyCase(1L);
    b.getReceipts().add(mockPassedSample());
    when(b.getCaseDaysSpent()).thenReturn(14);

    // A is more behind on its earliest incomplete step
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyNeitherBehind() {
    Case a = mockEmptyCase(1L);
    when(a.getCaseDaysSpent()).thenReturn(3);

    Case b = mockEmptyCase(1L);
    when(b.getCaseDaysSpent()).thenReturn(1);

    // Neither behind, but A has less time remaining
    testUrgencyComparator(a, b);
  }

  @org.junit.jupiter.api.Test
  public void testSortByUrgencyNeitherBehindDifferentGates() {
    // A 3 days remaining on library prep (15d target)
    Case a = mockEmptyCase(1L);
    a.getReceipts().add(mockPassedSample());
    a.getTests().get(0).getExtractions().add(mockPassedSample());
    when(a.getCaseDaysSpent()).thenReturn(12);

    // B 3 days remaining on receipt (5d target)
    Case b = mockEmptyCase(1L);
    when(b.getCaseDaysSpent()).thenReturn(2);

    // Neither behind, but A has less time remaining at case level
    testUrgencyComparator(a, b);
  }

  private static Map<Long, Assay> makeAssays() {
    Map<Long, Assay> assaysById = new HashMap<>();

    // Assay 1 has targets
    Assay assay1 = mock(Assay.class);
    when(assay1.getId()).thenReturn(1L);
    AssayTargets targets1 = mock(AssayTargets.class);
    when(targets1.getCaseDays()).thenReturn(45);
    when(targets1.getReceiptDays()).thenReturn(5);
    when(targets1.getExtractionDays()).thenReturn(10);
    when(targets1.getLibraryPreparationDays()).thenReturn(15);
    when(targets1.getLibraryQualificationDays()).thenReturn(20);
    when(targets1.getFullDepthSequencingDays()).thenReturn(25);
    when(targets1.getAnalysisReviewDays()).thenReturn(30);
    when(targets1.getReleaseApprovalDays()).thenReturn(35);
    when(targets1.getReleaseDays()).thenReturn(40);
    when(assay1.getTargets()).thenReturn(targets1);
    assaysById.put(1L, assay1);

    // Assay 2 has no targets set
    Assay assay2 = mock(Assay.class);
    when(assay2.getId()).thenReturn(2L);
    AssayTargets targets2 = mock(AssayTargets.class);
    when(assay2.getTargets()).thenReturn(targets2);
    assaysById.put(2L, assay2);

    return assaysById;
  }

  private static Case mockEmptyCase(Long assayId) {
    Case kase = mock(Case.class);
    when(kase.getAssayId()).thenReturn(assayId);

    Requisition requisition = mock(Requisition.class);
    when(requisition.getAssayId()).thenReturn(assayId);
    List<RequisitionQc> analysisReviews = new ArrayList<>();
    when(requisition.getAnalysisReviews()).thenReturn(analysisReviews);
    List<RequisitionQc> releaseApprovals = new ArrayList<>();
    when(requisition.getReleaseApprovals()).thenReturn(releaseApprovals);
    List<RequisitionQc> releases = new ArrayList<>();
    when(requisition.getReleases()).thenReturn(releases);
    when(kase.getRequisition()).thenReturn(requisition);

    List<Sample> receipts = new ArrayList<>();
    when(kase.getReceipts()).thenReturn(receipts);

    List<Test> tests = new ArrayList<>();
    Test test = mock(Test.class);
    List<Sample> extractions = new ArrayList<>();
    when(test.getExtractions()).thenReturn(extractions);
    List<Sample> libraries = new ArrayList<>();
    when(test.getLibraryPreparations()).thenReturn(libraries);
    List<Sample> qualifications = new ArrayList<>();
    when(test.getLibraryQualifications()).thenReturn(qualifications);
    List<Sample> sequencings = new ArrayList<>();
    when(test.getFullDepthSequencings()).thenReturn(sequencings);
    tests.add(test);
    when(kase.getTests()).thenReturn(tests);

    return kase;
  }

  private static Sample mockPassedSample() {
    Sample sample = mock(Sample.class);
    when(sample.getQcDate()).thenReturn(LocalDate.now());
    when(sample.getQcPassed()).thenReturn(true);
    return sample;
  }

  private static void testUrgencyComparator(Case moreUrgent, Case lessUrgent) {
    testUrgencyComparator(moreUrgent, lessUrgent, false);
  }

  private static void testUrgencyComparator(Case moreUrgent, Case lessUrgent, boolean equivalent) {
    int forwardCompare =
        CaseSort.URGENCY.comparator(mockAssaysById).compare(moreUrgent, lessUrgent);
    int reverseCompare =
        CaseSort.URGENCY.comparator(mockAssaysById).compare(lessUrgent, moreUrgent);
    if (equivalent) {
      assertEquals(0, forwardCompare);
      assertEquals(0, reverseCompare);
    } else {
      assertTrue(forwardCompare > 0);
      assertTrue(reverseCompare < 0);
    }
  }

  private static RequisitionQc mockRequisitionQc(boolean qcPassed, int year, int month, int day) {
    RequisitionQc qc = mock(RequisitionQc.class);
    when(qc.getQcDate()).thenReturn(LocalDate.of(year, month, day));
    when(qc.isQcPassed()).thenReturn(qcPassed);
    return qc;
  }

  private static <T> void assertOrder(List<Case> cases, Function<Case, T> getter, T[] expectedOrder,
      boolean reversed) {
    assertNotNull(cases);
    assertEquals(cases.size(), expectedOrder.length);
    assertEquals(expectedOrder[reversed ? 4 : 0], getter.apply(cases.get(0)));
    assertEquals(expectedOrder[reversed ? 3 : 1], getter.apply(cases.get(1)));
    assertEquals(expectedOrder[reversed ? 2 : 2], getter.apply(cases.get(2)));
    assertEquals(expectedOrder[reversed ? 1 : 3], getter.apply(cases.get(3)));
    assertEquals(expectedOrder[reversed ? 0 : 4], getter.apply(cases.get(4)));
  }

  private static List<Case> getCasesSorted(CaseSort sort, boolean descending) {
    Comparator<Case> comparator = sort.comparator(mockAssaysById);
    if (descending) {
      comparator = comparator.reversed();
    }
    List<Case> cases = mockCases().stream().sorted(comparator).collect(Collectors.toList());
    return cases;
  }

  private static List<Case> mockCases() {
    return IntStream.range(0, 5).mapToObj(CaseSortTest::mockCase).toList();
  }

  private static Case mockCase(int caseNumber) {
    Case kase = mock(Case.class);
    when(kase.getAssayName()).thenReturn(caseAssays[caseNumber]);
    Donor donor = mock(Donor.class);
    when(donor.getName()).thenReturn(caseDonors[caseNumber]);
    when(kase.getDonor()).thenReturn(donor);
    when(kase.getStartDate()).thenReturn(caseReceiptDates[caseNumber]);
    when(kase.getLatestActivityDate()).thenReturn(caseActivityDates[caseNumber]);
    return kase;
  }
}
