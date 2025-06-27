package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseRelease;

public class CacheUpdatedCaseDeliverable implements CaseDeliverable {

  private final CaseDeliverable baseDeliverable;
  private final CachedSignoff cachedAnalysisReviewSignoff;
  private final CachedSignoff cachedReleaseApprovalSignoff;
  private final List<CaseRelease> cachedReleases;

  public CacheUpdatedCaseDeliverable(CaseDeliverable baseDeliverable, NabuSavedSignoff signoff) {
    this.baseDeliverable = requireNonNull(baseDeliverable);
    switch (signoff.getSignoffStepName()) {
      case ANALYSIS_REVIEW:
        this.cachedAnalysisReviewSignoff = new CachedSignoff(signoff);
        this.cachedReleaseApprovalSignoff = null;
        this.cachedReleases = null;
        break;
      case RELEASE_APPROVAL:
        this.cachedAnalysisReviewSignoff = null;
        this.cachedReleaseApprovalSignoff = new CachedSignoff(signoff);
        this.cachedReleases = null;
        break;
      case RELEASE:
        this.cachedAnalysisReviewSignoff = null;
        this.cachedReleaseApprovalSignoff = null;
        List<CaseRelease> releases = new ArrayList<>();
        for (CaseRelease release : baseDeliverable.getReleases()) {
          if (Objects.equals(release.getDeliverable(), signoff.getDeliverable())) {
            releases.add(new CacheUpdatedRelease(release, signoff));
          } else {
            releases.add(release);
          }
        }
        this.cachedReleases = Collections.unmodifiableList(releases);
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid signoff step: %s".formatted(signoff.getSignoffStepName()));
    }
  }

  @Override
  public boolean isAnalysisReviewSkipped() {
    return baseDeliverable.isAnalysisReviewSkipped();
  }

  @Override
  public LocalDate getAnalysisReviewQcDate() {
    if (cachedAnalysisReviewSignoff != null) {
      return cachedAnalysisReviewSignoff.getQcDate();
    } else {
      return baseDeliverable.getAnalysisReviewQcDate();
    }
  }

  @Override
  public String getAnalysisReviewQcNote() {
    if (cachedAnalysisReviewSignoff != null) {
      return cachedAnalysisReviewSignoff.getQcNote();
    } else {
      return baseDeliverable.getAnalysisReviewQcNote();
    }
  }

  @Override
  public AnalysisReviewQcStatus getAnalysisReviewQcStatus() {
    if (cachedAnalysisReviewSignoff != null) {
      return (AnalysisReviewQcStatus) cachedAnalysisReviewSignoff.getQcStatus();
    } else {
      return baseDeliverable.getAnalysisReviewQcStatus();
    }
  }

  @Override
  public String getAnalysisReviewQcUser() {
    if (cachedAnalysisReviewSignoff != null) {
      return cachedAnalysisReviewSignoff.getQcUser();
    } else {
      return baseDeliverable.getAnalysisReviewQcUser();
    }
  }

  @Override
  public String getDeliverableCategory() {
    return baseDeliverable.getDeliverableCategory();
  }

  @Override
  public LocalDate getLatestActivityDate() {
    return baseDeliverable.getLatestActivityDate();
  }

  @Override
  public LocalDate getReleaseApprovalQcDate() {
    if (cachedReleaseApprovalSignoff != null) {
      return cachedReleaseApprovalSignoff.getQcDate();
    } else {
      return baseDeliverable.getReleaseApprovalQcDate();
    }
  }

  @Override
  public String getReleaseApprovalQcNote() {
    if (cachedReleaseApprovalSignoff != null) {
      return cachedReleaseApprovalSignoff.getQcNote();
    } else {
      return baseDeliverable.getReleaseApprovalQcNote();
    }
  }

  @Override
  public ReleaseApprovalQcStatus getReleaseApprovalQcStatus() {
    if (cachedReleaseApprovalSignoff != null) {
      return (ReleaseApprovalQcStatus) cachedReleaseApprovalSignoff.getQcStatus();
    } else {
      return baseDeliverable.getReleaseApprovalQcStatus();
    }
  }

  @Override
  public String getReleaseApprovalQcUser() {
    if (cachedReleaseApprovalSignoff != null) {
      return cachedReleaseApprovalSignoff.getQcUser();
    } else {
      return baseDeliverable.getReleaseApprovalQcUser();
    }
  }

  @Override
  public List<CaseRelease> getReleases() {
    return cachedReleases != null ? cachedReleases : baseDeliverable.getReleases();
  }

  @Override
  public int getAnalysisReviewDaysSpent() {
    return baseDeliverable.getAnalysisReviewDaysSpent();
  }

  @Override
  public int getDeliverableDaysSpent() {
    return baseDeliverable.getDeliverableDaysSpent();
  }

  @Override
  public int getReleaseApprovalDaysSpent() {
    return baseDeliverable.getReleaseApprovalDaysSpent();
  }

  @Override
  public int getReleaseDaysSpent() {
    return baseDeliverable.getReleaseDaysSpent();
  }

}
