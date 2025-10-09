package com.bproj.skilltree.mapper;

import com.bproj.skilltree.dto.ActivityFeedItem;
import com.bproj.skilltree.dto.ActivityRequest;
import com.bproj.skilltree.dto.ActivityResponse;
import com.bproj.skilltree.dto.ActivitySummary;
import com.bproj.skilltree.dto.RecentActivity;
import com.bproj.skilltree.dto.WeightedSkill;
import com.bproj.skilltree.model.Activity;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.model.SkillWeight;
import com.bproj.skilltree.model.User;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

/**
 * Activity DTO conversions.
 */
public class ActivityMapper {
  private ActivityMapper() {}

  /**
   * Create an Activity from an ActivityRequest DTO.
   *
   * @param activityRequest The ActivityRequest DTO the Activity is created from
   * @return The created Activity. Requires field assignments post creation.
   */
  public static Activity toActivity(ActivityRequest activityRequest) {
    if (activityRequest == null) {
      return null;
    }
    return new Activity(activityRequest.getName(), activityRequest.getDescription(),
        activityRequest.getDuration(), activityRequest.getSkillWeights());
  }

  private static List<WeightedSkill> toWeightedSkills(Activity activity, List<Skill> skills) {
    Map<ObjectId, Skill> skillMap =
        skills.stream().collect(Collectors.toMap(Skill::getId, skill -> skill));
    List<SkillWeight> skillWeights = activity.getSkillWeights();
    return skillWeights.stream().map(sw -> {
      Skill s = skillMap.get(sw.getSkillId());
      if (s == null) {
        throw new IllegalStateException(
            "Could not find skill {" + sw.getSkillId() + "} in the provided Skills list.");
      }
      return new WeightedSkill(sw.getWeight(), s.getName(), s.getBackgroundUrl());
    }).toList();
  }

  /**
   * Create an ActivityResponse DTO from an Activity.
   *
   * @param activity The Activity the ActivityResponse DTO is created from
   * @return The created ActivityResponse
   */
  public static ActivityResponse fromActivity(Activity activity, List<Skill> skills) {
    if (activity == null) {
      return null;
    }
    List<WeightedSkill> weightedSkills = toWeightedSkills(activity, skills);
    return new ActivityResponse(activity.getId(), activity.getName(), activity.getDescription(),
        activity.getDuration(), weightedSkills);
  }

  /**
   * Create an ActivitySummary DTO from an Activity.
   *
   * @param activity The Activity the ActivitySummary DTO is created from
   * @return The created ActivitySummary
   */
  public static ActivitySummary toActivitySummary(Activity activity, List<Skill> skills) {
    if (activity == null) {
      return null;
    }
    List<WeightedSkill> weightedSkills = toWeightedSkills(activity, skills);
    return new ActivitySummary(activity.getName(), activity.getDuration(), weightedSkills);
  }

  /**
   * Create an ActivityFeedItem DTO from an Activity, a List of Skills, and a User.
   *
   * @param activity The Activity
   * @param skills The List of Skills
   * @param user The User
   * @return The ActivityFeedItem DTO
   */
  public static ActivityFeedItem toActivityFeedItem(Activity activity, List<Skill> skills,
      User user) {
    if (activity == null || user == null) {
      return null;
    }
    List<WeightedSkill> weightedSkills = toWeightedSkills(activity, skills);
    return new ActivityFeedItem(activity.getCreatedAt(), user.getDisplayName(),
        user.getProfilePictureUrl(), activity.getName(), activity.getDuration(),
        activity.getDescription(), weightedSkills);
  }

  /**
   * Create a summary of recent activity given a list of Activities.
   *
   * @param activities The Activities to be summarized
   * @return The RecentActivity DTO
   */
  public static RecentActivity toRecentActivity(List<Activity> activities) {
    Map<LocalDate, Integer> dailyCounts = activities.stream().collect(Collectors.groupingBy(
        a -> a.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(), Collectors.summingInt(x -> 1)));

    List<LocalDate> sortedDates = dailyCounts.keySet().stream().sorted().toList();

    int streak = 0;

    for (int i = sortedDates.size() - 1; i >= 0; i--) {
      LocalDate date = sortedDates.get(i);
      Integer count = dailyCounts.getOrDefault(date, 0);

      if (count != null && count > 0) {
        streak++;
      } else {
        if (streak > 0) { 
          break;
        }
      }
    }
    return new RecentActivity(streak, dailyCounts);
  }
}
