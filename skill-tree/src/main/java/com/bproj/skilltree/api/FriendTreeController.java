package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.TreeLayout;
import com.bproj.skilltree.dto.TreeSummary;
import com.bproj.skilltree.exception.ForbiddenException;
import com.bproj.skilltree.service.FriendshipService;
import com.bproj.skilltree.service.TreeService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for users getting information on their friends' trees.
 */
@RestController
@RequestMapping("/api/trees/friends")
public class FriendTreeController {
  private final TreeService treeService;
  private final FriendshipService friendService;
  private final AuthUtils authUtils;

  /**
   * Make a FriendTreeController.
   *
   * @param treeService TreeService
   * @param friendService FriendService
   * @param userService UserService
   */
  public FriendTreeController(TreeService treeService, FriendshipService friendService,
      UserService userService) {
    this.treeService = treeService;
    this.friendService = friendService;
    this.authUtils = new AuthUtils(userService);
  }

  /**
   * Get all TreeSummaries for a specific friend.
   *
   * @param auth JWT
   * @param friendId Id of the Friend TreeSummaries are gathered for
   * @return The list of TreeSummaries
   */
  @GetMapping("/{friendId}")
  public ResponseEntity<List<TreeSummary>> getFriendTrees(Authentication auth,
      @PathVariable String friendId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId friendObjectId = ObjectIdUtils.validateObjectId(friendId, "friendId");
    if (!friendService.areFriends(userId, friendObjectId)) {
      throw new ForbiddenException("You are not friends with this user.");
    }
    List<TreeSummary> treeSummaries = treeService.getSummariesByUserId(friendObjectId);
    return ResponseEntity.ok(treeSummaries);
  }

  /**
   * Get a single TreeLayout belonging to a specific friend.
   *
   * @param auth JWT
   * @param friendId Id of the Friend 
   * @param treeId Id of the Tree
   * @return The TreeLayout 
   */
  @GetMapping("/{friendId}/trees/{treeId}")
  public ResponseEntity<TreeLayout> getFriendTreeLayout(Authentication auth,
      @PathVariable String friendId, @PathVariable String treeId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId friendObjectId = ObjectIdUtils.validateObjectId(friendId, "friendId");
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    if (!friendService.areFriends(userId, friendObjectId)) {
      throw new ForbiddenException("You are not friends with this user.");
    }
    return ResponseEntity.ok(treeService.getLayoutByUserIdAndId(friendObjectId, treeObjectId));
  }

}
