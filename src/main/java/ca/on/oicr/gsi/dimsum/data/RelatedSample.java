package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.util.Objects;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;

public class RelatedSample {

  private final String id;
  private final String name;
  private final Boolean qcPassed;
  private final String qcReason;
  private final String runName;
  private final Boolean runQcPassed;
  private final String sequencingLane;

  public RelatedSample(Sample sample) {
    this.id = requireNonNull(sample.getId());
    this.name = requireNonNull(sample.getName());
    if (Objects.equals(Boolean.FALSE, sample.getDataReviewPassed())) {
      this.qcPassed = false;
      this.qcReason = null;
    } else {
      this.qcPassed = sample.getQcPassed();
      this.qcReason = sample.getQcReason();
    }
    Run run = requireNonNull(sample.getRun());
    this.runName = requireNonNull(run.getName());
    if (Objects.equals(Boolean.FALSE, run.getDataReviewPassed())) {
      this.runQcPassed = false;
    } else {
      this.runQcPassed = run.getQcPassed();
    }
    this.sequencingLane = requireNonNull(sample.getSequencingLane());
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Boolean getQcPassed() {
    return qcPassed;
  }

  public String getQcReason() {
    return qcReason;
  }

  public String getRunName() {
    return runName;
  }

  public Boolean getRunQcPassed() {
    return runQcPassed;
  }

  public String getSequencingLane() {
    return sequencingLane;
  }

}
