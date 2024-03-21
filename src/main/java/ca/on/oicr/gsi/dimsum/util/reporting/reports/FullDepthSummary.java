package ca.on.oicr.gsi.dimsum.util.reporting.reports;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.util.reporting.Column;
import ca.on.oicr.gsi.dimsum.util.reporting.Report;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection;
import ca.on.oicr.gsi.dimsum.util.reporting.ReportSection.TableReportSection;

public class FullDepthSummary extends Report {

  private static class RowData {
    private Case kase;
    private Sample sample;

    public RowData(Case kase, Sample sample) {
      this.kase = kase;
      this.sample = sample;
    }

    public Case getCase() {
      return kase;
    }

    public Sample getSample() {
      return sample;
    }
  }

  private static final ReportSection<RowData> mainSection =
      new TableReportSection<RowData>("Full-Depth Sequencing", Arrays.asList(
          Column.forString("Case ID", x -> x.getCase().getId()),
          Column.forString("Project", x -> x.getSample().getProject()),
          Column.forString("Donor", x -> x.getCase().getDonor().getName()),
          Column.forString("External Name", x -> x.getCase().getDonor().getExternalName()),
          Column.forString("Requisition", x -> x.getCase().getRequisition().getName()),
          Column.forString("Assay", x -> x.getCase().getAssayName()),
          Column.forString("Run", x -> x.getSample().getRun().getName()),
          Column.forString("Lane", x -> x.getSample().getSequencingLane()),
          Column.forString("Library", x -> x.getSample().getName()),
          Column.forString("Group ID", x -> x.getSample().getGroupId()))) {

        @Override
        public List<RowData> getData(CaseService caseService, Map<String, String> parameters) {
          Set<String> caseIds = getParameterStringSet(parameters, "caseIds");
          if (caseIds == null) {
            throw new BadRequestException("caseIds parameter missing");
          }
          return caseService.getCases(null).stream()
              .filter(x -> caseIds.contains(x.getId()))
              .flatMap(kase -> kase.getTests().stream()
                  .flatMap(test -> test.getFullDepthSequencings().stream()
                      .map(sample -> new RowData(kase, sample))))
              .toList();
        }

      };

  public static final FullDepthSummary INSTANCE = new FullDepthSummary();

  private FullDepthSummary() {
    super("Full-Depth Summary", mainSection);
  }

}
