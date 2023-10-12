package ca.on.oicr.gsi.dimsum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import ca.on.oicr.gsi.dimsum.controller.mvc.CommonModelAttributeProvider;

@ControllerAdvice(basePackages = "ca.on.oicr.gsi.dimsum.controller.mvc")
public class MvcExceptionHandler {

    @Autowired
    private CommonModelAttributeProvider commonModelAttributeProvider;
    private static final Logger logger = LoggerFactory.getLogger(MvcExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleResponseStatusException(ResponseStatusException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorStatus", ex.getStatus().toString());
        mav.addObject("errorMessage", ex.getReason());

        mav.addObject("dataAgeMinutes", commonModelAttributeProvider.getDataAgeMinutes());
        mav.addObject("buildVersion", commonModelAttributeProvider.getBuildVersion());

        if (ex.getStatus().is5xxServerError()) {
            logger.error(ex.getReason(), ex);
        }

        logger.info("Error Status: " + ex.getStatus());
        logger.info("Error Message: " + ex.getReason());

        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception ex) {
        logger.error("Unexpected error", ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", "Unexpected error");
        return mav;
    }
}
