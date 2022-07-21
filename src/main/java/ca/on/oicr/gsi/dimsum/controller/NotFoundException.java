package ca.on.oicr.gsi.dimsum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception to throw when the client has requested a resource that doesn't exist, generally based
 * on requested URL. Will result in a HTTP 404 not found error
 */
public class NotFoundException extends ResponseStatusException {

  /**
   * Create a new BadRequestException indicating client error
   * 
   * @param reason explanation to return to the client
   */
  public NotFoundException(String reason) {
    super(HttpStatus.NOT_FOUND, reason);
  }

}
