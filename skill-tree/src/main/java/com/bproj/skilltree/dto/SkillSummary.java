package com.bproj.skilltree.dto;

/**
 * Basic Skill information. Used to display a Skill to Users that do not own it.
 */
public class SkillSummary {
  private String name;
  private String backgroundUrl;
  private double timeSpentHours;

  /**
   * Explicit value constructor.
   *
   * @param name The name of the Skill
   * @param backgroundUrl The background URL for the Skill
   * @param timeSpentHours The time spent on this Skill in hours
   */
  public SkillSummary(String name, String backgroundUrl, double timeSpentHours) {
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.timeSpentHours = timeSpentHours;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public void setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
  }

  public double getTimeSpentHours() {
    return timeSpentHours;
  }

  public void setTimeSpentHours(double timeSpentHours) {
    this.timeSpentHours = timeSpentHours;
  }
}
