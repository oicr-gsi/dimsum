package ca.on.oicr.gsi.dimsum.controller.mvc;

import ca.on.oicr.gsi.dimsum.service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = {"ca.on.oicr.gsi.dimsum.controller.mvc"})
public class CommonModelAttributeProvider {

  @Autowired
  private CaseService caseService;

  @Value("${instancename}")
  private String instanceName;
  @Value("${build.version}")
  private String buildVersion;
  @Value("${bugreport.url}")
  private String bugReportUrl;

  @ModelAttribute("dataAgeMinutes")
  public long getDataAgeMinutes() {
    return caseService.getDataAge().toMinutes();
  }

  @ModelAttribute("instanceName")
  public String getInstanceName() {
    return instanceName;
  }

  @ModelAttribute("buildVersion")
  public String getBuildVersion() {
    return buildVersion;
  }

  @ModelAttribute("docsVersion")
  public String getDocsVersion() {
    if (buildVersion.endsWith("SNAPSHOT")) {
      return "latest";
    } else {
      return "v" + buildVersion;
    }
  }

  @ModelAttribute("bugReportUrl")
  public String getJiraLink() {
    return bugReportUrl;
  }

}
