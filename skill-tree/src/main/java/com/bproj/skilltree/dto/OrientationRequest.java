package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.AchievementLocation;
import com.bproj.skilltree.model.SkillLocation;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Contains Orientation information required to perform an update.
 */
public class OrientationRequest {
  private ObjectId treeId;
  private List<SkillLocation> skillLocations;
  private List<AchievementLocation> achievementLocations;

  /**
   * Explicit value constructor.
   *
   * @param treeId The Id of the Tree this Orientiaton belongs to
   * @param skillLocations The locations of each Skill in the Tree
   * @param achievementLocations The locations of each Achievement in the Tree
   */
  public OrientationRequest(ObjectId treeId, List<SkillLocation> skillLocations,
      List<AchievementLocation> achievementLocations) {
    this.treeId = treeId;
    this.skillLocations = skillLocations;
    this.achievementLocations = achievementLocations;
  }

  public ObjectId getTreeId() {
    return treeId;
  }

  public void setTreeId(ObjectId treeId) {
    this.treeId = treeId;
  }

  public List<SkillLocation> getSkillLocations() {
    return skillLocations;
  }

  public void setSkillLocations(List<SkillLocation> skillLocations) {
    this.skillLocations = skillLocations;
  }

  public List<AchievementLocation> getAchievementLocations() {
    return achievementLocations;
  }

  public void setAchievementLocations(List<AchievementLocation> achievementLocations) {
    this.achievementLocations = achievementLocations;
  }
}
