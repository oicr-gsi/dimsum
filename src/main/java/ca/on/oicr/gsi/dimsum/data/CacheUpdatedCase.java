package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import ca.on.oicr.gsi.cardea.data.AnalysisQcGroup;
import ca.on.oicr.gsi.cardea.data.ArchivingStatus;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.Donor;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;

public class CacheUpdatedCase implements Case {

  private final Case baseCase;
  private final List<CaseDeliverable> cacheUpdatedDeliverables;

  public CacheUpdatedCase(Case baseCase, NabuSavedSignoff signoff) {
    this.baseCase = requireNonNull(baseCase);
    requireNonNull(signoff);
    List<CaseDeliverable> deliverables = new ArrayList<>();
    for (CaseDeliverable original : baseCase.getDeliverables()) {
      if (Objects.equals(original.getDeliverableCategory(), signoff.getDeliverableType())) {
        deliverables.add(new CacheUpdatedCaseDeliverable(original, signoff));
      } else {
        deliverables.add(original);
      }
    }
    this.cacheUpdatedDeliverables = Collections.unmodifiableList(deliverables);
  }

  @Override
  public int getAnalysisReviewDaysSpent() {
    return baseCase.getAnalysisReviewDaysSpent();
  }

  @Override
  public String getAssayDescription() {
    return baseCase.getAssayDescription();
  }

  @Override
  public long getAssayId() {
    return baseCase.getAssayId();
  }

  @Override
  public String getAssayName() {
    return baseCase.getAssayName();
  }

  @Override
  public int getCaseDaysSpent() {
    return baseCase.getCaseDaysSpent();
  }

  @Override
  public List<CaseDeliverable> getDeliverables() {
    return cacheUpdatedDeliverables != null ? cacheUpdatedDeliverables : baseCase.getDeliverables();
  }

  @Override
  public Donor getDonor() {
    return baseCase.getDonor();
  }

  @Override
  public String getId() {
    return baseCase.getId();
  }

  @Override
  public LocalDate getLatestActivityDate() {
    return baseCase.getLatestActivityDate();
  }

  @Override
  public int getPauseDays() {
    return baseCase.getPauseDays();
  }

  @Override
  public Set<Project> getProjects() {
    return baseCase.getProjects();
  }

  @Override
  public List<AnalysisQcGroup> getQcGroups() {
    return baseCase.getQcGroups();
  }

  @Override
  public int getReceiptDaysSpent() {
    return baseCase.getReceiptDaysSpent();
  }

  @Override
  public List<Sample> getReceipts() {
    return baseCase.getReceipts();
  }

  @Override
  public int getReleaseApprovalDaysSpent() {
    return baseCase.getReleaseApprovalDaysSpent();
  }

  @Override
  public int getReleaseDaysSpent() {
    return baseCase.getReleaseDaysSpent();
  }

  @Override
  public Requisition getRequisition() {
    return baseCase.getRequisition();
  }

  @Override
  public LocalDate getStartDate() {
    return baseCase.getStartDate();
  }

  @Override
  public List<Test> getTests() {
    return baseCase.getTests();
  }

  @Override
  public String getTimepoint() {
    return baseCase.getTimepoint();
  }

  @Override
  public String getTissueOrigin() {
    return baseCase.getTissueOrigin();
  }

  @Override
  public String getTissueType() {
    return baseCase.getTissueType();
  }

  @Override
  public boolean isStopped() {
    return baseCase.isStopped();
  }

  @Override
  public ArchivingStatus getArchivingStatus() {
    return baseCase.getArchivingStatus();
  }

  @Override
  public String getArchivingDestination() {
    return baseCase.getArchivingDestination();
  }

  @Override
  public Integer getArchivingTtlDays() {
    return baseCase.getArchivingTtlDays();
  }

}
