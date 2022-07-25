package ca.on.oicr.gsi.dimsum.controller.mvc;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;

@Controller
@RequestMapping("/donors")
public class DonorController {

  @Autowired
  private CaseService caseService;

  @GetMapping("/{donorName}")
  public String getDonorDetailsPage(@PathVariable String donorName, ModelMap model) {
    List<Case> cases = caseService.getCases(new CaseFilter(CaseFilterKey.DONOR, donorName));
    if (cases.isEmpty()) {
      throw new NotFoundException(String.format("No data found for donor: %s", donorName));
    }

    model.put("title", String.format("%s Donor Details", donorName));
    model.put("detailType", CaseFilterKey.DONOR.name());
    model.put("detailValue", donorName);
    return "detail";
  }

}
