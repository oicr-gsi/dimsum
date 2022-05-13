package ca.on.oicr.gsi.dimsum.controller.mvc;

import ca.on.oicr.gsi.dimsum.service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = {"ca.on.oicr.gsi.dimsum.controller.mvc"})
public class CommonModelAttributeProvider {

  @Autowired
  private CaseService caseService;

  @ModelAttribute("dataAgeMinutes")
  public long getDataAgeMinutes() {
    return caseService.getDataAge().toMinutes();
  }

}
