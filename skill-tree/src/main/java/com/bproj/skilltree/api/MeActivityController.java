package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.ActivityRequest;
import com.bproj.skilltree.dto.ActivityResponse;
import com.bproj.skilltree.dto.RecentActivity;
import com.bproj.skilltree.mapper.ActivityMapper;
import com.bproj.skilltree.model.Activity;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.service.ActivityService;
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
 * End-points for a User performing operations on their own Activities.
 */
@RestController
@RequestMapping("/api/activities/me")
public class MeActivityController {
  private static final Logger logger = LoggerFactory.getLogger(MeActivityController.class);
  private final ActivityService activityService;
  private final AuthUtils authUtils;

  public MeActivityController(ActivityService activityService, AuthUtils authUtils) {
    this.activityService = activityService;
    this.authUtils = authUtils;
  }

  /**
   * Create an Activity from an ActivityRequest.
   *
   * @param auth JWT
   * @param activityRequest The request body containing minimum information for creating a new
   *        Activity.
   * @return The new Activity as an ActivityResponse
   */
  @PostMapping
  public ResponseEntity<ActivityResponse> create(Authentication auth,
      @Valid @RequestBody ActivityRequest activityRequest) {
    logger.info("POST /api/activities/me - create(activityRequest={})", activityRequest);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    Activity activity = ActivityMapper.toActivity(activityRequest);
    activity.setUserId(userId);
    Activity createdActivity = activityService.create(activity, userId);
    List<Skill> activitySkills = activityService.getSkillsForActivity(createdActivity);
    ActivityResponse activityResponse =
        ActivityMapper.fromActivity(createdActivity, activitySkills);
    return ResponseEntity.created(URI.create("/api/activities/me/" + activityResponse.getId()))
        .body(activityResponse);
  }

  /**
   * Return all of the authenticated user's Activities. If skillId is provided, returned Activities
   * must have the associated skill listed in 'skillWeights'.
   *
   * @param auth JWT
   * @param skillId The Id of the Skill Activities must have
   * @return The List of ActivityResponses
   */
  @GetMapping
  public ResponseEntity<List<ActivityResponse>> getCurrentUserActivities(Authentication auth,
      @RequestParam(required = false) String skillId) {
    logger.info("GET /api/activities/me - getCurrentUserActivities(skillId={})", skillId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId skillObjectId = null;
    if (skillId != null) {
      skillObjectId = ObjectIdUtils.validateObjectId(skillId, "skillId");
    }
    List<ActivityResponse> activitieResponses = activityService
        .mapActivitiesToResponses(activityService.findByUserId(userId, skillObjectId));
    return ResponseEntity.ok(activitieResponses);
  }

  /**
   * Return a single Activity belonging to the authed User given an Id.
   *
   * @param auth JWT
   * @param activityId The Id of the Activity
   * @return The ActivityResponse of the Activity
   */
  @GetMapping("/{activityId}")
  public ResponseEntity<ActivityResponse> findByUserIdAndId(Authentication auth,
      @PathVariable String activityId) {
    logger.info("GET /api/activities/me/{} - findByUserIdAndId(activityId={})", activityId,
        activityId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId activityObjectId = ObjectIdUtils.validateObjectId(activityId, "activityId");
    Activity activity = activityService.findByUserIdAndId(userId, activityObjectId);
    List<Skill> activitySkills = activityService.getSkillsForActivity(activity);
    ActivityResponse activityResponse = ActivityMapper.fromActivity(activity, activitySkills);
    return ResponseEntity.ok(activityResponse);
  }

  /**
   * Compute the User's 'streak' and daily activity over the last 30 days. 30 days is hard coded, so
   * streaks don't go beyond 30 - too worried about excessive Activity lookup just for a streak
   * count especially because this end-point is hit from the user's dash board.
   *
   * @param auth JWT
   * @return The RecentActivity DTO for this User.
   */
  @GetMapping("/streak")
  public ResponseEntity<RecentActivity> getActivityStreak(Authentication auth) {
    logger.info("GET /api/activities/me/streak - getActivityStreak()");
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    int days = 30;
    return ResponseEntity.ok(activityService.getRecentActivityByUserId(userId, days));
  }

  /**
   * Partially update an Activity with the request body by its Id.
   *
   * @param auth JWT
   * @param activityId The Id of the Activity to be updated
   * @param updates The updates to be applied to the Activity
   * @return The ActivityResponse of the updated Activity
   */
  @PatchMapping(path = "/{activityId}")
  public ResponseEntity<ActivityResponse> patch(Authentication auth,
      @PathVariable String activityId, @RequestBody Map<String, Object> updates) {
    logger.info("PATCH /api/activities/me/{} - patch(activityId={}, updates={})", activityId,
        activityId, updates);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId activityObjectId = ObjectIdUtils.validateObjectId(activityId, "activityId");
    Activity updatedActivity = activityService.patch(userId, activityObjectId, updates);
    List<Skill> activitySkills = activityService.getSkillsForActivity(updatedActivity);
    ActivityResponse activityResponse =
        ActivityMapper.fromActivity(updatedActivity, activitySkills);
    return ResponseEntity.ok(activityResponse);
  }

  /**
   * Delete all of this user's Activities.
   *
   * @param auth JWT
   * @return No Content
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteByUserId(Authentication auth) {
    logger.info("DELETE /api/activities/me - deleteByUserId()");
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    activityService.deleteByUserId(userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Deleta single one of this User's Activities.
   *
   * @param auth JWT
   * @param activityId The Id of the Activity to be deleted
   * @return No Content
   */
  @DeleteMapping("/{activityId}")
  public ResponseEntity<Void> delete(Authentication auth, @PathVariable String activityId) {
    logger.info("DELETE /api/activities/me/{} - delete(activityId={})", activityId, activityId);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId activityObjectId = ObjectIdUtils.validateObjectId(activityId, "activityId");
    activityService.deleteByUserIdAndId(userId, activityObjectId);
    return ResponseEntity.noContent().build();
  }
}
