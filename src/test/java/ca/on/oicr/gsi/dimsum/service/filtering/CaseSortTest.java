package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.dimsum.data.Assay;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.Donor;

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

  @Test
  public void testSortByAssayAscending() {
    List<Case> cases = getCasesSorted(CaseSort.ASSAY, false);
    assertOrder(cases, kase -> kase.getAssay().getName(), assaysOrdered, false);
  }

  @Test
  public void testSortByAssayDescending() {
    List<Case> cases = getCasesSorted(CaseSort.ASSAY, true);
    assertOrder(cases, kase -> kase.getAssay().getName(), assaysOrdered, true);
  }

  @Test
  public void testSortByDonorAscending() {
    List<Case> cases = getCasesSorted(CaseSort.DONOR, false);
    assertOrder(cases, kase -> kase.getDonor().getName(), donorsOrdered, false);
  }

  @Test
  public void testSortByDonorDescending() {
    List<Case> cases = getCasesSorted(CaseSort.DONOR, true);
    assertOrder(cases, kase -> kase.getDonor().getName(), donorsOrdered, true);
  }

  @Test
  public void testSortByReceiptDateAscending() {
    List<Case> cases = getCasesSorted(CaseSort.START_DATE, false);
    assertOrder(cases, Case::getStartDate, datesOrdered, false);
  }

  @Test
  public void testSortByReceiptDateDescending() {
    List<Case> cases = getCasesSorted(CaseSort.START_DATE, true);
    assertOrder(cases, Case::getStartDate, datesOrdered, true);
  }

  @Test
  public void testSortByLastActivityAscending() {
    List<Case> cases = getCasesSorted(CaseSort.LAST_ACTIVITY, false);
    assertOrder(cases, Case::getLatestActivityDate, datesOrdered, false);
  }

  @Test
  public void testSortByLastActivityDescending() {
    List<Case> cases = getCasesSorted(CaseSort.LAST_ACTIVITY, true);
    assertOrder(cases, Case::getLatestActivityDate, datesOrdered, true);
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
    Comparator<Case> comparator = descending ? sort.comparator().reversed() : sort.comparator();
    List<Case> cases = mockCases().stream().sorted(comparator).collect(Collectors.toList());
    return cases;
  }

  private static List<Case> mockCases() {
    return IntStream.range(0, 5).mapToObj(CaseSortTest::mockCase).toList();
  }

  private static Case mockCase(int caseNumber) {
    Case kase = mock(Case.class);
    Assay assay = mock(Assay.class);
    when(assay.getName()).thenReturn(caseAssays[caseNumber]);
    when(kase.getAssay()).thenReturn(assay);
    Donor donor = mock(Donor.class);
    when(donor.getName()).thenReturn(caseDonors[caseNumber]);
    when(kase.getDonor()).thenReturn(donor);
    when(kase.getStartDate()).thenReturn(caseReceiptDates[caseNumber]);
    when(kase.getLatestActivityDate()).thenReturn(caseActivityDates[caseNumber]);
    return kase;
  }

}
