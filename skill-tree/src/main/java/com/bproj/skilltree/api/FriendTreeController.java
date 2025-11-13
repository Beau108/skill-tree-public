package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.TreeLayout;
import com.bproj.skilltree.dto.TreeSummary;
import com.bproj.skilltree.exception.ForbiddenException;
import com.bproj.skilltree.mapper.TreeMapper;
import com.bproj.skilltree.service.FriendshipService;
import com.bproj.skilltree.service.TreeService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import java.util.List;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger logger = LoggerFactory.getLogger(FriendTreeController.class);
  private final TreeService treeService;
  private final FriendshipService friendService;
  private final AuthUtils authUtils;

  /**
   * Create a FriendTreeController.
   *
   * @param treeService Tree Service
   * @param friendService Tree Service
   * @param authUtils Authentication Utilities
   */
  public FriendTreeController(TreeService treeService, FriendshipService friendService,
      AuthUtils authUtils) {
    this.treeService = treeService;
    this.friendService = friendService;
    this.authUtils = authUtils;
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
    logger.info("GET /api/trees/friends/{} - getFriendTrees(friendId={})", friendId, friendId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId friendObjectId = ObjectIdUtils.validateObjectId(friendId, "friendId");
    if (!friendService.areFriends(userId, friendObjectId)) {
      throw new ForbiddenException("You are not friends with this user.");
    }
    List<TreeSummary> treeSummaries =
        treeService.findByUserId(friendObjectId).stream().map(TreeMapper::toTreeSummary).toList();
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
    logger.info("GET /api/trees/friends/{}/trees/{} - getFriendTreeLayout(friendId={}, treeId={})",
        friendId, treeId, friendId, treeId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId friendObjectId = ObjectIdUtils.validateObjectId(friendId, "friendId");
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    if (!friendService.areFriends(userId, friendObjectId)) {
      throw new ForbiddenException("You are not friends with this user.");
    }
    return ResponseEntity.ok(treeService.getLayoutByUserIdAndId(friendObjectId, treeObjectId));
  }

}
