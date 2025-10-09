package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.ActivityRequest;
import com.bproj.skilltree.dto.ActivityResponse;
import com.bproj.skilltree.dto.RecentActivity;
import com.bproj.skilltree.mapper.ActivityMapper;
import com.bproj.skilltree.model.Activity;
import com.bproj.skilltree.service.ActivityService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import com.bproj.skilltree.util.ObjectIdUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import jakarta.validation.Valid;
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
 * End-points for a User performing operations on their own Activities.
 */
@RestController
@RequestMapping("/api/activities/me")
public class MeActivityController {
  private final ActivityService activityService;
  private final AuthUtils authUtils;

  public MeActivityController(ActivityService activityService, UserService userService) {
    this.activityService = activityService;
    this.authUtils = new AuthUtils(userService);
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
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    Activity activity = ActivityMapper.toActivity(activityRequest);
    activity.setUserId(userId);
    ActivityResponse createdActivity = activityService.createResponse(userId, activity);
    return ResponseEntity.created(URI.create("/api/activities/me/" + createdActivity.getId()))
        .body(createdActivity);
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
  public ResponseEntity<List<ActivityResponse>> getMyActivities(Authentication auth,
      @RequestParam(required = false) String skillId) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId skillObjectId = null;
    if (skillId != null) {
      skillObjectId = ObjectIdUtils.validateObjectId(skillId, "skillId");
    }
    List<ActivityResponse> activities = activityService.getResponsesByUserId(userId, skillObjectId);
    return new ResponseEntity<>(activities, HttpStatus.OK);
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
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId activityObjectId = ObjectIdUtils.validateObjectId(activityId, "activityId");
    return ResponseEntity.ok(activityService.getResponseByUserIdAndId(userId, activityObjectId));
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
  public ResponseEntity<RecentActivity> getMyStreak(Authentication auth) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    int days = 30;
    return ResponseEntity.ok(activityService.getRecentActivityByUserId(userId, days));
  }

  /**
   * Update a given Activity with the request body by its Id.
   *
   * @param auth JWT
   * @param activityId The Id of the Activity being updated
   * @param updatedActivity The new version of the Activity
   * @return The ActivityResponse of the updated Activity
   */
  @PutMapping("/{activityId}")
  public ResponseEntity<ActivityResponse> update(Authentication auth,
      @PathVariable String activityId, @Valid @RequestBody ActivityRequest updatedActivity) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId activityObjectId = ObjectIdUtils.validateObjectId(activityId, "activityId");
    Activity activity = ActivityMapper.toActivity(updatedActivity);
    return ResponseEntity.ok(activityService.updateResponse(userId, activityObjectId, activity));
  }

  /**
   * Partially update an Activity with the request body by its Id.
   *
   * @param auth JWT
   * @param activityId The Id of the Activity to be updated
   * @param updates The updates to be applied to the Activity
   * @return The ActivityResponse of the updated Activity
   */
  @PatchMapping(path = "/{activityId}", consumes = "application/merge-patch+json")
  public ResponseEntity<ActivityResponse> patch(Authentication auth,
      @PathVariable String activityId, @RequestBody JsonMergePatch updates) {
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId activityObjectId = ObjectIdUtils.validateObjectId(activityId, "activityId");
    ActivityResponse updatedActivity =
        activityService.patchResponse(userId, activityObjectId, updates);
    return ResponseEntity.ok(updatedActivity);
  }

  /**
   * Delete all of this user's Activities.
   *
   * @param auth JWT
   * @return No Content
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteByUserId(Authentication auth) {
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
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    ObjectId activityObjectId = ObjectIdUtils.validateObjectId(activityId, "activityId");
    activityService.deleteByUserIdAndId(userId, activityObjectId);
    return ResponseEntity.noContent().build();
  }
}
