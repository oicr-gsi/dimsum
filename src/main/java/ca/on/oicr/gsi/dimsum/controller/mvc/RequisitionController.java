package ca.on.oicr.gsi.dimsum.controller.mvc;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ca.on.oicr.gsi.dimsum.FrontEndConfig;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;

@Controller
@RequestMapping("/requisitions")
public class RequisitionController {

  @Autowired
  private CaseService caseService;
  @Autowired
  private FrontEndConfig frontEndConfig;

  @GetMapping("/{requisitionId}")
  public String getRequisitionDetailsPage(@PathVariable String requisitionId, ModelMap model) {
    if (!requisitionId.matches("^\\d+$")) {
      throw new BadRequestException(String.format("Invalid requisition ID: ", requisitionId));
    }
    List<Case> cases =
        caseService.getCases(new CaseFilter(CaseFilterKey.REQUISITION_ID, requisitionId));
    if (cases.isEmpty()) {
      throw new NotFoundException(
          String.format("No data found for requisition: %s", requisitionId));
    }
    Requisition requisition = cases.get(0).getRequisition();

    model.put("title", String.format("%s Requisition Details", requisition.getName()));
    model.put("titleLink", makeMisoRequisitionUrl(requisitionId));
    model.put("detailType", CaseFilterKey.REQUISITION_ID.name());
    model.put("detailValue", requisitionId);
    model.put("detailName", requisition.getName());
    return "detail";
  }

  private String makeMisoRequisitionUrl(String requisitionId) {
    return String.format("%s/miso/requisition/%s", frontEndConfig.getMisoUrl(), requisitionId);
  }

}
