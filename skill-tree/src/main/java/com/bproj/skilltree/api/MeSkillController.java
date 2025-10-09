package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.SkillRequest;
import com.bproj.skilltree.dto.SkillResponse;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.mapper.SkillMapper;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.model.SkillSortMode;
import com.bproj.skilltree.service.SkillService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.net.URI;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles interactions from the client between an authenticated user and their skills.
 */
@RestController
@RequestMapping("/api/skills/me")
public class MeSkillController {
  private final SkillService skillService;
  private final AuthUtils authUtils;

  public MeSkillController(SkillService skillService, UserService userService) {
    this.skillService = skillService;
    this.authUtils = new AuthUtils(userService);
  }

  /**
   * Create a Skill for this user.
   *
   * @param auth JWT
   * @param skillRequest The Skill to be created
   * @return The created Skill as a SkillResponse DTO
   */
  @PostMapping
  public ResponseEntity<SkillResponse> create(Authentication auth,
      @RequestBody SkillRequest skillRequest) {
    if (skillRequest == null) {
      throw new BadRequestException("Request body cannot be null.");
    }
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    Skill skill = SkillMapper.toSkill(skillRequest);
    skill.setUserId(userId);
    SkillResponse createdSkill = skillService.createResponse(userId, skill);
    return ResponseEntity.created(URI.create("/api/skills/me/" + createdSkill.getId()))
        .body(createdSkill);
  }

  /**
   * Retrieve all of this user's skills (queryable).
   *
   * @param auth JWT
   * @param root Whether or returned Skills are root Skills (null parentSkillId)
   * @param parentSkillId The Id of the Skill returned Skills are sub Skills to
   * @param sortMode How the returned list will be ordered
   * @return The filtered, sorted list of SkillResponse DTOs
   */
  @GetMapping
  public ResponseEntity<List<SkillResponse>> mySkills(Authentication auth,
      @RequestParam(required = false) Boolean root,
      @RequestParam(required = false) String parentSkillId,
      @RequestParam(required = false) SkillSortMode sortMode) {

    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId parentObjectId = null;
    if (parentSkillId != null) {
      parentObjectId = ObjectIdUtils.validateObjectId(parentSkillId, "parentSkillId");
    }
    if (parentSkillId != null && Boolean.TRUE.equals(root)) {
      throw new BadRequestException(
          "Query parameters 'parentSkillId' and 'root' cannot be used together.");
    }
    List<SkillResponse> skills =
        skillService.responseQuery(userId, parentObjectId, root, sortMode);
    return new ResponseEntity<>(skills, HttpStatus.OK);
  }

  /**
   * Retrieve a single Skill.
   *
   * @param auth JWT
   * @param skillId The Id of the Skill being retrieved
   * @return The SkillResponse DTO for the Skill found
   */
  @GetMapping("/{skillId}")
  public ResponseEntity<SkillResponse> mySkill(Authentication auth, @PathVariable String skillId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId skillObjectId = ObjectIdUtils.validateObjectId(skillId, "skillId");
    return ResponseEntity.ok(skillService.getResponseByUserIdAndId(userId, skillObjectId));
  }

  /**
   * Update a Skill for this User.
   *
   * @param auth JWT
   * @param skillId The Id of the Skill being updated
   * @param updatedSkill The new version of the Skill
   * @return The SkillResponse DTO of the updated Skill
   */
  @PutMapping("/{skillId}")
  public ResponseEntity<SkillResponse> update(Authentication auth, @PathVariable String skillId,
      @RequestBody SkillRequest updatedSkill) {
    if (updatedSkill == null) {
      throw new BadRequestException("Request body cannot be null.");
    }
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId skillObjectId = ObjectIdUtils.validateObjectId(skillId, "skillId");
    Skill skill = SkillMapper.toSkill(updatedSkill);
    skill.setUserId(userId);
    SkillResponse result = skillService.updateResponse(userId, skillObjectId, skill);
    return ResponseEntity.ok(result);
  }

  /**
   * Partially update a Skill for this User.
   *
   * @param auth JWT
   * @param skillId The Id of the Skill being updated
   * @param updates The updates to be applied to the Skill
   * @return The SkillResponse DTO of the updated Skill
   */
  @PatchMapping(path = "/{skillId}", consumes = "application/merge-patch+json")
  public ResponseEntity<SkillResponse> patch(Authentication auth, @PathVariable String skillId,
      @RequestBody JsonMergePatch updates) {

    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId skillObjectId = ObjectIdUtils.validateObjectId(skillId, "skillId");
    SkillResponse updatedSkill = skillService.patchResponse(userId, skillObjectId, updates);
    return ResponseEntity.ok(updatedSkill);
  }

  /**
   * Delete a single Skill for this User.
   *
   * @param auth JWT
   * @param skillId The Id of the Skill being deleted
   * @return No Content
   */
  @DeleteMapping("/{skillId}")
  public ResponseEntity<Void> deleteById(Authentication auth, @PathVariable String skillId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId skillObjectId = ObjectIdUtils.validateObjectId(skillId, "skillId");
    skillService.deleteByUserIdAndId(userId, skillObjectId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Delete all of this User's Skills.
   *
   * @param auth JWT
   * @return No Content
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteByUserId(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    skillService.deleteByUserId(userId);
    return ResponseEntity.noContent().build();
  }
}
