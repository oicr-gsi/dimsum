package ca.on.oicr.gsi.dimsum.data;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.OmittedSample;

@Immutable
@JsonDeserialize(builder = CardeaCaseData.Builder.class)
public class CardeaCaseData {
    private final Map<Long, Assay> assaysById;
    private final List<Case> cases;
    private final List<OmittedSample> omittedSamples;
    private final ZonedDateTime timestamp;

    private CardeaCaseData(Builder builder) {
        this.assaysById = unmodifiableMap(builder.assaysById);
        this.cases = unmodifiableList(builder.cases);
        this.omittedSamples = unmodifiableList(builder.omittedSamples);
        this.timestamp = requireNonNull(builder.timestamp);
    }

    public Map<Long, Assay> getAssaysById() {
        return assaysById;
    }

    public List<Case> getCases() {
        return cases;
    }

    public List<OmittedSample> getOmittedSamples() {
        return omittedSamples;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private Map<Long, Assay> assaysById;
        private List<Case> cases;
        private List<OmittedSample> omittedSamples;
        private ZonedDateTime timestamp;

        public CardeaCaseData build() {
            return new CardeaCaseData(this);
        }

        public Builder assaysById(Map<Long, Assay> assaysById) {
            this.assaysById = assaysById;
            return this;
        }

        public Builder cases(List<Case> cases) {
            this.cases = cases;
            return this;
        }

        public Builder omittedSamples(List<OmittedSample> omittedSamples) {
            this.omittedSamples = omittedSamples;
            return this;
        }

        public Builder timestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }


    }

}
