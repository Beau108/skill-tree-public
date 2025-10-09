package com.bproj.skilltree.dto;

/**
 * How a single Skill will be shown.
 */
public class SkillLayout {
  private String parentSkill;
  private double timeSpentHours;
  private String backgroundUrl;
  private double x;
  private double y;

  /**
   * Explicit value constructor.
   *
   * @param parentSkill Name of the parent Skill
   * @param timeSpentHours Time spent on this Skill in hours
   * @param backgroundUrl Background URL for the Skill
   * @param x X-coordinate for rendering position
   * @param y Y-coordinate for rendering position
   */
  public SkillLayout(String parentSkill, double timeSpentHours, String backgroundUrl, double x,
      double y) {
    this.parentSkill = parentSkill;
    this.timeSpentHours = timeSpentHours;
    this.backgroundUrl = backgroundUrl;
    this.x = x;
    this.y = y;
  }

  public String getParentSkill() {
    return parentSkill;
  }

  public void setParentSkill(String parentSkill) {
    this.parentSkill = parentSkill;
  }

  public double getTimeSpentHours() {
    return timeSpentHours;
  }

  public void setTimeSpentHours(double timeSpentHours) {
    this.timeSpentHours = timeSpentHours;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public void setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }


}
