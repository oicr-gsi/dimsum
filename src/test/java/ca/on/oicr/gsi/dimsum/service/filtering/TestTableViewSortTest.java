package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.dimsum.data.Assay;
import ca.on.oicr.gsi.dimsum.data.Donor;
import ca.on.oicr.gsi.dimsum.data.TestTableView;

public class TestTableViewSortTest {
  private static final String[] testsOrdered = {"TEST_A", "TEST_B", "TEST_C", "TEST_D", "TEST_E"};
  private static final String[] testNames =
      {testsOrdered[0], testsOrdered[4], testsOrdered[1], testsOrdered[2], testsOrdered[3]};
  private static final String[] assaysOrdered = {"A", "B", "C", "D", "E"};
  private static final String[] testAssays =
      {assaysOrdered[1], assaysOrdered[4], assaysOrdered[2], assaysOrdered[0], assaysOrdered[3]};
  private static String[] donorsOrdered =
      {"APROJ_0001", "APROJ_0002", "BPROJ_0001", "BPROJ_0002", "BPROJ_0003"};
  private static String[] testDonors =
      {donorsOrdered[2], donorsOrdered[1], donorsOrdered[0], donorsOrdered[4], donorsOrdered[3]};

  @Test
  public void testSortByTestAscending() {
    List<TestTableView> testTableViews =
        getTestTableViewsSorted(TestTableViewSort.TEST, false);
    assertOrder(testTableViews, testTableView -> testTableView.getTest().getName(), testsOrdered,
        false);
  }

  @Test
  public void testSortByTestDescending() {
    List<TestTableView> testTableViews =
        getTestTableViewsSorted(TestTableViewSort.TEST, true);
    assertOrder(testTableViews, testTableView -> testTableView.getTest().getName(), testsOrdered,
        true);
  }

  @Test
  public void testSortByAssayAscending() {
    List<TestTableView> testTableViews =
        getTestTableViewsSorted(TestTableViewSort.ASSAY, false);
    assertOrder(testTableViews, testTableView -> testTableView.getAssay().getName(), assaysOrdered,
        false);
  }

  @Test
  public void testSortByAssayDescending() {
    List<TestTableView> testTableViews =
        getTestTableViewsSorted(TestTableViewSort.ASSAY, true);
    assertOrder(testTableViews, testTableView -> testTableView.getAssay().getName(), assaysOrdered,
        true);
  }

  @Test
  public void testSortByDonorAscending() {
    List<TestTableView> testTableViews = getTestTableViewsSorted(TestTableViewSort.DONOR, false);
    assertOrder(testTableViews, testTableView -> testTableView.getDonor().getName(), donorsOrdered,
        false);
  }

  @Test
  public void testSortByDonorDescending() {
    List<TestTableView> testTableViews = getTestTableViewsSorted(TestTableViewSort.DONOR, true);
    assertOrder(testTableViews, testTableView -> testTableView.getDonor().getName(), donorsOrdered,
        true);
  }

  private static List<TestTableView> getTestTableViewsSorted(TestTableViewSort sort,
      boolean descending) {
    Comparator<TestTableView> comparator =
        descending ? sort.comparator().reversed() : sort.comparator();
    List<TestTableView> testTableViews =
        mockTestTableViews().stream().sorted(comparator).collect(Collectors.toList());
    return testTableViews;
  }

  private static List<TestTableView> mockTestTableViews() {
    return IntStream.range(0, 5).mapToObj(TestTableViewSortTest::mockTestTableView).toList();
  }

  private static TestTableView mockTestTableView(int testTableViewNumber) {
    TestTableView testTableView = mock(TestTableView.class);
    ca.on.oicr.gsi.dimsum.data.Test test = mock(ca.on.oicr.gsi.dimsum.data.Test.class);
    when(test.getName()).thenReturn(testNames[testTableViewNumber]);
    when(testTableView.getTest()).thenReturn(test);
    Assay assay = mock(Assay.class);
    when(assay.getName()).thenReturn(testAssays[testTableViewNumber]);
    when(testTableView.getAssay()).thenReturn(assay);
    Donor donor = mock(Donor.class);
    when(donor.getName()).thenReturn(testDonors[testTableViewNumber]);
    when(testTableView.getDonor()).thenReturn(donor);
    return testTableView;
  }

  private static <T> void assertOrder(List<TestTableView> testTableViews,
      Function<TestTableView, T> getter, T[] expectedOrder,
      boolean reversed) {
    assertNotNull(testTableViews);
    assertEquals(testTableViews.size(), expectedOrder.length);
    assertEquals(expectedOrder[reversed ? 4 : 0], getter.apply(testTableViews.get(0)));
    assertEquals(expectedOrder[reversed ? 3 : 1], getter.apply(testTableViews.get(1)));
    assertEquals(expectedOrder[reversed ? 2 : 2], getter.apply(testTableViews.get(2)));
    assertEquals(expectedOrder[reversed ? 1 : 3], getter.apply(testTableViews.get(3)));
    assertEquals(expectedOrder[reversed ? 0 : 4], getter.apply(testTableViews.get(4)));
  }

}
