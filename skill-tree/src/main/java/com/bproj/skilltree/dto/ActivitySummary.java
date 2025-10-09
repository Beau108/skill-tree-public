package com.bproj.skilltree.dto;

import java.util.List;

/**
 * Basic Activity information. Uses WeightedSkills to avoid lookups on the client side and also to
 * hide skill Ids.
 */
public class ActivitySummary {
  private String name;
  private double duration;
  private List<WeightedSkill> weightedSkills;

  /**
   * Explicit value constructor.
   *
   * @param name The name of the Activity
   * @param duration The duration of the Activity
   * @param weightedSkills List of weighted Skills with full Skill information
   */
  public ActivitySummary(String name, double duration, List<WeightedSkill> weightedSkills) {
    this.name = name;
    this.duration = duration;
    this.weightedSkills = weightedSkills;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getDuration() {
    return duration;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public List<WeightedSkill> getWeightedSkills() {
    return weightedSkills;
  }

  public void setWeightedSkills(List<WeightedSkill> weightedSkills) {
    this.weightedSkills = weightedSkills;
  }
}
