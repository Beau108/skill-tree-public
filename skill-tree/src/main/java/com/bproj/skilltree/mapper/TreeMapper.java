package com.bproj.skilltree.mapper;

import com.bproj.skilltree.dto.AchievementLayout;
import com.bproj.skilltree.dto.MeAchievementLayout;
import com.bproj.skilltree.dto.MeSkillLayout;
import com.bproj.skilltree.dto.MeTreeLayout;
import com.bproj.skilltree.dto.SkillLayout;
import com.bproj.skilltree.dto.TreeFeedItem;
import com.bproj.skilltree.dto.TreeLayout;
import com.bproj.skilltree.dto.TreeRequest;
import com.bproj.skilltree.dto.TreeResponse;
import com.bproj.skilltree.dto.TreeSummary;
import com.bproj.skilltree.model.Achievement;
import com.bproj.skilltree.model.AchievementLocation;
import com.bproj.skilltree.model.Orientation;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.model.SkillLocation;
import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

/**
 * Tree DTO conversions.
 */
public class TreeMapper {
  private TreeMapper() {}

  /**
   * Create a TreeResponse dto from a Tree.
   *
   * @param tree The Tree the dto will be made from
   * @return The Tree dto
   */
  public static TreeResponse fromTree(Tree tree) {
    if (tree == null) {
      return null;
    }
    return new TreeResponse(tree.getId().toString(), tree.getName(), tree.getBackgroundUrl(),
        tree.getDescription(), tree.getVisibility());
  }

  /**
   * Create a TreeSummary dto from a Tree.
   *
   * @param tree The Tree the dto will be made from
   * @return The TreeSummary dto
   */
  public static TreeSummary toTreeSummary(Tree tree) {
    if (tree == null) {
      return null;
    }

    return new TreeSummary(tree.getName(), tree.getBackgroundUrl(), tree.getDescription());
  }

  /**
   * Create a Tree from a Tree dto.
   *
   * @param treeRequest The dto to make a Tree from
   * @return The Tree created
   */
  public static Tree toTree(TreeRequest treeRequest) {
    if (treeRequest == null) {
      return null;
    }
    return new Tree(treeRequest.getName(), treeRequest.getBackgroundUrl(),
        treeRequest.getDescription(), treeRequest.getVisibility());
  }

  /**
   * Create a TreeLayout from a list of skills, a list of achievements, and an orientation.
   *
   * @param skills The list of skill
   * @param achievements The list of achievements
   * @param orientation The orientation
   * @return The created TreeLayout
   */
  public static TreeLayout toTreeLayout(List<Skill> skills, List<Achievement> achievements,
      Orientation orientation) {
    // Index locations to avoid O(n^2) runtime
    Map<ObjectId, SkillLocation> skillLocationMap = orientation.getSkillLocations().stream()
        .collect(Collectors.toMap(SkillLocation::getSkillId, sl -> sl));
    Map<ObjectId, AchievementLocation> achievementLocationMap =
        orientation.getAchievementLocations().stream()
            .collect(Collectors.toMap(AchievementLocation::getAchievementId, al -> al));

    // Index Achievements and Skills as well
    Map<ObjectId, Skill> skillMap = skills.stream().collect(Collectors.toMap(Skill::getId, s -> s));
    Map<ObjectId, Achievement> achievementMap =
        achievements.stream().collect(Collectors.toMap(Achievement::getId, a -> a));

    // Create Layout Maps
    Map<String, SkillLayout> skillLayoutMap = new HashMap<String, SkillLayout>();
    Map<String, AchievementLayout> achievementLayoutMap = new HashMap<String, AchievementLayout>();

    // Build SkillLayouts
    for (Skill s : skills) {
      String parentSkillName = null;
      if (s.getParentSkillId() != null) {
        if (!skillMap.containsKey(s.getParentSkillId())) {
          throw new IllegalStateException(
              "Parent skill of " + s.getName() + " is not in the same tree.");
        }
        parentSkillName = skillMap.get(s.getParentSkillId()).getName();
      }
      SkillLocation skillLocation = skillLocationMap.get(s.getId());
      if (skillLocation == null) {
        throw new IllegalStateException("Orientation does not contain skill: " + s.getName());
      }

      SkillLayout sl = new SkillLayout(parentSkillName, s.getTimeSpentHours(), s.getBackgroundUrl(),
          skillLocation.getX(), skillLocation.getY());
      skillLayoutMap.put(s.getName(), sl);
    }

    // Build AchievementLayouts
    for (Achievement a : achievements) {
      List<String> prerequisiteTitles = a.getPrerequisites().stream()
          .map(prereqId -> achievementMap.get(prereqId).getTitle()).toList();

      AchievementLocation achievementLocation = achievementLocationMap.get(a.getId());
      if (achievementLocation == null) {
        throw new IllegalStateException(
            "Orientation does not contain achievement: " + a.getTitle());
      }

      AchievementLayout al = new AchievementLayout(prerequisiteTitles, a.getDescription(),
          a.getBackgroundUrl(), a.isComplete(), a.getCompletedAt(), achievementLocation.getX(),
          achievementLocation.getY());
      achievementLayoutMap.put(a.getTitle(), al);
    }
    return new TreeLayout(skillLayoutMap, achievementLayoutMap);
  }


