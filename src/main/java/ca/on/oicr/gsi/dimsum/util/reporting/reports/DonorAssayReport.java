package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Project;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CompletedGate;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.TableReportSection;

public class DonorAssayReport extends Report {

  private static class RowData {

    private final Case kase;

    public RowData(Case kase, Test test, Assay assay) {
      this.kase = kase;
    }

    public Case getCase() {
      return kase;
    }

  }

  private static final ReportSection<RowData> mainSection =
      new TableReportSection<RowData>("Donor Assay Report", Arrays.asList(
          Column.forString("Project", x -> x.getCase().getProjects().stream()
              .map(Project::getName)
              .collect(Collectors.joining(", "))),
          Column.forString("Pipeline", x -> getProjectPipelines(x.getCase())),
          Column.forString("Donor", x -> x.getCase().getDonor().getName()),
          Column.forString("External Name", x -> x.getCase().getDonor().getExternalName()),
          Column.forString("Assay", x -> x.getCase().getAssayName()),
          Column.forString("Start Date", x -> x.getCase().getStartDate().toString()),
          Column.forString("Latest Activity", x -> x.getCase().getLatestActivityDate().toString()),
          Column.forString("Status", x -> getCaseStatus(x.getCase())),
          Column.forString("Stop/Pause Reason", x -> getStopPauseReason(x.getCase())))) {
        @Override
        public List<RowData> getData(CaseService caseService, JsonNode parameters) {
          Set<String> caseIds = getParameterStringSet(parameters, "caseIds");
          if (caseIds == null || caseIds.isEmpty()) {
            throw new BadRequestException("caseIds parameter missing or empty");
          }
          List<Case> cases = caseService.getCasesByIds(caseIds);
          Map<Long, Assay> assaysById = caseService.getAssaysById();
          return cases.stream()
              .flatMap(kase -> kase.getTests().stream()
                  .map(test -> new RowData(kase, test, assaysById.get(kase.getAssayId()))))
              .collect(Collectors.toList());
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
    return null;
  }
}
