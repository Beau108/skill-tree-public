package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.FeedItem;
import com.bproj.skilltree.dto.UserPatch;
import com.bproj.skilltree.dto.UserRequest;
import com.bproj.skilltree.dto.UserResponse;
import com.bproj.skilltree.mapper.UserMapper;
import com.bproj.skilltree.model.User;
import com.bproj.skilltree.security.UserPrincipal;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import jakarta.json.JsonMergePatch;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * Endpoints for the authenticated user's access to their associated User object.
 */
@RestController
@RequestMapping("/api/users/me")
public class MeUserController {
  private static final Logger logger = LoggerFactory.getLogger(MeUserController.class);
  private final UserService userService;
  private final AuthUtils authUtils;

  public MeUserController(UserService userService, AuthUtils authUtils) {
    this.userService = userService;
    this.authUtils = authUtils;
  }

  /**
   * Create a new User. Used by client when a user signs up.
   *
   * @param auth JWT
   * @param userRequest The details of the User to be created
   * @return The created User's DTO representation
   */
  @PostMapping
  public ResponseEntity<UserResponse> create(Authentication auth,
      @Valid @RequestBody UserRequest userRequest) {
    logger.info("POST /api/users/me - create(userRequest={})", userRequest);
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
    User user = UserMapper.toUser(userRequest);
    UserResponse userResponse = UserMapper
        .fromUser(userService.create(user, principal.getFirebaseId(), principal.getEmail()));
    return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
  }

  /**
   * Return the authed UserResponse DTO.
   *
   * @param auth JWT
   * @return The authed user's DTO representation
   */
  @GetMapping
  public ResponseEntity<UserResponse> getAuthenticatedUser(Authentication auth) {
    logger.info("GET /api/users/me - getAuthenticatedUser()");
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    UserResponse userResponse = UserMapper.fromUser(userService.findById(userId));
    return ResponseEntity.ok(userResponse);
  }

  @GetMapping("/feed")
  public ResponseEntity<Page<FeedItem>> getActivityFeed(Authentication auth,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
    logger.info("GET /api/users/me/feed - getActivityFeed(page={}, size={})", page, size);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    return ResponseEntity.ok(userService.getUserActionsFeed(userId, page, size));
  }

  /**
   * Partially updated the authed user.
   *
   * @param auth JWT
   * @param updates The updates to be applied to the User
   * @return The updated User
   */
  @PatchMapping
  public ResponseEntity<UserResponse> patch(Authentication auth,
      @RequestBody Map<String, Object> updates) {
    logger.info("PATCH /api/users/me - patch(updates={})", updates);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    UserResponse userResponse = UserMapper.fromUser(userService.patch(userId, updates));
    return ResponseEntity.ok(userResponse);
  }

  /**
   * Delete the authed user.
   *
   * @param auth JWT
   * @return 204 - NO_CONTENT
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteAuthenticatedUser(Authentication auth) {
    logger.info("DELETE /api/users/me - deleteAuthenticatedUser()");
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    userService.deleteById(userId);
    return ResponseEntity.noContent().build();
  }
}
