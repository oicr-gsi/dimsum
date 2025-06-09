package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.util.Arrays;
import java.util.Comparator;
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

public class DonorAssayReport extends Report {

  private static final ReportSection<Case> mainSection =
      new TableReportSection<Case>("Donor Assay Report", Arrays.asList(
          Column.forString("Project", DonorAssayReport::getProjectNames),
          Column.forString("Pipeline", DonorAssayReport::getProjectPipelines),
          Column.forString("Donor", kase -> kase.getDonor().getName()),
          Column.forString("External Name", kase -> kase.getDonor().getExternalName()),
          Column.forString("Assay", Case::getAssayName),
          Column.forString("Start Date", kase -> kase.getStartDate().toString()),
          Column.forString("Latest Activity", kase -> kase.getLatestActivityDate().toString()),
          Column.forString("Completed",
              kase -> CompletedGate.RELEASE.qualifyCase(kase) ? "Yes" : "No"),
          Column.forString("Stopped/Paused", DonorAssayReport::getStoppedPausedStatus),
          Column.forString("Stop/Pause Reason", DonorAssayReport::getStopPauseReason))) {

        @Override
        public List<Case> getData(CaseService caseService, JsonNode parameters) {
          Set<String> caseIds = getParameterStringSet(parameters, "caseIds");
          if (caseIds == null || caseIds.isEmpty()) {
            throw new BadRequestException("caseIds parameter missing or empty");
          }
          return caseService.getCasesByIds(caseIds).stream()
              .sorted(Comparator.comparing(DonorAssayReport::getProjectNames))
              .toList();
        }
      };

  public static final DonorAssayReport INSTANCE = new DonorAssayReport();

  private DonorAssayReport() {
    super("Donor Assay Report", mainSection);
  }

  private static String getProjectNames(Case kase) {
    return kase.getProjects().stream()
        .map(Project::getName)
        .sorted()
        .collect(Collectors.joining(", "));
  }

  private static String getProjectPipelines(Case kase) {
    return kase.getProjects().stream()
        .map(Project::getPipeline)
        .filter(Objects::nonNull)
        .sorted()
        .collect(Collectors.joining(", "));
  }

  private static String getStoppedPausedStatus(Case kase) {
    if (kase.isStopped()) {
      return "Stopped";
    } else if (kase.getRequisition().isPaused()) {
      return "Paused";
    } else {
      return "Neither";
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
    return null;
  }
}
