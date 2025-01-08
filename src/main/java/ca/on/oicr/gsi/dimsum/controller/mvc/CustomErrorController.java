package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error");

        int statusCode = response.getStatus();
        String statusMessage = HttpStatus.valueOf(statusCode).getReasonPhrase();

        modelAndView.addObject("errorStatus", statusCode);
        modelAndView.addObject("errorReason", statusMessage);

        return modelAndView;
    }
}

