package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import static ca.on.oicr.gsi.dimsum.util.DataUtils.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.StaticTableReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.shared.CaseSampleRowData;

public class FullDepthSummary extends Report {

  private static final ReportSection<CaseSampleRowData> mainSection =
      new StaticTableReportSection<CaseSampleRowData>("Full-Depth Sequencing", Arrays.asList(
          Column.forString("Case ID", x -> x.getCase().getId()),
          Column.forString("Project", x -> x.getSample().getProject()),
          Column.forString("Donor", x -> x.getCase().getDonor().getName()),
          Column.forString("External Name", x -> x.getCase().getDonor().getExternalName()),
          Column.forString("Requisition", x -> x.getCase().getRequisition().getName()),
          Column.forString("Assay", x -> x.getCase().getAssayName()),
          Column.forString("Run", x -> x.getSample().getRun().getName()),
          Column.forString("Lane", x -> x.getSample().getSequencingLane()),
          Column.forString("Library", x -> x.getSample().getName()),
          Column.forString("Group ID", x -> x.getSample().getGroupId()),
          Column.forString("QC Status", x -> getQcStatus(x.getSample())))) {

        @Override
        public List<CaseSampleRowData> getData(CaseService caseService,
            JsonNode parameters) {
          Set<String> caseIds = getParameterStringSet(parameters, "caseIds");
          if (caseIds == null) {
            throw new BadRequestException("caseIds parameter missing");
          }
          return CaseSampleRowData.listByCaseIds(caseService, caseIds);
        }

      };

  public static final FullDepthSummary INSTANCE = new FullDepthSummary();

  private FullDepthSummary() {
    super("Full-Depth Summary", mainSection);
  }

  private static String getQcStatus(Sample sample) {
    if (isPendingQc(sample) || isPendingDataReview(sample)) {
      return "Pending";
    } else if (isFailed(sample)) {
      return "FAILED";
    } else {
      // will include passing as well as "top-up required" samples
      return "Passed";
    }
  }

}
