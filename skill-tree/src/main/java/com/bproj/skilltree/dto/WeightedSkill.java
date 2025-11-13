package com.bproj.skilltree.dto;

/**
 * DTO version of an Activity's "SkillWeight" that contains a weight and minimal
 * skill info rather than an Id and a weight.
 */
public class WeightedSkill {
  private double weight;
  private String skillName;
  private String backgroundUrl;

  public WeightedSkill() {
  }

  /**
   * Explicit value constructor.
   *
   * @param weight        How much this Skill contributed to its Activity
   * @param skillName     The name of this Skill
   * @param backgroundUrl URL of the background image of this Skill
   */
  public WeightedSkill(double weight, String skillName, String backgroundUrl) {
    this.weight = weight;
    this.skillName = skillName;
    this.backgroundUrl = backgroundUrl;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public String getSkillName() {
    return skillName;
  }

  public void setSkillName(String skillName) {
    this.skillName = skillName;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public void setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
  }
}
