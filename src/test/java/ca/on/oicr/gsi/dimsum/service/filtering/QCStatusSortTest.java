package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class QCStatusSortTest {

  public List<String> QC_STATUS_ORDER =
      Arrays.asList("pendingQC", "dataReview", "topUp", "passed", "failed");

  @Test
  public void testSortByStatusAscending() {
    QCStatusSort qcStatusSort = new QCStatusSort();
    qcStatusSort.setReverseOrder(false);
    Collections.sort(QC_STATUS_ORDER, Collections.reverseOrder());
  }

  @Test
  public void testSortByStatusDescending() {
    QCStatusSort qcStatusSort = new QCStatusSort();
    qcStatusSort.setReverseOrder(true);
    Collections.sort(QC_STATUS_ORDER);
  }
}
