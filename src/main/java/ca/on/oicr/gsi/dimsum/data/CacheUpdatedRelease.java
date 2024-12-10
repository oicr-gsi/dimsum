package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.time.LocalDate;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseRelease;

public class CacheUpdatedRelease implements CaseRelease {

  private final CaseRelease baseRelease;
  private final CachedSignoff cachedSignoff;

  public CacheUpdatedRelease(CaseRelease baseRelease, NabuSavedSignoff signoff) {
    this.baseRelease = requireNonNull(baseRelease);
    this.cachedSignoff = new CachedSignoff(requireNonNull(signoff));
  }

  @Override
  public String getDeliverable() {
    return baseRelease.getDeliverable();
  }

  @Override
  public LocalDate getQcDate() {
    return cachedSignoff.getQcDate();
  }

  @Override
  public String getQcNote() {
    return cachedSignoff.getQcNote();
  }

  @Override
  public ReleaseQcStatus getQcStatus() {
    return (ReleaseQcStatus) cachedSignoff.getQcStatus();
  }

  @Override
  public String getQcUser() {
    return cachedSignoff.getQcUser();
  }

}
