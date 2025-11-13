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
import com.bproj.skilltree.util.PatchUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements business logic for 'activities' collection.
 */
@Service
public class ActivityService {
  private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);
  private final ActivityRepository activityRepository;
  private final UserRepository userRepository;
  private final SkillRepository skillRepository;
  private final SkillService skillService;


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
      @Qualifier("mongoSkillRepository") SkillRepository skillRepository,
      SkillService skillService) {
    this.activityRepository = activityRepository;
    this.userRepository = userRepository;
    this.skillRepository = skillRepository;
    this.skillService = skillService;
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
      throw new BadRequestException("Activity must reference an existing user.");
    }

    // duration
    double duration = activity.getDuration();
    if (duration < 0 || duration > 12) {
      throw new BadRequestException("Duration must be between 0 and 12 hours.");
    }

    // weights
    List<SkillWeight> skillWeights = activity.getSkillWeights();
    if (skillWeights.size() < 1) {
      throw new BadRequestException("Activity must have at least one skill.");
    }

    double sumWeights = 0;
    for (SkillWeight sw : skillWeights) {
      double weight = sw.getWeight();
      if (weight < 0) {
        throw new BadRequestException("Skill weight cannot be negative.");
      } else if (weight > 1) {
        throw new BadRequestException("Skill weight cannot be greater than 1.");
      }
      sumWeights += weight;

      ObjectId skillId = sw.getSkillId();
      if (!skillRepository.existsByUserIdAndId(userId, skillId)) {
        throw new BadRequestException(
            "Skill weight must reference an existing skill owned by the user.");
      }
    }
    if (Math.abs(sumWeights - 1.0) > 0.05) {
      throw new BadRequestException("Skill weights must sum to approximately 1.");
    }
  }

  public List<Skill> getSkillsForActivity(Activity activity) {
    logger.info("getSkillsForActivity(activity={})", activity);
    logger.info("skillRepository.findByIdIn(skillIds={})", activity.getSkillWeights().stream().map(sw -> sw.getSkillId()).toList());
    return skillRepository
        .findByIdIn(activity.getSkillWeights().stream().map(sw -> sw.getSkillId()).toList());
  }

  /**
   * Maps Activities to ActivityResponses while avoiding N + 1 problem (batch Skills lookup).
   *
   * @param activities The List of Activities to be mapped to ActivityResponses
   * @return The List of ActivityResponses
   */
  public List<ActivityResponse> mapActivitiesToResponses(List<Activity> activities) {
    logger.info("mapActivitiesToResponses(activities={})", activities);
    if (activities.isEmpty()) {
      return List.of();
    }
    List<ObjectId> skillIds =
        activities.stream().flatMap(a -> a.getSkillWeights().stream().map(SkillWeight::getSkillId))
            .distinct().toList();
    logger.info("skillRepository.findByIdIn(skillIds={})", skillIds);
    Map<ObjectId, Skill> skillMap = skillRepository.findByIdIn(skillIds).stream()
        .collect(Collectors.toMap(Skill::getId, s -> s));
    return activities.stream().map(a -> {
      List<Skill> skills =
          a.getSkillWeights().stream().map(s -> skillMap.get(s.getSkillId())).toList();
      return ActivityMapper.fromActivity(a, skills);
    }).toList();
  }


  /**
   * Create a new Activity. Add weight * duration hours to each referenced Skill.
   *
   * @param activity The Activity to be created
   * @param userId The Id of the User the Activity belongs to
   * @return The created Activity
   */
  @Transactional
  public Activity create(Activity activity, ObjectId userId) {
    logger.info("create(activity={}, userId={})", activity, userId);
    activity.setUserId(userId);
    validateActivity(activity);
    logger.info("activityRepository.insert(activity={})", activity);
    Activity createdActivity = activityRepository.insert(activity);
    double duration = createdActivity.getDuration();
    createdActivity.getSkillWeights().forEach(sw -> {
      skillService.addHours(sw.getSkillId(), duration * sw.getWeight());
    });
    return createdActivity;
  }

  public boolean existsById(ObjectId activityId) {
    logger.info("existsById(activityId={})", activityId);
    logger.info("activityRepository.existsById(activityId={})", activityId);
    return activityRepository.existsById(activityId);
  }

  public boolean existsByUserIdAndId(ObjectId userId, ObjectId activityId) {
    logger.info("existsByUserIdAndId(userId={}, activityId={})", userId, activityId);
    logger.info("activityRepository.existsByUserIdAndId(userId={}, activityId={})", userId, activityId);
    return activityRepository.existsByUserIdAndId(userId, activityId);
  }

  /**
   * Finds an Activity by Id.
   *
   * @param activityId The Id of the desired Activity
   * @return The Activity. If not found, throws NFE.
   */
  public Activity findById(ObjectId activityId) {
    logger.info("findById(activityId={})", activityId);
    logger.info("activityRepository.findById(activityId={})", activityId);
    return activityRepository.findById(activityId).orElseThrow(
        () -> new NotFoundException("activities", Map.of("activityId", activityId.toString())));
  }

  public List<Activity> findByUserId(ObjectId userId) {
    logger.info("findByUserId(userId={})", userId);
    logger.info("activityRepository.findByUserId(userId={})", userId);
    return activityRepository.findByUserId(userId);
  }

  /**
   * Finds Activities matching userId and optionally skillId.
   *
   * @param userId The Id of the User the Activity belongs to
   * @param skillId (optional) The skillId Activities must have to be pat of the return list.
   * @return A List of Activities satisfying all query parameters.
   */
  public List<Activity> findByUserId(ObjectId userId, ObjectId skillId) {
    logger.info("findByUserId(userId={}, skillId={})", userId, skillId);
    if (skillId != null) {
      logger.info("activityRepository.findByUserIdAndSkillWeightsSkillId(userId={}, skillId={})", userId, skillId);
      return activityRepository.findByUserIdAndSkillWeightsSkillId(userId, skillId);
    }
    logger.info("activityRepository.findByUserId(userId={})", userId);
    return activityRepository.findByUserId(userId);
  }

  /**
   * Finds Activities matching userId and Id.
   *
   * @param userId The Id of the user the Activity belongs to
   * @param activityId The Id of the Activity
   * @return The matching Activity. Throws NFE otherwise.
   */
  public Activity findByUserIdAndId(ObjectId userId, ObjectId activityId) {
    logger.info("findByUserIdAndId(userId={}, activityId={})", userId, activityId);
    logger.info("activityRepository.findByUserIdAndId(userId={}, activityId={})", userId, activityId);
    return activityRepository.findByUserIdAndId(userId, activityId)
        .orElseThrow(() -> new NotFoundException("activities",
            Map.of("userId", userId.toString(), "activityId", activityId.toString())));
  }

  private List<ActivityFeedItem> mapActivitiesToFeedItems(List<Activity> activities) {
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
  public List<ActivityFeedItem> getActivityFeedItemsByUserIds(List<ObjectId> userIds, int days) {
    logger.info("getActivityFeedItemsByUserIds(userIds={}, days={})", userIds, days);
    if (userIds.isEmpty()) {
      return List.of();
    }
    Instant startInstant =
        LocalDate.now(ZoneOffset.UTC).minusDays(days - 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endInstant =
        LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    logger.info("activityRepository.findByUserIdInAndCreatedAtBetween(userIds={}, startInstant={}, endInstant={})", userIds, startInstant, endInstant);
    List<Activity> activities =
        activityRepository.findByUserIdInAndCreatedAtBetween(userIds, startInstant, endInstant);
    return mapActivitiesToFeedItems(activities);
  }

  /**
   * Fully update and Activity given its userId, Id, and new Activity.
   *
   * @param userId References the user the Activity belongs to
   * @param activityId Id of the Activity to be updated
   * @param updatedActivity The new Activity
   * @return The updated Activity
   */
  @Transactional
  public Activity update(ObjectId userId, ObjectId activityId, Activity updatedActivity) {
    logger.info("update(userId={}, activityId={}, updatedActivity={})", userId, activityId, updatedActivity);
    Activity existingActivity = findByUserIdAndId(userId, activityId);

    updatedActivity.setId(existingActivity.getId());
    updatedActivity.setUserId(userId);

    validateActivity(updatedActivity);

    Map<ObjectId, Double> skillTimeDiffs = new HashMap<>();

    double oldDuration = existingActivity.getDuration();
    for (SkillWeight sw : existingActivity.getSkillWeights()) {
      skillTimeDiffs.put(sw.getSkillId(), oldDuration * sw.getWeight() * -1);
    }

    double newDuration = updatedActivity.getDuration();
    for (SkillWeight sw : updatedActivity.getSkillWeights()) {
      if (skillTimeDiffs.containsKey(sw.getSkillId())) {
        double temp = skillTimeDiffs.get(sw.getSkillId());
        skillTimeDiffs.put(sw.getSkillId(), temp + sw.getWeight() * newDuration);
      } else {
        skillTimeDiffs.put(sw.getSkillId(), sw.getWeight() * newDuration);
      }
    }

    skillTimeDiffs.entrySet().forEach(e -> {
      ObjectId skillId = e.getKey();
      double value = e.getValue();
      skillService.addHours(skillId, value);
    });

    logger.info("activityRepository.save(updatedActivity={})", updatedActivity);
    return activityRepository.save(updatedActivity);
  }

  /**
   * Partially updates an Activity.
   *
   * @param userId The User the Activity belongs to
   * @param activityId The Id of the Activity to be updated
   * @param updates The changes to be made to the Activity
   * @return The updated Activity. Throws NFE otherwise.
   */
  @Transactional
  public Activity patch(ObjectId userId, ObjectId activityId, Map<String, Object> updates) {
    logger.info("patch(userId={}, activityId={}, updates={})", userId, activityId, updates);
    logger.info("activityRepository.findByUserIdAndId(userId={}, activityId={})", userId, activityId);
    Activity existingActivity = activityRepository.findByUserIdAndId(userId, activityId)
        .orElseThrow(() -> new NotFoundException("activities",
            Map.of("userId", userId.toString(), "activityId", activityId.toString())));

    Activity updatedActivity = PatchUtils.applyActivityPatch(existingActivity, updates);

    updatedActivity.setUserId(existingActivity.getUserId());
    updatedActivity.setId(existingActivity.getId());

    validateActivity(updatedActivity);

    Map<ObjectId, Double> skillTimeDiffs = new HashMap<>();

    double oldDuration = existingActivity.getDuration();
    for (SkillWeight sw : existingActivity.getSkillWeights()) {
      skillTimeDiffs.put(sw.getSkillId(), oldDuration * sw.getWeight() * -1);
    }

    double newDuration = updatedActivity.getDuration();
    for (SkillWeight sw : updatedActivity.getSkillWeights()) {
      if (skillTimeDiffs.containsKey(sw.getSkillId())) {
        double temp = skillTimeDiffs.get(sw.getSkillId());
        skillTimeDiffs.put(sw.getSkillId(), temp + sw.getWeight() * newDuration);
      } else {
        skillTimeDiffs.put(sw.getSkillId(), sw.getWeight() * newDuration);
      }
    }

    skillTimeDiffs.entrySet().forEach(e -> {
      ObjectId skillId = e.getKey();
      double value = e.getValue();
      skillService.addHours(skillId, value);
    });

    logger.info("activityRepository.save(updatedActivity={})", updatedActivity);
    return activityRepository.save(updatedActivity);
  }

  /**
   * Given a userId and a number of days, return the RecentActivity DTO for the User in those days.
   *
   * @param userId The Id of the User
   * @param days The number of days
   * @return The RecentActivityDTO
   */
  public RecentActivity getRecentActivityByUserId(ObjectId userId, int days) {
    logger.info("getRecentActivityByUserId(userId={}, days={})", userId, days);
    Instant startInstant =
        LocalDate.now(ZoneOffset.UTC).minusDays(days).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endInstant =
        LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    logger.info("activityRepository.findByUserIdAndCreatedAtBetween(userId={}, startInstant={}, endInstant={})", userId, startInstant, endInstant);
    List<Activity> activities =
        activityRepository.findByUserIdAndCreatedAtBetween(userId, startInstant, endInstant);
    return ActivityMapper.toRecentActivity(activities);
  }

  /**
   * Delete an Activity given its Id. Also removes the duration * weight from each involved skill's
   * timeSpentHours.
   *
   * @param activityId The Id of the Activity to be deleted
   */
  @Transactional
  public void deleteById(ObjectId activityId) {
    logger.info("deleteById(activityId={})", activityId);
    logger.info("activityRepository.findById(activityId={})", activityId);
    Activity activity = activityRepository.findById(activityId).orElseThrow(
        () -> new NotFoundException("activities", Map.of("activityId", activityId.toString())));
    double duration = activity.getDuration();
    activity.getSkillWeights().forEach(sw -> {
      skillService.addHours(sw.getSkillId(), sw.getWeight() * duration * -1);
    });
    logger.info("activityRepository.deleteById(activityId={})", activityId);
    activityRepository.deleteById(activityId);
  }

  @Transactional
  public void deleteByUserId(ObjectId userId) {
    logger.info("deleteByUserId(userId={})", userId);
    logger.info("activityRepository.findByUserId(userId={})", userId);
    activityRepository.findByUserId(userId).forEach(activity -> deleteById(activity.getId()));
  }

  /**
   * Delete an Activity given its userId and Id. Also removes the duration * weight from each
   * involved skill's timeSpentHours.
   *
   * @param userId The Id of the User the Activity belongs to
   * @param activityId The Id of the Activity
   */
  public void deleteByUserIdAndId(ObjectId userId, ObjectId activityId) {
    logger.info("deleteByUserIdAndId(userId={}, activityId={})", userId, activityId);
    logger.info("activityRepository.findByUserIdAndId(userId={}, activityId={})", userId, activityId);
    activityRepository.findByUserIdAndId(userId, activityId)
        .orElseThrow(() -> new NotFoundException("activities",
            Map.of("userId", userId.toString(), "activityId", activityId.toString())));
    logger.info("activityRepository.deleteByUserIdAndId(userId={}, activityId={})", userId, activityId);
    activityRepository.deleteByUserIdAndId(userId, activityId);
  }
}
