package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.FavoriteTree;
import com.bproj.skilltree.dto.MeTreeLayout;
import com.bproj.skilltree.dto.TreeRequest;
import com.bproj.skilltree.dto.TreeResponse;
import com.bproj.skilltree.dto.TreeStats;
import com.bproj.skilltree.mapper.TreeMapper;
import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.service.TreeService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import jakarta.validation.Valid;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for the authenticated user's access to their Trees.
 */
@RestController
@RequestMapping("/api/trees/me")
public class MeTreeController {
  private final TreeService treeService;
  private final AuthUtils authUtils;

  public MeTreeController(TreeService treeService, UserService userService) {
    this.treeService = treeService;
    this.authUtils = new AuthUtils(userService);
  }

  /**
   * Create a new Tree under the authed user's account. Also creates an Orientation for the new
   * Tree.
   *
   * @param auth JWT
   * @param treeRequest The tree information dto
   * @return The created tree dto
   */
  @PostMapping
  public ResponseEntity<TreeResponse> create(Authentication auth,
      @Valid @RequestBody TreeRequest treeRequest) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    Tree tree = TreeMapper.toTree(treeRequest);
    tree.setUserId(userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(treeService.createResponse(tree));
  }

  /**
   * Return all of the authed user's Trees.
   *
   * @param auth JWT
   * @return The List of the authed user's Trees.
   */
  @GetMapping
  public ResponseEntity<List<TreeResponse>> findMyTrees(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    List<TreeResponse> trees = treeService.getResponsesByUserId(userId);
    return ResponseEntity.ok(trees);
  }

  /**
   * Return one of the authed user's Trees.
   *
   * @param auth JWT
   * @param treeId The Id of the Tree to be returned
   * @return Tree DTO
   */
  @GetMapping("/{treeId}")
  public ResponseEntity<TreeResponse> findOneTree(Authentication auth,
      @PathVariable String treeId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    TreeResponse treeResponse = treeService.getResponseByUserIdAndId(userId, treeObjectId);
    return ResponseEntity.ok(treeResponse);
  }

  /**
   * Fully update a Tree belonging to the authed user.
   *
   * @param auth JWT
   * @param treeId Id of the Tree being updated
   * @param treeRequest The information of the updated Tree
   * @return The updated Tree DTO
   */
  @PutMapping("/{treeId}")
  public ResponseEntity<TreeResponse> update(Authentication auth, @PathVariable String treeId,
      @Valid @RequestBody TreeRequest treeRequest) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    Tree tree = TreeMapper.toTree(treeRequest);
    TreeResponse response = treeService.updateResponse(userId, treeObjectId, tree);
    return ResponseEntity.ok(response);
  }

  /**
   * Partially update a Tree belonging to the authed user.
   *
   * @param auth JWT
   * @param treeId Id of the Tree being updated
   * @param updates Updates to be applied to the Tree
   * @return The Tree DTO
   */
  @PatchMapping(path = "/{treeId}", consumes = "application/merge-patch+json")
  public ResponseEntity<TreeResponse> patch(Authentication auth, @PathVariable String treeId,
      @RequestBody JsonMergePatch updates) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    TreeResponse response = treeService.patchResponse(userId, treeObjectId, updates);
    return ResponseEntity.ok(response);
  }

  /**
   * Return a MeTreeLayout to the end user. Only the owning user can access a Tree's MeTreeLayout.
   *
   * @param auth JWT
   * @param treeId The Id of the Tree belonging to the authed User
   * @return The MeTreeLayout DTO
   */
  @GetMapping("/layout/{treeId}")
  public ResponseEntity<MeTreeLayout> loadTreeLayout(Authentication auth,
      @PathVariable String treeId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    return ResponseEntity.ok(treeService.getMeLayoutByUserIdAndId(userId, treeObjectId));
  }

  @GetMapping("/stats")
  public ResponseEntity<TreeStats> getAggregateStats(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    return ResponseEntity.ok(treeService.getStatsByUserId(userId));
  }

  /**
   * Get the statistics for a single tree belonging to the authed user.
   *
   * @param auth JWT
   * @param treeId The Id of the Tree
   * @return The TreeStats response for the given Tree
   */
  @GetMapping("/stats/{treeId}")
  public ResponseEntity<TreeStats> getSingleStats(Authentication auth,
      @PathVariable String treeId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    return ResponseEntity.ok(treeService.getStatsByUserIdAndId(userId, treeObjectId));
  }

  @GetMapping("/favorite")
  public ResponseEntity<FavoriteTree> getFavoriteTree(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    return ResponseEntity.ok(treeService.getFavoriteTree(userId));
  }

  /**
   * Delete a single Tree belonging to the authed user.
   *
   * @param auth JWT
   * @param treeId Id of the tree to be deleted
   * @return HttpStatus.NO_CONTENT
   */
  @DeleteMapping("/{treeId}")
  public ResponseEntity<Void> deleteOneTree(Authentication auth, @PathVariable String treeId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    treeService.deleteByUserIdAndId(userId, treeObjectId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Delete all of the authed user's trees.
   *
   * @param auth JWT
   * @return HttpStatus.NO_CONTENT
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteMyTrees(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    treeService.deleteByUserId(userId);
    return ResponseEntity.noContent().build();
  }
}
