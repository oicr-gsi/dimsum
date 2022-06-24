package ca.on.oicr.gsi.dimsum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception to throw when the client has submitted an invalid request, generally based on request
 * parameters, or request body. Will result in a HTTP 400 bad request error
 */
public class BadRequestException extends ResponseStatusException {

  /**
   * Create a new BadRequestException indicating client error
   * 
   * @param reason explanation to return to the client
   */
  public BadRequestException(String reason) {
    super(HttpStatus.BAD_REQUEST, reason);
  }

}
