package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.util.Collections;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
public class ProjectSummary {
  private final String name;
  private final Map<String, Integer> counts;

  public ProjectSummary(Builder builder) {
    this.name = requireNonNull(builder.name);
    this.counts = Collections.unmodifiableMap(builder.counts);
  }

  public String getName() {
    return name;
  }

  public Map<String, Integer> getCounts() {
    return counts;
  }

  public static Map<String, Integer> addCounts(Map<String, Integer> to,
      Map<String, Integer> added) {
    for (Map.Entry<String, Integer> entry : added.entrySet()) {
      to.put(entry.getKey(), to.getOrDefault(entry.getKey(), 0) + entry.getValue());
    }
    return to;
  }

  public static class Builder {

    private String name;
    private Map<String, Integer> counts;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder counts(Map<String, Integer> counts) {
      this.counts = counts;
      return this;
    }

    public ProjectSummary build() {
      return new ProjectSummary(this);
    }
  }
}
