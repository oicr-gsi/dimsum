package ca.on.oicr.gsi.dimsum.service.filtering;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Predicate;
import ca.on.oicr.gsi.cardea.data.Run;;

public enum RunFilterKey {

  // @formatter:off
  NAME(string -> run -> run.getName().toLowerCase().startsWith(string.toLowerCase())),
  COMPLETED_BEFORE(string -> run ->
      run.getCompletionDate() != null && run.getCompletionDate().isBefore(LocalDate.parse(string))),
  COMPLETED_AFTER(string -> run ->
      run.getCompletionDate() != null && run.getCompletionDate().isAfter(LocalDate.parse(string)));
  // @formatter:on

  private final Function<String, Predicate<Run>> create;

  private RunFilterKey(Function<String, Predicate<Run>> create) {
    this.create = create;
  }

  public Function<String, Predicate<Run>> create() {
    return create;
  }
}