  /**
   * Create a MeTreeLayout (TreeLayout with Ids).
   *
   * @param skills The Skills in the Tree
   * @param achievements The Achievements in the Tree
   * @param orientation The Orientation of the Tree
   * @return The MeTreeLayout DTO
   */
  public static MeTreeLayout toMeTreeLayout(List<Skill> skills, List<Achievement> achievements,
      Orientation orientation) {
    // Create skillLayout mapping
    Map<ObjectId, SkillLocation> skillLocationMap = orientation.getSkillLocations().stream()
        .collect(Collectors.toMap(SkillLocation::getSkillId, sl -> sl));
    Map<ObjectId, MeSkillLayout> skillLayout =
        skills.stream().collect(Collectors.toMap(Skill::getId, s -> {
          SkillLocation skillLocation = skillLocationMap.get(s.getId());
          if (skillLocation == null) {
            throw new IllegalStateException("Skill not listed in Orientation.");
          }
          return new MeSkillLayout(s.getId().toString(), s.getName(), skillLocation.getX(),
              skillLocation.getY(), s.getBackgroundUrl(), s.getTimeSpentHours(),
              s.getParentSkillId() != null ? s.getParentSkillId().toString() : null);
        }));

    // Create achievementLayout mapping
    Map<ObjectId, AchievementLocation> achievementLocationMap =
        orientation.getAchievementLocations().stream()
            .collect(Collectors.toMap(AchievementLocation::getAchievementId, al -> al));
    Map<ObjectId, MeAchievementLayout> achievementLayout =
        achievements.stream().collect(Collectors.toMap(Achievement::getId, a -> {
          AchievementLocation achievementLocation = achievementLocationMap.get(a.getId());
          if (achievementLocation == null) {
            throw new IllegalStateException("Achievement not listed in Orientation.");
          }
          return new MeAchievementLayout(a.getId().toString(), a.getTitle(), achievementLocation.getX(),
              achievementLocation.getY(), a.getBackgroundUrl(), a.isComplete(), a.getCompletedAt(),
              a.getPrerequisites().stream().map(ObjectId::toString).toList());
        }));
    return new MeTreeLayout(skillLayout, achievementLayout);
  }

  /**
   * Create a TreeFeedItem DTO from a Tree and User.
   *
   * @param tree The Tree
   * @param user The User
   * @return The TreeFeedItem DTO
   */
  public static TreeFeedItem toTreeFeedItem(Tree tree, User user) {
    if (tree == null || user == null) {
      return null;
    }
    return new TreeFeedItem(tree.getCreatedAt(), user.getDisplayName(), user.getProfilePictureUrl(),
        tree.getName(), tree.getDescription(), tree.getBackgroundUrl());
  }

}
