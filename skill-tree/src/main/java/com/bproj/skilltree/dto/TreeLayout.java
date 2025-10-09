package com.bproj.skilltree.dto;

import java.util.Map;

/**
 * Minimum required information for rendering a Tree. Names and Titles of Skills/Achievements are
 * used as Map keys.
 */
public class TreeLayout {
  Map<String, SkillLayout> skillLayout;
  Map<String, AchievementLayout> achievementLayout;

  /**
   * Explicit value constructor.
   *
   * @param skillLayout Map of Skill names to their layout information
   * @param achievementLayout Map of Achievement titles to their layout information
   */
  public TreeLayout(Map<String, SkillLayout> skillLayout,
      Map<String, AchievementLayout> achievementLayout) {
    this.skillLayout = skillLayout;
    this.achievementLayout = achievementLayout;
  }

  public Map<String, SkillLayout> getSkillLayout() {
    return skillLayout;
  }

  public void setSkillLayout(Map<String, SkillLayout> skillLayout) {
    this.skillLayout = skillLayout;
  }

  public Map<String, AchievementLayout> getAchievementLayout() {
    return achievementLayout;
  }

  public void setAchievementLayout(Map<String, AchievementLayout> achievementLayout) {
    this.achievementLayout = achievementLayout;
  }

}
