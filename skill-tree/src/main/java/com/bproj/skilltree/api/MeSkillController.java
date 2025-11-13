package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.SkillRequest;
import com.bproj.skilltree.dto.SkillResponse;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.mapper.SkillMapper;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.model.SkillSortMode;
import com.bproj.skilltree.service.SkillService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import jakarta.json.JsonMergePatch;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
  private static final Logger logger = LoggerFactory.getLogger(MeSkillController.class);
  private final SkillService skillService;
  private final AuthUtils authUtils;

  public MeSkillController(SkillService skillService, AuthUtils authUtils) {
    this.skillService = skillService;
    this.authUtils = authUtils;
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
      @Valid @RequestBody SkillRequest skillRequest) {
    logger.info("POST /api/skills/me - create(skillRequest={})", skillRequest);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    Skill skill = SkillMapper.toSkill(skillRequest);
    SkillResponse skillResponse = SkillMapper.fromSkill(skillService.create(skill, userId));
    return ResponseEntity.created(URI.create("/api/skills/me/" + skillResponse.getId()))
        .body(skillResponse);
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
      @RequestParam(required = false, defaultValue = "NAME") SkillSortMode sortMode) {
    logger.info("GET /api/skills/me - mySkills(root={}, parentSkillId={}, sortMode={})", root,
        parentSkillId, sortMode);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId parentObjectId = null;
    if (parentSkillId != null) {
      parentObjectId = ObjectIdUtils.validateObjectId(parentSkillId, "parentSkillId");
    }
    if (parentSkillId != null && Boolean.TRUE.equals(root)) {
      throw new BadRequestException(
          "Query parameters 'parentSkillId' and 'root' cannot be used together.");
    }
    List<SkillResponse> skillResponses =
        skillService.findAndSortSkills(userId, parentObjectId, root, sortMode).stream()
            .map(SkillMapper::fromSkill).toList();
    return ResponseEntity.ok(skillResponses);
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
    logger.info("GET /api/skills/me/{} - mySkill(skillId={})", skillId, skillId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId skillObjectId = ObjectIdUtils.validateObjectId(skillId, "skillId");
    SkillResponse skillResponse =
        SkillMapper.fromSkill(skillService.findByUserIdAndId(userId, skillObjectId));
    return ResponseEntity.ok(skillResponse);
  }

  /**
   * Partially update a Skill for this User.
   *
   * @param auth JWT
   * @param skillId The Id of the Skill being updated
   * @param updates The updates to be applied to the Skill
   * @return The SkillResponse DTO of the updated Skill
   */
  @PatchMapping(path = "/{skillId}")
  public ResponseEntity<SkillResponse> patch(Authentication auth, @PathVariable String skillId,
      @RequestBody Map<String, Object> updates) {
    logger.info("PATCH /api/skills/me/{} - patch(skillId={}, updates={})", skillId, skillId,
        updates);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId skillObjectId = ObjectIdUtils.validateObjectId(skillId, "skillId");
    SkillResponse skillResponse =
        SkillMapper.fromSkill(skillService.patch(userId, skillObjectId, updates));
    return ResponseEntity.ok(skillResponse);
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
    logger.info("DELETE /api/skills/me/{} - deleteById(skillId={})", skillId, skillId);
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
    logger.info("DELETE /api/skills/me - deleteByUserId()");
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    skillService.deleteByUserId(userId);
    return ResponseEntity.noContent().build();
  }
}
