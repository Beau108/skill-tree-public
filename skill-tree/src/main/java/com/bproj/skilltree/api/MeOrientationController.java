package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.OrientationRequest;
import com.bproj.skilltree.mapper.OrientationMapper;
import com.bproj.skilltree.service.OrientationService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Interactions for the authenticated User and their own orientations. Only supports update/patch
 * because an Orientation alone is useless and other orientation CRUD operations are done
 * automatically with the owning Trees.
 */
@RestController
@RequestMapping("/api/orientations/me")
public class MeOrientationController {
  private final OrientationService orientationService;
  private final AuthUtils authUtils;

  public MeOrientationController(OrientationService orientationService, UserService userService) {
    this.orientationService = orientationService;
    this.authUtils = new AuthUtils(userService);
  }

  @PutMapping("/{treeId}")
  public ResponseEntity<Void> update(Authentication auth, @PathVariable String treeId,
      @Valid @RequestBody OrientationRequest orientationRequest) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    orientationService.update(userId, treeObjectId, OrientationMapper.toOrientation(orientationRequest));
    return ResponseEntity.noContent().build();
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
      JsonMergePatch updates) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    orientationService.patch(userId, treeObjectId, updates);
    return ResponseEntity.noContent().build();
  }
}
