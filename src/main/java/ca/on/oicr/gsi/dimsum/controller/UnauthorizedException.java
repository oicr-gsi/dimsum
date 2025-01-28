package ca.on.oicr.gsi.dimsum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception to throw when the client has requested a resource without authorization to that type of
 * resource. This should not be based on specific resources, as the difference between
 * "unauthorized" and "not found" responses would then expose the existence or non-existence of
 * resources to which a user does not have authorization. Will result in a HTTP 401 unauthorized
 * error
 */
public class UnauthorizedException extends ResponseStatusException {

  /**
   * Create a new UnauthorizedException
   */
  public UnauthorizedException() {
    super(HttpStatus.UNAUTHORIZED);
  }

}
