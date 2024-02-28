package ca.on.oicr.gsi.dimsum.data;

import java.time.LocalDate;

public class CachedSignoff {

  private final Boolean qcPassed;
  private final String qcUser;
  private final LocalDate qcDate;
  private final String qcNote;

  public CachedSignoff(Boolean qcPassed, String qcUser, LocalDate qcDate, String qcNote) {
    this.qcPassed = qcPassed;
    this.qcUser = qcUser;
    this.qcDate = qcDate;
    this.qcNote = qcNote;
  }

  public CachedSignoff(NabuSavedSignoff nabuSignoff) {
    this.qcPassed = nabuSignoff.getQcPassed();
    this.qcUser = nabuSignoff.getUsername();
    this.qcDate = nabuSignoff.getCreated().toLocalDate();
    this.qcNote = nabuSignoff.getComment();
  }

  public Boolean getQcPassed() {
    return qcPassed;
  }

  public String getQcUser() {
    return qcUser;
  }

  public LocalDate getQcDate() {
    return qcDate;
  }

  public String getQcNote() {
    return qcNote;
  }

}
