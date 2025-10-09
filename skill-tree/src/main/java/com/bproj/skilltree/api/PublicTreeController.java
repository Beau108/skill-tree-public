package com.bproj.skilltree.api;

import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.ForbiddenException;
import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.model.Visibility;
import com.bproj.skilltree.service.TreeService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Responsible for all operations relating to trees with no associated User. These Trees are
 * 'presets', meaning Users are able to copy the Userless data into their own account.
 */
@RestController
@RequestMapping("/api/trees/public")
public class PublicTreeController {
  private final TreeService treeService;
  private final AuthUtils authUtils;
  
  public PublicTreeController(TreeService treeService, UserService userService) {
    this.treeService = treeService;
    this.authUtils = new AuthUtils(userService);
  }
  
  /**
   * Returns pageinated Preset Trees. 
   *
   * @param page    Which page the User is browsing
   * @param size    The size of each page
   * @param auth    The Authentication object
   * @return        The corresponding Page of Preset Trees
   */
  @GetMapping
  public ResponseEntity<Page<Tree>> getPresets(
      Authentication auth,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size) {
    
    if (!authUtils.isAuthenticated(auth)) {
      throw new ForbiddenException("Not authenticated.");
    }
    Page<Tree> trees = treeService.findPublicTrees(page, size);
    return ResponseEntity.ok(trees);
  }
  
  /**
   * Copies the specified Preset Tree to the User's account.
   *
   * @param auth    The web Authentication object attached to the end-user's request
   * @param treeId  The Id of the Preset Tree to be copied
   * @return    The copied Tree
   */
  @PostMapping("/{treeId}")
  public ResponseEntity<Tree> copyTree(
      Authentication auth,
      @PathVariable String treeId) {
    
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    Tree tree = treeService.getEntityById(treeObjectId);
    
    if (tree.getVisibility() != Visibility.PRESET) {
      throw new ForbiddenException("Wrong endpoint for copying user trees.");
    }
    return null;
  }
}
