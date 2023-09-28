package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import ca.on.oicr.gsi.cardea.data.Requisition;

public class RequisitionSortTest {

  private static final String[] namesOrdered = {"Requisition A", "Requisition B", "Requisition C"};
  private static final String[] requisitionNames =
      {namesOrdered[1], namesOrdered[0], namesOrdered[2]};

  @Test
  public void testSortByNameAscending() {
    List<Requisition> requisitions = getRequisitionsSorted(RequisitionSort.NAME, false);
    assertOrder(requisitions, Requisition::getName, namesOrdered, false);
  }

  @Test
  public void testSortByNameDescending() {
    List<Requisition> requisitions = getRequisitionsSorted(RequisitionSort.NAME, true);
    assertOrder(requisitions, Requisition::getName, namesOrdered, true);
  }

  private static List<Requisition> getRequisitionsSorted(RequisitionSort sort, boolean descending) {
    Comparator<Requisition> comparator =
        descending ? sort.comparator().reversed() : sort.comparator();
    List<Requisition> requisitions = mockRequisitions().stream().sorted(comparator).toList();
    return requisitions;
  }

  private static List<Requisition> mockRequisitions() {
    return IntStream.range(0, 3).mapToObj(RequisitionSortTest::mockRequisition).toList();
  }

  private static Requisition mockRequisition(int requisitionNumber) {
    Requisition requisition = mock(Requisition.class);
    when(requisition.getName()).thenReturn(requisitionNames[requisitionNumber]);
    return requisition;
  }

  private static <T> void assertOrder(List<Requisition> requisitions,
      Function<Requisition, T> getter, T[] expectedOrder, boolean reversed) {
    assertNotNull(requisitions);
    assertEquals(requisitions.size(), expectedOrder.length);
    assertEquals(expectedOrder[reversed ? 2 : 0], getter.apply(requisitions.get(0)));
    assertEquals(expectedOrder[reversed ? 1 : 1], getter.apply(requisitions.get(1)));
    assertEquals(expectedOrder[reversed ? 0 : 2], getter.apply(requisitions.get(2)));
  }

}
