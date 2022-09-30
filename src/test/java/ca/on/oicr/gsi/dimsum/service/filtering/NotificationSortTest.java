package ca.on.oicr.gsi.dimsum.service.filtering;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ca.on.oicr.gsi.dimsum.data.Notification;
import ca.on.oicr.gsi.dimsum.data.Run;

public class NotificationSortTest {

  private static final String[] namesOrdered = {"Run_A", "Run_B", "Run_C"};
  private static final String[] names = {namesOrdered[1], namesOrdered[2], namesOrdered[0]};
  private static final LocalDate[] datesOrdered = {LocalDate.of(2022, 1, 1),
      LocalDate.of(2022, 1, 2), LocalDate.of(2022, 2, 1)};
  private static final LocalDate[] dates = {datesOrdered[2], datesOrdered[0], datesOrdered[1]};

  @Test
  public void testSortByRunAscending() {
    List<Notification> notifications = getNotificationsSorted(NotificationSort.RUN, false);
    assertOrder(notifications, x -> x.getRun().getName(), namesOrdered, false);
  }

  @Test
  public void testSortByRunDescending() {
    List<Notification> notifications = getNotificationsSorted(NotificationSort.RUN, true);
    assertOrder(notifications, x -> x.getRun().getName(), namesOrdered, true);
  }

  @Test
  public void testSortByCompletionDateAscending() {
    List<Notification> notifications =
        getNotificationsSorted(NotificationSort.COMPLETION_DATE, false);
    assertOrder(notifications, x -> x.getRun().getCompletionDate(), datesOrdered, false);
  }

  @Test
  public void testSortByCompletionDateDescending() {
    List<Notification> notifications =
        getNotificationsSorted(NotificationSort.COMPLETION_DATE, true);
    assertOrder(notifications, x -> x.getRun().getCompletionDate(), datesOrdered, true);
  }

  private static <T> void assertOrder(List<Notification> notifications,
      Function<Notification, T> getter, T[] expectedOrder,
      boolean reversed) {
    assertNotNull(notifications);
    assertEquals(notifications.size(), expectedOrder.length);
    assertEquals(expectedOrder[reversed ? 2 : 0], getter.apply(notifications.get(0)));
    assertEquals(expectedOrder[reversed ? 1 : 1], getter.apply(notifications.get(1)));
    assertEquals(expectedOrder[reversed ? 0 : 2], getter.apply(notifications.get(2)));
  }

  private static List<Notification> getNotificationsSorted(NotificationSort sort,
      boolean descending) {
    Comparator<Notification> comparator =
        descending ? sort.comparator().reversed() : sort.comparator();
    return IntStream.range(0, 3)
        .mapToObj(NotificationSortTest::mockNotification)
        .sorted(comparator)
        .toList();
  }

  private static Notification mockNotification(int runNumber) {
    Notification notification = Mockito.mock(Notification.class);
    Run run = mockRun(runNumber);
    Mockito.when(notification.getRun()).thenReturn(run);
    return notification;
  }

  private static Run mockRun(int runNumber) {
    Run run = Mockito.mock(Run.class);
    Mockito.when(run.getName()).thenReturn(names[runNumber]);
    Mockito.when(run.getCompletionDate()).thenReturn(dates[runNumber]);
    return run;
  }

}
