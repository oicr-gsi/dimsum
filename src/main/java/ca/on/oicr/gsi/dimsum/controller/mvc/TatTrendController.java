package ca.on.oicr.gsi.dimsum.controller.mvc;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TatTrendController {

  @GetMapping("/tat-trend")
  public ModelAndView tatTrend(@RequestParam Map<String, String> filters) {
    ModelAndView modelAndView = new ModelAndView("tat-trend");
    modelAndView.addObject("filters", filters);
    return modelAndView;
  }
}
