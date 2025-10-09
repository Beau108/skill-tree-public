package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.FeedItem;
import com.bproj.skilltree.dto.UserRequest;
import com.bproj.skilltree.dto.UserResponse;
import com.bproj.skilltree.mapper.UserMapper;
import com.bproj.skilltree.model.User;
import com.bproj.skilltree.security.UserPrincipal;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Endpoints for the authenticated user's access to their associated User object.
 */
@RestController
@RequestMapping("/api/users/me")
public class MeUserController {
  private final UserService userService;
  private final AuthUtils authUtils;

  public MeUserController(UserService userService) {
    this.userService = userService;
    this.authUtils = new AuthUtils(userService);
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
      @RequestBody UserRequest userRequest) {
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
    User user = UserMapper.toUser(userRequest);
    user.setFirebaseId(principal.getFirebaseId());
    user.setEmail(principal.getEmail());
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(user));
  }

  /**
   * Return the authed UserResponse DTO.
   *
   * @param auth JWT
   * @return The authed user's DTO representation
   */
  @GetMapping
  public ResponseEntity<UserResponse> findMe(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    return ResponseEntity.ok(userService.getResponseById(userId));
  }
  
  @GetMapping("/feed")
  public ResponseEntity<Page<FeedItem>> myActivityFeed(Authentication auth, 
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    return ResponseEntity.ok(userService.myActivityFeed(userId, page, size));
  }

  /**
   * Update the authed user.
   *
   * @param auth JWT
   * @param updatedUser The updated User
   * @return The updated User
   */
  @PutMapping
  public ResponseEntity<UserResponse> update(Authentication auth,
      @RequestBody UserRequest updatedUser) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    return ResponseEntity.ok(userService.updateResponse(userId, UserMapper.toUser(updatedUser)));
  }

  /**
   * Partially updated the authed user.
   *
   * @param auth JWT
   * @param updates The updates to be applied to the User
   * @return The updated User
   */
  @PatchMapping(consumes = "application/merge-patch+json")
  public ResponseEntity<UserResponse> patch(Authentication auth,
      @RequestBody JsonMergePatch updates) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    User updatedUser = userService.patch(userId, updates);
    return ResponseEntity.ok(UserMapper.fromUser(updatedUser));
  }

  /**
   * Delete the authed user.
   *
   * @param auth JWT
   * @return 204 - NO_CONTENT
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteById(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    userService.deleteById(userId);
    return ResponseEntity.noContent().build();
  }
}
