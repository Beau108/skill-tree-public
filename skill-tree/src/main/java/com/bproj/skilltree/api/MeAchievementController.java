package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.AchievementRequest;
import com.bproj.skilltree.dto.AchievementResponse;
import com.bproj.skilltree.mapper.AchievementMapper;
import com.bproj.skilltree.model.Achievement;
import com.bproj.skilltree.model.AchievementSortMode;
import com.bproj.skilltree.service.AchievementService;
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
 * -Responsible for all operations where an end-user retrieves their own Achievement data. -Each
 * method verifies the authenticated user, then calls the Service Layer for any required business
 * logic. -No responses other than OK or CREATED are return explicitly, exceptions are thrown
 * instead and are handled by the GlobalExceptionHandler class.
 */
@RestController
@RequestMapping("/api/achievements/me")
public class MeAchievementController {
  private static final Logger logger = LoggerFactory.getLogger(MeAchievementController.class);
  private final AchievementService achievementService;
  private final AuthUtils authUtils;

  public MeAchievementController(AchievementService achievementService, AuthUtils authUtils) {
    this.achievementService = achievementService;
    this.authUtils = authUtils;
  }

  /**
   * Creates a new achievement under the Authenticated user from the request body.
   *
   * @param auth The Authentication object for the user.
   * @param achievementRequest The body of the response and the Achievement to be created.
   * @return On success, status=201, body=createdAchievement.
   */
  @PostMapping
  public ResponseEntity<AchievementResponse> create(Authentication auth,
      @Valid @RequestBody AchievementRequest achievementRequest) {
    logger.info("POST /api/achievements/me - create(achievementRequest={})", achievementRequest);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    Achievement achievement = AchievementMapper.toAchievement(achievementRequest);
    AchievementResponse achievementResponse =
        AchievementMapper.fromAchievement(achievementService.create(achievement, userId));
    return ResponseEntity.created(URI.create("/api/achievements/me/" + achievementResponse.getId()))
        .body(achievementResponse);
  }

  /**
   * Queries the authenticated User's Achievements.
   *
   * @param auth JWT
   * @param treeId Achievements must belong to the Tree with matching Id
   * @param next Achievements must have all prerequisites complete and be incomplete themselves
   * @param sortMode How the resulting list will be sorted
   * @return The filtered, sorted list of the authed user's achievements
   */
  @GetMapping
  public ResponseEntity<List<AchievementResponse>> queryMyAchievements(Authentication auth,
      @RequestParam(required = false) String treeId, @RequestParam(required = false) Boolean next,
      @RequestParam(required = false, defaultValue = "TITLE") AchievementSortMode sortMode) {
    logger.info("GET /api/achievements/me - queryMyAchievements(treeId={}, next={}, sortMode={})",
        treeId, next, sortMode);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = null;
    if (treeId != null) {
      treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    }
    List<AchievementResponse> achievementResponses =
        achievementService.query(userId, treeObjectId, next, sortMode).stream()
            .map(AchievementMapper::fromAchievement).toList();
    return ResponseEntity.ok(achievementResponses);
  }

  /**
   * Get a single Achievement belonging to the authed User by its Id.
   *
   * @param auth JWT
   * @param achievementId The Id of the returned Achievement
   * @return The AchievementResponse DTO
   */
  @GetMapping("/{achievementId}")
  public ResponseEntity<AchievementResponse> getOne(Authentication auth,
      @PathVariable String achievementId) {
    logger.info("GET /api/achievements/me/{} - getOne(achievementId={})", achievementId,
        achievementId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId achievementObjectId = ObjectIdUtils.validateObjectId(achievementId, "achievementId");
    AchievementResponse achievementResponse = AchievementMapper
        .fromAchievement(achievementService.findByUserIdAndId(userId, achievementObjectId));
    return ResponseEntity.ok(achievementResponse);
  }

  /**
   * Partially update an Achievement given its Id and updates do be applied.
   *
   * @param auth JWT
   * @param achievementId The Id of the Achievement
   * @param updates The updates to be applied
   * @return The updated Achievement's AchievementResponse
   */
  @PatchMapping(path = "/{achievementId}")
  public ResponseEntity<AchievementResponse> patch(Authentication auth,
      @PathVariable String achievementId, @RequestBody Map<String, Object> updates) {
    logger.info("PATCH /api/achievements/me/{} - patch(achievementId={}, updates={})",
        achievementId, achievementId, updates);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId achievementObjectId = ObjectIdUtils.validateObjectId(achievementId, "achievementId");
    AchievementResponse updatedAchievement = AchievementMapper
        .fromAchievement(achievementService.patch(userId, achievementObjectId, updates));
    return ResponseEntity.ok(updatedAchievement);
  }

  /**
   * Delete an Achievement by its Id.
   *
   * @param auth JWT
   * @param achievementId The Id of the Achievement
   * @return No Content
   */
  @DeleteMapping("/{achievementId}")
  public ResponseEntity<Void> deleteByUserIdAndAchievementid(Authentication auth,
      @PathVariable String achievementId) {
    logger.info("DELETE /api/achievements/me/{} - deleteByUserIdAndAchievementid(achievementId={})",
        achievementId, achievementId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId achievementObjectId = ObjectIdUtils.validateObjectId(achievementId, "achievementId");
    if (achievementService.existsByUserIdAndId(userId, achievementObjectId)) {
      achievementService.deleteById(achievementObjectId);
    }
    return ResponseEntity.noContent().build();
  }

  /**
   * Delete all Achievements belonging to the authed User.
   *
   * @param auth JWT
   * @return No Content
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteByUserId(Authentication auth) {
    logger.info("DELETE /api/achievements/me - deleteByUserId()");
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    achievementService.deleteByUserId(userId);
    return ResponseEntity.noContent().build();
  }
}
