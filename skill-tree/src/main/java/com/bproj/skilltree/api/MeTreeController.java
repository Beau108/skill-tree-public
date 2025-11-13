package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.FavoriteTree;
import com.bproj.skilltree.dto.MeTreeLayout;
import com.bproj.skilltree.dto.TreeRequest;
import com.bproj.skilltree.dto.TreeResponse;
import com.bproj.skilltree.dto.TreeStats;
import com.bproj.skilltree.mapper.TreeMapper;
import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.service.TreeService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import jakarta.json.JsonMergePatch;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for the authenticated user's access to their Trees.
 */
@RestController
@RequestMapping("/api/trees/me")
public class MeTreeController {
  private static final Logger logger = LoggerFactory.getLogger(MeTreeController.class);
  private final TreeService treeService;
  private final AuthUtils authUtils;

  public MeTreeController(TreeService treeService, AuthUtils authUtils) {
    this.treeService = treeService;
    this.authUtils = authUtils;
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
    logger.info("POST /api/trees/me - create(treeRequest={})", treeRequest);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    Tree tree = TreeMapper.toTree(treeRequest);
    TreeResponse treeResponse = TreeMapper.fromTree(treeService.create(tree, userId));
    logger.error("Returning TreeResponse with id={}", treeResponse.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(treeResponse);
  }

  /**
   * Return all of the authed user's Trees.
   *
   * @param auth JWT
   * @return The List of the authed user's Trees.
   */
  @GetMapping
  public ResponseEntity<List<TreeResponse>> getCurrentUserTrees(Authentication auth) {
    logger.info("GET /api/trees/me - getCurrentUserTrees()");
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    List<TreeResponse> treeResponses =
        treeService.findByUserId(userId).stream().map(TreeMapper::fromTree).toList();
    return ResponseEntity.ok(treeResponses);
  }

  /**
   * Return one of the authed user's Trees.
   *
   * @param auth JWT
   * @param treeId The Id of the Tree to be returned
   * @return Tree DTO
   */
  @GetMapping("/{treeId}")
  public ResponseEntity<TreeResponse> getTreeById(Authentication auth,
      @PathVariable String treeId) {
    logger.info("GET /api/trees/me/{} - getTreeById(treeId={})", treeId, treeId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    TreeResponse treeResponse =
        TreeMapper.fromTree(treeService.findByUserIdAndId(userId, treeObjectId));
    logger.info(treeResponse.getId());
    return ResponseEntity.ok(treeResponse);
  }

  /**
   * Partially update a Tree belonging to the authed user.
   *
   * @param auth JWT
   * @param treeId Id of the Tree being updated
   * @param updates Updates to be applied to the Tree
   * @return The Tree DTO
   */
  @PatchMapping(path = "/{treeId}")
  public ResponseEntity<TreeResponse> patch(Authentication auth, @PathVariable String treeId,
      @RequestBody Map<String, Object> updates) {
    logger.info("PATCH /api/trees/me/{} - patch(treeId={}, updates={})", treeId, treeId, updates);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    TreeResponse treeResponse =
        TreeMapper.fromTree(treeService.patch(userId, treeObjectId, updates));
    return ResponseEntity.ok(treeResponse);
  }

  /**
   * Return a MeTreeLayout to the end user. Only the owning user can access a Tree's MeTreeLayout.
   *
   * @param auth JWT
   * @param treeId The Id of the Tree belonging to the authed User
   * @return The MeTreeLayout DTO
   */
  @GetMapping("/layout/{treeId}")
  public ResponseEntity<MeTreeLayout> getTreeLayout(Authentication auth,
      @PathVariable String treeId) {
    logger.info("GET /api/trees/me/layout/{} - getTreeLayout(treeId={})", treeId, treeId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    return ResponseEntity.ok(treeService.getMeLayoutByUserIdAndId(userId, treeObjectId));
  }

  @GetMapping("/stats")
  public ResponseEntity<TreeStats> getAggregatedTreeStats(Authentication auth) {
    logger.info("GET /api/trees/me/stats - getAggregatedTreeStats()");
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
  public ResponseEntity<TreeStats> getTreeStats(Authentication auth, @PathVariable String treeId) {
    logger.info("GET /api/trees/me/stats/{} - getTreeStats(treeId={})", treeId, treeId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    return ResponseEntity.ok(treeService.getStatsByUserIdAndId(userId, treeObjectId));
  }

  @GetMapping("/favorite")
  public ResponseEntity<FavoriteTree> getFavoriteTree(Authentication auth) {
    logger.info("GET /api/trees/me/favorite - getFavoriteTree()");
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
  public ResponseEntity<Void> deleteTreeById(Authentication auth, @PathVariable String treeId) {
    logger.info("DELETE /api/trees/me/{} - deleteTreeById(treeId={})", treeId, treeId);
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
  public ResponseEntity<Void> deleteAllUserTrees(Authentication auth) {
    logger.info("DELETE /api/trees/me - deleteAllUserTrees()");
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    treeService.deleteByUserId(userId);
    return ResponseEntity.noContent().build();
  }
}
