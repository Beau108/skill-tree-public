package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.OrientationMovePatch;
import com.bproj.skilltree.service.OrientationService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import jakarta.json.JsonMergePatch;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Interactions for the authenticated User and their own orientations. Only supports patch because
 * an Orientation alone is useless and other orientation CRUD operations are done automatically with
 * the owning Trees.
 */
@RestController
@RequestMapping("/api/orientations/me")
public class MeOrientationController {
  private static final Logger logger = LoggerFactory.getLogger(MeOrientationController.class);
  private final OrientationService orientationService;
  private final AuthUtils authUtils;

  public MeOrientationController(OrientationService orientationService, AuthUtils authUtils) {
    this.orientationService = orientationService;
    this.authUtils = authUtils;
  }

  /**
   * Partially update an Orientation given a Tree's id and the updates to be applied.
   *
   * @param auth JWT
   * @param treeId The Id of the Tree the Orientation belongs to
   * @param updates The updates to be applied to the Orientation
   * @return No Content (TreeLayout already in client side)
   */
  @PatchMapping("/{treeId}")
  public ResponseEntity<Void> patch(Authentication auth, @PathVariable String treeId,
      @RequestBody List<OrientationMovePatch> updates) {
    logger.info("PATCH /api/orientations/me/{} - patch(treeId={}, updates={})", treeId, treeId,
        updates);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    orientationService.patch(userId, treeObjectId, updates);
    return ResponseEntity.noContent().build();
  }
}
