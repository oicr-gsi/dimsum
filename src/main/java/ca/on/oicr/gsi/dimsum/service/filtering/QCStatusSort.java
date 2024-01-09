package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.List;
import ca.on.oicr.gsi.cardea.data.Sample;

public class QCStatusSort implements Comparator<Sample> {

    private boolean reverseOrder;

    public void setReverseOrder(boolean reverseOrder) {
        this.reverseOrder = reverseOrder;
    }

    public static final List<String> QC_STATUS_ORDER =
            List.of("pendingQC", "dataReview", "topUp", "passed", "failed");

    public static String getSampleQcStatus(Sample sample) {
        if (PendingState.Helpers.isPendingQc(sample)) {
            return "pendingQC";
        } else if (PendingState.Helpers.isPendingDataReview(sample)) {
            return "dataReview";
        } else if (PendingState.Helpers.isTopUpRequired(sample)) {
            return "topUp";
        } else if (PendingState.Helpers.isPassed(sample)) {
            return "passed";
        } else {
            return "failed";
        }
    }

    @Override
    public int compare(Sample o1, Sample o2) {
        String sampleQcStatus1 = getSampleQcStatus(o1);
        String sampleQcStatus2 = getSampleQcStatus(o2);
        int index1 = QC_STATUS_ORDER.indexOf(sampleQcStatus1);
        int index2 = QC_STATUS_ORDER.indexOf(sampleQcStatus2);
        if (index1 == -1 || index2 == -1) {
            throw new RuntimeException("QC Status not found");
        }
        if (reverseOrder)
            return index1 - index2;
        else
            return index2 - index1;
    }
}
