package ca.on.oicr.gsi.dimsum.controller.mvc;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error");

        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        String statusMessage = HttpStatus.valueOf(statusCode).getReasonPhrase();

        modelAndView.addObject("errorStatus", statusCode);
        modelAndView.addObject("errorReason", statusMessage);

        return modelAndView;
    }
}

