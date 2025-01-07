package ca.on.oicr.gsi.dimsum.data.external;

import java.time.LocalDate;
import ca.on.oicr.gsi.cardea.data.Run;

public record ExternalRun(long id, String name, LocalDate startDate, LocalDate completionDate,
    LocalDate qcDate, Boolean qcPassed, LocalDate dataReviewDate, Boolean dataReviewPassed) {

  public ExternalRun(Run from) {
    this(from.getId(),
        from.getName(),
        from.getStartDate(),
        from.getCompletionDate(),
        from.getQcDate(),
        from.getQcPassed(),
        from.getDataReviewDate(),
        from.getDataReviewPassed());
  }

}
