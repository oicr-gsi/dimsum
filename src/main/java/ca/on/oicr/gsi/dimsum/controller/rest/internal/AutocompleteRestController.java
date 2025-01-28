package ca.on.oicr.gsi.dimsum.controller.rest.internal;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.service.CaseService;

@RestController
@RequestMapping("/rest/internal/autocomplete")
public class AutocompleteRestController {

  @Autowired
  private CaseService caseService;

  @GetMapping("/run-names")
  public Set<String> queryRuns(@RequestParam String q) {
    return caseService.getMatchingRunNames(q);
  }
}
