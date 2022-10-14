package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.dimsum.data.Notification;

public enum NotificationSort {

  // @formatter:off
  RUN("Run", Comparator.comparing(x -> x.getRun().getName())),
  COMPLETION_DATE("Completion Date", Comparator.comparing(x -> x.getRun().getCompletionDate()));
  // @formatter:on

  private static final Map<String, NotificationSort> map = Stream.of(NotificationSort.values())
      .collect(Collectors.toMap(NotificationSort::getLabel, Function.identity()));

  public static NotificationSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<Notification> comparator;

  private NotificationSort(String label, Comparator<Notification> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<Notification> comparator() {
    return comparator;
  }

}
