package ca.on.oicr.gsi.dimsum.controller.rest.common;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.service.CaseService;

@RestController
@RequestMapping("/rest/common/autocomplete")
public class CommonAutocompleteRestController {

  // Note: CaseService filters all below results based on the user's authorized projects

  @Autowired
  private CaseService caseService;

  @GetMapping("/assay-names")
  public Set<String> queryAssays(@RequestParam String q) {
    return caseService.getMatchingAssayNames(q);
  }

  @GetMapping("/requisition-names")
  public Set<String> queryRequisitions(@RequestParam String q) {
    return caseService.getMatchingRequisitionNames(q);
  }

  @GetMapping("/project-names")
  public Set<String> queryProjects(@RequestParam String q) {
    return caseService.getMatchingProjectNames(q);
  }

  @GetMapping("/donor-names")
  public Set<String> queryDonors(@RequestParam String q) {
    return caseService.getMatchingDonorNames(q);
  }

  @GetMapping("/test-names")
  public Set<String> queryTests(@RequestParam String q) {
    return caseService.getMatchingTestNames(q);
  }
}
