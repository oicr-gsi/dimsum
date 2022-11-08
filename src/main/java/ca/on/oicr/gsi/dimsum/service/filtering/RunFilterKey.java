package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.function.Function;
import java.util.function.Predicate;
import ca.on.oicr.gsi.dimsum.data.Run;;

public enum RunFilterKey {
  NAME(string -> run -> run.getName().toLowerCase().startsWith(string.toLowerCase()));

  private final Function<String, Predicate<Run>> create;

  private RunFilterKey(Function<String, Predicate<Run>> create) {
    this.create = create;
  }

  public Function<String, Predicate<Run>> create() {
    return create;
  }
}
