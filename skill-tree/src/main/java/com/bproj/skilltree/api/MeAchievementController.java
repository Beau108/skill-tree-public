package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.AchievementRequest;
import com.bproj.skilltree.dto.AchievementResponse;
import com.bproj.skilltree.mapper.AchievementMapper;
import com.bproj.skilltree.model.Achievement;
import com.bproj.skilltree.model.AchievementSortMode;
import com.bproj.skilltree.service.AchievementService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.net.URI;
import java.util.List;
import org.bson.types.ObjectId;
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
 * -Responsible for all operations where an end-user retrieves their own Achievement data. -Each
 * method verifies the authenticated user, then calls the Service Layer for any required business
 * logic. -No responses other than OK or CREATED are return explicitly, exceptions are thrown
 * instead and are handled by the GlobalExceptionHandler class.
 */
@RestController
@RequestMapping("/api/achievements/me")
public class MeAchievementController {
  private final AchievementService achievementService;
  private final AuthUtils authUtils;

  /**
   * Creates a new MeAchievementController.
   *
   * @param achievementService Connection to Achievement Service layer
   * @param userService Connection to User Service layer, needed for AuthUtils
   */
  public MeAchievementController(AchievementService achievementService, UserService userService) {
    this.achievementService = achievementService;
    this.authUtils = new AuthUtils(userService);
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
      @RequestBody AchievementRequest achievementRequest) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    Achievement achievement = AchievementMapper.toAchievement(achievementRequest);
    AchievementResponse createdAchievement = achievementService.createResponse(userId, achievement);
    return ResponseEntity.created(URI.create("/api/achievements/me/" + createdAchievement.getId()))
        .body(createdAchievement);
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
      @RequestParam(required = false) AchievementSortMode sortMode) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId treeObjectId = null;
    if (treeId != null) {
      treeObjectId = ObjectIdUtils.validateObjectId(treeId, "treeId");
    }

    List<AchievementResponse> achievements =
        achievementService.queryResponses(userId, treeObjectId, next, sortMode);
    return ResponseEntity.ok(achievements);
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
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId achievementObjectId = ObjectIdUtils.validateObjectId(achievementId, "achievementId");
    AchievementResponse achievementResponse =
        achievementService.getResponseByUserIdAndId(userId, achievementObjectId);
    return ResponseEntity.ok(achievementResponse);
  }

  /**
   * Update an Achievement given its Id and the new version of the Achievement.
   *
   * @param auth JWT
   * @param updatedAchievement The new version of the Achievement
   * @return The AchievementResponse of the updated Achievement
   */
  @PutMapping("/{achievementId}")
  public ResponseEntity<AchievementResponse> update(Authentication auth,
      @PathVariable String achievementId, @RequestBody AchievementRequest updatedAchievement) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId achievementObjectId = ObjectIdUtils.validateObjectId(achievementId, "achievementId");
    Achievement achievement = AchievementMapper.toAchievement(updatedAchievement);
    achievement.setUserId(userId);
    achievement.setId(achievementObjectId);
    AchievementResponse result =
        achievementService.updateResponse(userId, achievementObjectId, achievement);
    return ResponseEntity.ok(result);
  }

  /**
   * Partially update an Achievement given its Id and updates do be applied.
   *
   * @param auth JWT
   * @param achievementId The Id of the Achievement
   * @param updates The updates to be applied
   * @return The updated Achievement's AchievementResponse
   */
  @PatchMapping(path = "/{achievementId}", consumes = "application/merge-patch+json")
  public ResponseEntity<AchievementResponse> patch(Authentication auth,
      @PathVariable String achievementId, @RequestBody JsonMergePatch updates) {

    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId achievementObjectId = ObjectIdUtils.validateObjectId(achievementId, "achievementId");
    AchievementResponse updatedAchievement =
        achievementService.patchResponse(userId, achievementObjectId, updates);
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
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    achievementService.deleteByUserId(userId);
    return ResponseEntity.noContent().build();
  }
}
