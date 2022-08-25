package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Assay {

  private final Long id;
  private final String name;
  private final String description;
  private final String version;
  private final Map<MetricCategory, List<MetricSubcategory>> metricCategories;

  public Assay(Builder builder) {
    this.id = requireNonNull(builder.id);
    this.name = requireNonNull(builder.name);
    this.description = builder.description;
    this.version = requireNonNull(builder.version);
    Map<MetricCategory, List<MetricSubcategory>> tempMap =
        builder.metricCategories.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(),
                entry -> Collections.unmodifiableList(entry.getValue())));
    this.metricCategories = Collections.unmodifiableMap(tempMap);
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getVersion() {
    return version;
  }

  public Map<MetricCategory, List<MetricSubcategory>> getMetricCategories() {
    return metricCategories;
  }

  public static class Builder {

    private Long id;
    private String name;
    private String description;
    private String version;
    private Map<MetricCategory, List<MetricSubcategory>> metricCategories;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Builder metricCategories(Map<MetricCategory, List<MetricSubcategory>> metricCategories) {
      this.metricCategories = metricCategories;
      return this;
    }

    public Assay build() {
      return new Assay(this);
    }

  }

}
