package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.TableReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.shared.CaseSampleRowData;

public class DareInputSheet extends Report {

  private static final ReportSection<CaseSampleRowData> mainSection =
      new TableReportSection<CaseSampleRowData>("Full-Depth Sequencing", Arrays.asList(
          Column.forString("Library", x -> x.getSample().getName()),
          Column.forString("Run", x -> x.getSample().getRun().getName()))) {

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

  public static final DareInputSheet INSTANCE = new DareInputSheet();

  private DareInputSheet() {
    super("Dare Input Sheet", mainSection);
  }

}
