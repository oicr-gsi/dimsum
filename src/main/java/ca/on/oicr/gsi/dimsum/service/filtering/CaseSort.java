package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;

import ca.on.oicr.gsi.dimsum.data.Case;

public enum CaseSort {

  // @formatter:off
  ASSAY(Comparator.comparing(Case::getAssayName)),
  DONOR(Comparator.comparing(kase -> kase.getDonor().getName())),
  RECEIPT_DATE(Comparator.comparing(Case::getEarliestReceiptDate)),
  LAST_ACTIVITY(Comparator.comparing(Case::getLatestActivityDate));
  // @formatter:on

  private final Comparator<Case> comparator;

  private CaseSort(Comparator<Case> comparator) {
    this.comparator = comparator;
  }

  public Comparator<Case> comparator() {
    return comparator;
  }

}
