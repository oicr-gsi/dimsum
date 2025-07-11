package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.DataUtils;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.StaticTableReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.shared.CaseSampleRowData;

public class DareInputSheet extends Report {

  private static final ReportSection<CaseSampleRowData> mainSection =
      new StaticTableReportSection<CaseSampleRowData>("Full-Depth Sequencing", Arrays.asList(
          Column.forString("Library", x -> x.getSample().getName()),
          Column.forString("Run", x -> x.getSample().getRun().getName()),
          Column.forString("Lane", x -> x.getSample().getSequencingLane()))) {

        @Override
        public List<CaseSampleRowData> getData(CaseService caseService,
            JsonNode parameters) {
          Set<String> caseIds = getParameterStringSet(parameters, "caseIds");
          if (caseIds == null) {
            throw new BadRequestException("caseIds parameter missing");
          }
          boolean includeSupplemental = parameters.get("includeSupplemental").asBoolean(true);
          return CaseSampleRowData.listByCaseIds(caseService, caseIds)
              .stream()
              .filter(x -> !DataUtils.isFailed(x.getSample()))
              .filter(x -> includeSupplemental ? true
                  : Objects.equals(x.getSample().getRequisitionId(),
                      x.getCase().getRequisition().getId()))
              .toList();
        }
      };

  public static final DareInputSheet INSTANCE = new DareInputSheet();

  private DareInputSheet() {
    super("Dare Input Sheet", mainSection);
  }

}
