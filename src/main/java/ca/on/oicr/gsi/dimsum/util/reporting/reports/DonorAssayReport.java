package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CompletedGate;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.TableReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.reports.shared.CaseSampleRowData;

public class DonorAssayReport extends Report {

  private static final ReportSection<CaseSampleRowData> mainSection =
      new TableReportSection<CaseSampleRowData>("Donor Assay Report", Arrays.asList(
          Column.forString("Project", x -> x.getSample().getProject()),
          Column.forString("Pipeline", x -> getProjectPipelines(x.getCase())),
          Column.forString("Donor", x -> x.getCase().getDonor().getName()),
          Column.forString("External Name", x -> x.getCase().getDonor().getExternalName()),
          Column.forString("Assay", x -> x.getCase().getAssayName()),
          Column.forString("Start Date", x -> getStartDate(x.getCase()).toString()),
          Column.forString("Latest Activity", x -> getLatestActivityDate(x.getCase()).toString()),
          Column.forString("Status", x -> getCaseStatus(x.getCase())),
          Column.forString("Stop/Pause Reason", x -> getStopPauseReason(x.getCase())))) {
        @Override
        public List<CaseSampleRowData> getData(CaseService caseService, JsonNode parameters) {
          Set<String> caseIds = getParameterStringSet(parameters, "caseIds");
          if (caseIds == null) {
            throw new BadRequestException("caseIds parameter missing");
          }
          return CaseSampleRowData.listByCaseIds(caseService, caseIds);
        }
      };

  public static final DonorAssayReport INSTANCE = new DonorAssayReport();

  private DonorAssayReport() {
    super("Donor Assay Report", mainSection);
  }

  private static String getProjectPipelines(Case kase) {
    return kase.getProjects().stream()
        .map(Project::getPipeline)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(", "));
  }

  private static LocalDate getStartDate(Case kase) {
    return kase.getStartDate();
  }

  private static LocalDate getLatestActivityDate(Case kase) {
    return kase.getLatestActivityDate();
  }

  private static String getCaseStatus(Case kase) {
    if (CompletedGate.RELEASE.qualifyCase(kase)) {
      return "Completed";
    } else if (kase.isStopped()) {
      return "Failed";
    } else if (kase.getRequisition().isPaused()) {
      return "Paused";
    } else {
      return "In Progress";
    }
  }

  private static String getStopPauseReason(Case kase) {
    if (kase.isStopped()) {
      return kase.getRequisition().getStopReason() != null
          ? kase.getRequisition().getStopReason()
          : "Reason Unspecified";
    } else if (kase.getRequisition().isPaused()) {
      return kase.getRequisition().getPauseReason() != null
          ? kase.getRequisition().getPauseReason()
          : "Reason Unspecified";
    }
    return "";
  }
}
