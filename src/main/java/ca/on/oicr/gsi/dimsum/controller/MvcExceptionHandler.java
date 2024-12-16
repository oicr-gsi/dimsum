package ca.on.oicr.gsi.dimsum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
        return prepareErrorModelAndView(ex.getStatusCode(), ex.getReason(), ex);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception ex) {
        return prepareErrorModelAndView(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex);
    }

    private ModelAndView prepareErrorModelAndView(HttpStatusCode status, String errorMessage,
            Exception ex) {
        String resolvedErrorMessage =
                (errorMessage != null) ? errorMessage : "No message available";
        if (status.is5xxServerError()) {
            logger.error("Error Status: {}. Error Message: {}", status, resolvedErrorMessage, ex);
        } else {
            logger.info("Error Status: {}. Error Message: {}", status, resolvedErrorMessage);
        }
        String reason = HttpStatus.resolve(status.value()).getReasonPhrase();
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorStatus", status.value());
        mav.addObject("errorReason", reason);
        mav.addObject("errorMessage", resolvedErrorMessage);
        mav.addObject("dataAgeMinutes", commonModelAttributeProvider.getDataAgeMinutes());
        mav.addObject("buildVersion", commonModelAttributeProvider.getBuildVersion());

        mav.addObject("title", status.value() + " " + reason + " - Dimsum");

        return mav;
    }
}
