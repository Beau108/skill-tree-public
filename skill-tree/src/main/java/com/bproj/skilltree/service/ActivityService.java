package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.ActivityRepository;
import com.bproj.skilltree.dao.SkillRepository;
import com.bproj.skilltree.dao.UserRepository;
import com.bproj.skilltree.dto.ActivityFeedItem;
import com.bproj.skilltree.dto.ActivityResponse;
import com.bproj.skilltree.dto.RecentActivity;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.mapper.ActivityMapper;
import com.bproj.skilltree.model.Activity;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.model.SkillWeight;
import com.bproj.skilltree.model.User;
import com.bproj.skilltree.util.JsonMergePatchUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implements business logic for 'activities' collection.
 */
@Service
public class ActivityService {
  private final ActivityRepository activityRepository;
  private final UserRepository userRepository;
  private final SkillRepository skillRepository;

  private static final String NAME_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,50}$";
  private static final String DESC_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,500}$";

  /**
   * Create an ActivityService.
   *
   * @param activityRepository Activity DB operations
   * @param userRepository User DB operations
   * @param skillRepository Skill DB operations
   */
  @Autowired
  public ActivityService(
      @Qualifier("mongoActivityRepository") ActivityRepository activityRepository,
      @Qualifier("mongoUserRepository") UserRepository userRepository,
      @Qualifier("mongoSkillRepository") SkillRepository skillRepository) {
    this.activityRepository = activityRepository;
    this.userRepository = userRepository;
    this.skillRepository = skillRepository;
  }

  /**
   * Validate an Activity. userId must reference an existing user. name must be 3-50 characters.
   * description must be less than 500 characters. duration must be 0-12. skillWeights can't be
   * empty, each skill must reference a valid skill belonging to the user, weights must be > 0 and
   * sum to 1.
   *
   * @param activity The Activity to be validated.
   */
  private void validateActivity(Activity activity) {
    // userId
    ObjectId userId = activity.getUserId();
    if (!userRepository.existsById(userId)) {
      throw new BadRequestException(
          String.format("userId {%s} must reference an existing user.", userId.toString()));
    }

    // name
    Pattern ptName = Pattern.compile(NAME_REGEX);
    Matcher mtName = ptName.matcher(activity.getName());
    if (!mtName.matches()) {
      throw new BadRequestException(
          String.format("Name {%s} must be 1-50 characters long.", activity.getName()));
    }

    // desc
    Pattern ptDesc = Pattern.compile(DESC_REGEX);
    Matcher mtDesc = ptDesc.matcher(activity.getDescription());
    if (!mtDesc.matches()) {
      throw new BadRequestException(String.format("Description {%s} must be 1-500 characters long.",
          activity.getDescription()));
    }

    // duration
    double duration = activity.getDuration();
    if (duration < 0 || duration > 12) {
      throw new BadRequestException(
          String.format("Duration {%s} must be between 0 and 12 hours.", duration));
    }

    // weights
    List<SkillWeight> skillWeights = activity.getSkillWeights();
    if (skillWeights.size() < 1) {
      throw new BadRequestException("SkillWeights must have atleast one skill.");
    }

    double sumWeights = 0;
    for (SkillWeight sw : skillWeights) {
      double weight = sw.getWeight();
      if (weight < 0) {
        throw new BadRequestException(
            String.format("SkillWeight weight {%s} cannot be negative.", weight));
      } else if (weight > 1) {
        throw new BadRequestException(
            String.format("SkillWeight weight {%s} cannot be > 1.", weight));
      }
      sumWeights += weight;

      ObjectId skillId = sw.getSkillId();
      if (!skillRepository.existsByUserIdAndId(userId, skillId)) {
        throw new BadRequestException(String.format(
            "SkillWeight skillId {%s} must reference an existing skill owned by user {%s}.",
            skillId.toString(), userId.toString()));
      }
    }
    if (Math.abs(sumWeights - 1.0) > 0.05) {
      throw new BadRequestException(
          String.format("SkillWeight weights must sum to ~1. Current sum: {%s}", sumWeights));
    }
  }

  private List<Skill> getSkills(Activity activity) {
    return skillRepository
        .findByIdIn(activity.getSkillWeights().stream().map(sw -> sw.getSkillId()).toList());
  }

  /**
   * Maps Activities to ActivityResponses while avoiding N + 1 problem (batch Skills lookup).
   *
   * @param activities The List of Activities to be mapped to ActivityResponses
   * @return The List of ActivityResponses
   */
  private List<ActivityResponse> getResponses(List<Activity> activities) {
    if (activities.isEmpty()) {
      return List.of();
    }
    List<ObjectId> skillIds =
        activities.stream().flatMap(a -> a.getSkillWeights().stream().map(SkillWeight::getSkillId))
            .distinct().toList();
    Map<ObjectId, Skill> skillMap = skillRepository.findByIdIn(skillIds).stream()
        .collect(Collectors.toMap(Skill::getId, s -> s));
    return activities.stream().map(a -> {
      List<Skill> skills =
          a.getSkillWeights().stream().map(s -> skillMap.get(s.getSkillId())).toList();
      return ActivityMapper.fromActivity(a, skills);
    }).toList();
  }


  public Activity create(Activity activity) {
    validateActivity(activity);
    return activityRepository.insert(activity);
  }

  public ActivityResponse createResponse(Activity activity) {
    List<Skill> skills = getSkills(activity);
    return ActivityMapper.fromActivity(create(activity), skills);
  }

  public ActivityResponse createResponse(ObjectId userId, Activity activity) {
    activity.setUserId(userId);
    return createResponse(activity);
  }

  public boolean existsById(ObjectId activityId) {
    return activityRepository.existsById(activityId);
  }

  public boolean existsByUserIdAndId(ObjectId userId, ObjectId activityId) {
    return activityRepository.existsByUserIdAndId(userId, activityId);
  }

  /**
   * Finds an Activity by Id.
   *
   * @param activityId The Id of the desired Activity
   * @return The Activity. If not found, throws NFE.
   */
  public Activity getEntityById(ObjectId activityId) {
    return activityRepository.findById(activityId)
        .orElseThrow(() -> new NotFoundException(Map.of("activityId", activityId.toString())));
  }

  /**
   * Find and Activity and create/return an ActivityResponse.
   *
   * @param activityId The Id of the Activity
   * @return ActivityResponse
   */
  public ActivityResponse getResponseById(ObjectId activityId) {
    Activity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> new NotFoundException(Map.of("activityId", activityId.toString())));
    return ActivityMapper.fromActivity(activity, getSkills(activity));
  }

  public List<Activity> getEntitiesByUserId(ObjectId userId) {
    return activityRepository.findByUserId(userId);
  }

  /**
   * Finds Activities matching userId and optionally skillId.
   *
   * @param userId The Id of the User the Activity belongs to
   * @param skillId (optional) The skillId Activities must have to be pat of the return list.
   * @return A List of Activities satisfying all query parameters.
   */
  public List<Activity> getEntitiesByUserId(ObjectId userId, ObjectId skillId) {
    if (skillId != null) {
      return activityRepository.findByUserIdAndSkillWeightsSkillId(userId, skillId);
    }
    return activityRepository.findByUserId(userId);
  }

  /**
   * Finds Activities matching userId. Returns a list of ActivityResponses.
   *
   * @param userId The Id of the User
   * @return The List of ActivityResponses
   */
  public List<ActivityResponse> getResponsesByUserId(ObjectId userId) {
    List<Activity> activities = activityRepository.findByUserId(userId);
    return getResponses(activities);
  }

  /**
   * Finds Activities matching userId and containing a Skill with skillId.
   *
   * @param userId The Id of the User the Activities belong to
   * @param skillId (optional) The Id of the Skill returned Activities use
   * @return The List of ActivityResponses
   */
  public List<ActivityResponse> getResponsesByUserId(ObjectId userId, ObjectId skillId) {
    if (skillId == null) {
      return getResponsesByUserId(userId);
    }
    return getResponses(activityRepository.findByUserIdAndSkillWeightsSkillId(userId, skillId));
  }

  /**
   * Finds Activities matching userId and Id.
   *
   * @param userId The Id of the user the Activity belongs to
   * @param activityId The Id of the Activity
   * @return The matching Activity. Throws NFE otherwise.
   */
  public Activity getEntityByUserIdAndId(ObjectId userId, ObjectId activityId) {
    Optional<Activity> optionalActivity = activityRepository.findByUserIdAndId(userId, activityId);
    if (optionalActivity.isEmpty()) {
      throw new NotFoundException(
          Map.of("userId", userId.toString(), "activityId", activityId.toString()));
    }
    return optionalActivity.get();
  }

  /**
   * Finds the Activity matching userId and activityId.
   *
   * @param userId The Id of the User the Activity belongs to
   * @param activityId The Id of the Activity
   * @return The ActivityResponse of the Activity
   */
  public ActivityResponse getResponseByUserIdAndId(ObjectId userId, ObjectId activityId) {
    Activity activity = activityRepository.findByUserIdAndId(userId, activityId)
        .orElseThrow(() -> new NotFoundException(
            Map.of("userId", userId.toString(), "activityId", activityId.toString())));
    return ActivityMapper.fromActivity(activity, getSkills(activity));
  }

  private List<ActivityFeedItem> getFeedItems(List<Activity> activities) {
    List<ObjectId> userIds = activities.stream().map(Activity::getUserId).distinct().toList();
    Map<ObjectId, User> userMap =
        userRepository.findByIdIn(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
    List<ObjectId> skillIds =
        activities.stream().flatMap(a -> a.getSkillWeights().stream().map(SkillWeight::getSkillId))
        .distinct().toList();
    Map<ObjectId, Skill> skillMap = skillRepository.findByIdIn(skillIds).stream()
        .collect(Collectors.toMap(Skill::getId, s -> s));
    return activities.stream().map(a -> {
      List<Skill> skills =
          a.getSkillWeights().stream().map(s -> skillMap.get(s.getSkillId())).toList();
      return ActivityMapper.toActivityFeedItem(a, skills, userMap.get(a.getUserId()));
    }).toList();
  }
  
  /**
   * Get the list of feed items for users in userIds for the past 'days' days.
   *
   * @param userIds The Ids of users ActivityFeedItems are being created for
   * @param days The number of days from the present to search for Activities
   * @return The List of ActivityFeedItems
   */
  public List<ActivityFeedItem> getFeedItems(List<ObjectId> userIds, int days) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    Instant startInstant =
        LocalDate.now(ZoneOffset.UTC).minusDays(days - 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endInstant =
        LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    List<Activity> activities =
        activityRepository.findByUserIdInAndCreatedAtBetween(userIds, startInstant, endInstant);
    return getFeedItems(activities);
  }

  /**
   * Fully update an Activity.
   *
   * @param updatedActivity The new Activity to overwrite the previous one
   * @return The modified Activity. Throws NFE otherwise.
   */
  public Activity update(Activity updatedActivity) {
    if (existsById(updatedActivity.getId())) {
      validateActivity(updatedActivity);
      return activityRepository.save(updatedActivity);
    }
    throw new NotFoundException(Map.of("activityId", updatedActivity.getId().toString()));
  }

  /**
   * Fully update and Activity given its userId, Id, and new Activity.
   *
   * @param userId References the user the Activity belongs to
   * @param activityId Id of the Activity to be updated
   * @param newActivity The new Activity
   * @return The updated Activity
   */
  public Activity update(ObjectId userId, ObjectId activityId, Activity newActivity) {
    if (!activityRepository.existsByUserIdAndId(userId, activityId)) {
      throw new NotFoundException(
          Map.of("userId", userId.toString(), "activityId", activityId.toString()));
    }
    newActivity.setId(activityId);
    validateActivity(newActivity);
    newActivity.setUpdatedAt(Instant.now());
    return activityRepository.save(newActivity);
  }

  /**
   * Fully update an Activity given a userId, activityId, and updated Activity.
   *
   * @param userId The Id of the User the Activity belongs to
   * @param activityId The Id of the Activity to be updated
   * @param newActivity The new version of the Activity
   * @return The ActivityResponse of the updated Activity.
   */
  public ActivityResponse updateResponse(ObjectId userId, ObjectId activityId,
      Activity newActivity) {
    Activity updated = update(userId, activityId, newActivity);
    return ActivityMapper.fromActivity(updated, getSkills(newActivity));
  }

  /**
   * Partially updates an Activity.
   *
   * @param userId The User the Activity belongs to
   * @param activityId The Id of the Activity to be updated
   * @param updates The changes to be made to the Activity
   * @return The updated Activity. Throws NFE otherwise.
   */
  public Activity patch(ObjectId userId, ObjectId activityId, JsonMergePatch updates) {
    Activity activity = activityRepository.findByUserIdAndId(userId, activityId)
        .orElseThrow(() -> new NotFoundException(
            Map.of("userId", userId.toString(), "activityId", activityId.toString())));
    Activity updated = JsonMergePatchUtils.applyMergePatch(updates, activity, Activity.class);
    updated.setUserId(userId);
    updated.setId(activityId);
    updated.setCreatedAt(activity.getCreatedAt());
    updated.setUpdatedAt(Instant.now());
    validateActivity(updated);
    return activityRepository.save(updated);
  }

  public ActivityResponse patchResponse(ObjectId userId, ObjectId activityId,
      JsonMergePatch updates) {
    Activity patched = patch(userId, activityId, updates);
    return ActivityMapper.fromActivity(patched, getSkills(patched));
  }

  /**
   * Given a userId and a number of days, return the RecentActivity DTO for the User in those days.
   *
   * @param userId The Id of the User
   * @param days The number of days
   * @return The RecentActivityDTO
   */
  public RecentActivity getRecentActivityByUserId(ObjectId userId, int days) {
    Instant startInstant =
        LocalDate.now(ZoneOffset.UTC).minusDays(days).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endInstant =
        LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    List<Activity> activities =
        activityRepository.findByUserIdAndCreatedAtBetween(userId, startInstant, endInstant);
    return ActivityMapper.toRecentActivity(activities);
  }

  public void deleteById(ObjectId activityId) {
    activityRepository.deleteById(activityId);
  }

  public void deleteByUserId(ObjectId userId) {
    activityRepository.deleteByUserId(userId);
  }

  public void deleteByUserIdAndId(ObjectId userId, ObjectId activityId) {
    activityRepository.deleteByUserIdAndId(userId, activityId);
  }
}
