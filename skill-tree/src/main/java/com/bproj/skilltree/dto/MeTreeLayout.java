package com.bproj.skilltree.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.bson.types.ObjectId;

/**
 * Information required for rendering a Tree plus identity fields on Skills and Achievements,
 * letting the end user 'select' these nodes for editing/deletion.
 */
public class MeTreeLayout {
  @NotNull
  @Valid
  private Map<ObjectId, MeSkillLayout> skillLayout;

  @NotNull
  @Valid
  private Map<ObjectId, MeAchievementLayout> achievementLayout;

  public MeTreeLayout() {
    this.skillLayout = Map.of();
    this.achievementLayout = Map.of();
  }

  public MeTreeLayout(Map<ObjectId, MeSkillLayout> skillLayout,
      Map<ObjectId, MeAchievementLayout> achievementLayout) {
    this.skillLayout = skillLayout;
    this.achievementLayout = achievementLayout;
  }

  public Map<ObjectId, MeSkillLayout> getSkillLayout() {
    return skillLayout;
  }

  public void setSkillLayout(Map<ObjectId, MeSkillLayout> skillLayout) {
    this.skillLayout = skillLayout;
  }

  public Map<ObjectId, MeAchievementLayout> getAchievementLayout() {
    return achievementLayout;
  }

  public void setAchievementLayout(Map<ObjectId, MeAchievementLayout> achievementLayout) {
    this.achievementLayout = achievementLayout;
  }

}
