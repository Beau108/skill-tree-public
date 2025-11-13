package com.bproj.skilltree.mapper;

import com.bproj.skilltree.dto.AchievementFeedItem;
import com.bproj.skilltree.dto.AchievementRequest;
import com.bproj.skilltree.dto.AchievementResponse;
import com.bproj.skilltree.dto.AchievementSummary;
import com.bproj.skilltree.model.Achievement;
import com.bproj.skilltree.model.User;
import org.bson.types.ObjectId;

/**
 * Achievement DTO conversions.
 */
public class AchievementMapper {
  private AchievementMapper() {}

  /**
   * Create an Achievement from an AchievementRequest DTO.
   *
   * @param achievementRequest The DTO the Achievement is created from
   * @return The created Achievement
   */
  public static Achievement toAchievement(AchievementRequest achievementRequest) {
    if (achievementRequest == null) {
      return null;
    }
    return new Achievement(new ObjectId(achievementRequest.getTreeId()), achievementRequest.getTitle(), achievementRequest.getBackgroundUrl(),
        achievementRequest.getDescription(), achievementRequest.getPrerequisites().stream().map(ObjectId::new).toList(),
        achievementRequest.isComplete(), achievementRequest.getCompletedAt());
  }

  /**
   * Create an AchievementResponse DTO from an Achievement.
   *
   * @param achievement The Achievement the AchievementResponse DTO will be created from
   * @return The created AchievementResponse DTO
   */
  public static AchievementResponse fromAchievement(Achievement achievement) {
    if (achievement == null) {
      return null;
    }

    return new AchievementResponse(achievement.getId().toString(), achievement.getTreeId().toString(),
        achievement.getTitle(), achievement.getBackgroundUrl(), achievement.getDescription(),
        achievement.getPrerequisites().stream().map(ObjectId::toString).toList(), achievement.isComplete(), achievement.getCompletedAt());
  }

  /**
   * Create an AchievementSummary DTO from an Achievement.
   *
   * @param achievement The Achievement the AchievementSummary DTO will be created from
   * @return The created AchievementSummary
   */
  public static AchievementSummary getAchievementSummary(Achievement achievement) {
    if (achievement == null) {
      return null;
    }

    return new AchievementSummary(achievement.getTitle(), achievement.getBackgroundUrl(),
        achievement.isComplete(), achievement.getCompletedAt());
  }

  /**
   * Create an AchievementFeedItem DTO from an Achievement and a User.
   *
   * @param achievement The Achievement
   * @param user The User
   * @return The AchievementFeedItem DTO
   */
  public static AchievementFeedItem toAchievementFeedItem(Achievement achievement, User user) {
    if (achievement == null || user == null) {
      return null;
    }
    return new AchievementFeedItem(achievement.getCompletedAt(), user.getDisplayName(),
        user.getProfilePictureUrl(), achievement.getTitle(), achievement.getBackgroundUrl(),
        achievement.getDescription());
  }
}
