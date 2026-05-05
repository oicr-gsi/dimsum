package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.time.LocalDate;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseRelease;

public class CacheUpdatedRelease implements CaseRelease {

  private final CaseRelease baseRelease;
  private final CachedSignoff cachedSignoff;
  private final String assignee;

  public CacheUpdatedRelease(CaseRelease baseRelease, NabuSavedSignoff signoff, String assignee) {
    this.baseRelease = requireNonNull(baseRelease);
    this.cachedSignoff = signoff == null ? null : new CachedSignoff(requireNonNull(signoff));
    this.assignee = assignee;
  }

  @Override
  public String getDeliverable() {
    return baseRelease.getDeliverable();
  }

  @Override
  public LocalDate getQcDate() {
    if (cachedSignoff != null) {
      return cachedSignoff.getQcDate();
    } else {
      return baseRelease.getQcDate();
    }
  }

  @Override
  public String getQcNote() {
    if (cachedSignoff != null) {
      return cachedSignoff.getQcNote();
    } else {
      return baseRelease.getQcNote();
    }
  }

  @Override
  public ReleaseQcStatus getQcStatus() {
    if (cachedSignoff != null) {
      return (ReleaseQcStatus) cachedSignoff.getQcStatus();
    } else {
      return baseRelease.getQcStatus();
    }
  }

  @Override
  public String getQcUser() {
    if (cachedSignoff != null) {
      return cachedSignoff.getQcUser();
    } else {
      return baseRelease.getQcUser();
    }
  }

  public String getAssignee() {
    return assignee;
  }

}
