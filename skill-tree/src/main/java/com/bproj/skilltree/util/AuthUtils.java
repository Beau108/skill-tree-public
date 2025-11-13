package com.bproj.skilltree.util;

import com.bproj.skilltree.exception.ForbiddenException;
import com.bproj.skilltree.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


/**
 * This class is meant to perform operations on the Authentication component in incoming requests.
 */
@Component
public class AuthUtils {
  private final UserService userService;

  public AuthUtils(UserService userService) {
    this.userService = userService;
  }

  /**
   * Returns the associated userId from the JWT.
   *
   * @param auth The Authentication object attached to the incoming request.
   * @return The found userId. If no userId is found, an exception is thrown.
   */
  public ObjectId getUserIdByAuth(Authentication auth) {
    if (auth == null) {
      throw new ForbiddenException("Authentication not found.");
    }
    String firebaseId = auth.getPrincipal().toString();
    return userService.findByFirebaseId(firebaseId).getId();
  }

  /**
   * Check if the provided Authentication object contains a JWT that is associated with a firebase
   * account and thus User account.
   *
   * @param auth    The Authentication object
   * @return    Whether or not the Authentication indicates its User is authenticated
   */
  public boolean isAuthenticated(Authentication auth) {
    if (auth == null) {
      return false;
    }
    String firebaseId = auth.getPrincipal().toString();
    return userService.existsByFirebaseId(firebaseId);
  }
}
