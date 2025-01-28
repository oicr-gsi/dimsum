package ca.on.oicr.gsi.dimsum.util.reporting.reports.shared;

import java.util.Collection;
import java.util.List;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.dimsum.service.CaseService;

public class CaseSampleRowData {
  private Case kase;
  private Sample sample;

  public CaseSampleRowData(Case kase, Sample sample) {
    this.kase = kase;
    this.sample = sample;
  }

  public Case getCase() {
    return kase;
  }

  public Sample getSample() {
    return sample;
  }

  public static List<CaseSampleRowData> listByCaseIds(CaseService caseService,
      Collection<String> caseIds) {
    return caseService.getAuthorizedCases(null).stream()
        .filter(x -> caseIds.contains(x.getId()))
        .flatMap(kase -> kase.getTests().stream()
            .flatMap(test -> test.getFullDepthSequencings().stream()
                .map(sample -> new CaseSampleRowData(kase, sample))))
        .toList();
  }
}
