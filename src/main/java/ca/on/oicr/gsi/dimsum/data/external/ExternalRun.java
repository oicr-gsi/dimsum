package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import ca.on.oicr.gsi.cardea.data.Run;

public class ExternalRun {

  private final long id;
  private final String name;
  private final LocalDate startDate;
  private final LocalDate completionDate;
  private final LocalDate qcDate;
  private final Boolean qcPassed;
  private final LocalDate dataReviewDate;
  private final Boolean dataReviewPassed;

  public ExternalRun(Run from) {
    id = from.getId();
    name = from.getName();
    startDate = from.getStartDate();
    completionDate = from.getCompletionDate();
    qcDate = from.getQcDate();
    qcPassed = from.getQcPassed();
    dataReviewDate = from.getDataReviewDate();
    dataReviewPassed = from.getDataReviewPassed();
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getCompletionDate() {
    return completionDate;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

  public Boolean getQcPassed() {
    return qcPassed;
  }

  public LocalDate getDataReviewDate() {
    return dataReviewDate;
  }

  public Boolean getDataReviewPassed() {
    return dataReviewPassed;
  }

}
