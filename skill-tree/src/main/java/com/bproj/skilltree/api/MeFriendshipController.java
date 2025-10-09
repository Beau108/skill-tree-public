package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.FriendList;
import com.bproj.skilltree.model.FriendRequestStatus;
import com.bproj.skilltree.service.FriendshipService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import java.net.URI;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * End points for users to manage their Friendships.
 */
@RestController
@RequestMapping("/api/friendships/me")
public class MeFriendshipController {
  private final FriendshipService friendshipService;
  private final AuthUtils authUtils;

  public MeFriendshipController(FriendshipService friendService, UserService userService) {
    this.friendshipService = friendService;
    this.authUtils = new AuthUtils(userService);
  }

  @GetMapping
  public ResponseEntity<FriendList> myFriends(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    return ResponseEntity.ok(friendshipService.getFriendList(userId));
  }

  /**
   * Send a friend request given a display name.
   *
   * @param auth JWT
   * @param displayName The display name of the User being sent a friend request
   * @return No Content
   */
  @PostMapping("/{displayName}")
  public ResponseEntity<String> sendFriendRequest(Authentication auth,
      @PathVariable String displayName) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    friendshipService.addFriend(userId, displayName);
    return ResponseEntity.created(URI.create("/api/friendships/me/" + displayName))
        .body(displayName);
  }

  /**
   * Change the status of an existing friend request.
   *
   * @param auth JWT
   * @param friendshipId The Id of the Friendship that is being updated
   * @param status The new status of the Friendship
   * @return The new status of the Friendship
   */
  @PatchMapping("/{friendshipId}")
  public ResponseEntity<FriendRequestStatus> changeStatus(Authentication auth,
      @PathVariable String friendshipId, @RequestParam FriendRequestStatus status) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId friendshipObjectId = ObjectIdUtils.validateObjectId(friendshipId, "friendshipId");
    friendshipService.changeStatus(userId, friendshipObjectId, status);
    return ResponseEntity.ok(status);
  }

  /**
   * Delete a Friendship. Can represent ignoring a request or unfriending a User.
   *
   * @param auth JWT
   * @param friendshipId The Id of the Friendship to be deleted
   * @return No Content
   */
  @DeleteMapping("/{friendshipId}")
  public ResponseEntity<Void> removeFriend(Authentication auth, @PathVariable String friendshipId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId friendshipObjectId = ObjectIdUtils.validateObjectId(friendshipId, "friendshipId");
    friendshipService.deleteByUserIdAndId(userId, friendshipObjectId);
    return ResponseEntity.noContent().build();
  }
}
