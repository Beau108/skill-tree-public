package com.bproj.skilltree.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.bson.types.ObjectId;

/**
 * Information required for rendering a single Skill plus the Id for editing/deleting purposes.
 */
public class MeSkillLayout {
  @NotNull
  private ObjectId id;

  @NotBlank
  private String name;

  private double x;

  private double y;

  private String backgroundUrl;

  @PositiveOrZero
  private double timeSpentHours;

  private ObjectId parentSkillId;

  /**
   * Explicit value constructor.
   *
   * @param id The id of the Skill
   * @param name The name of the Skill
   * @param x The x location of the Skill
   * @param y The y location of the Skill
   * @param backgroundUrl The background image displayed for this Skill
   * @param timeSpentHours The time the User has logged for this skill
   * @param parentSkillId The parent id of this skill
   */
  public MeSkillLayout(ObjectId id, String name, double x, double y, String backgroundUrl,
      double timeSpentHours, ObjectId parentSkillId) {
    this.id = id;
    this.name = name;
    this.x = x;
    this.y = y;
    this.backgroundUrl = backgroundUrl;
    this.timeSpentHours = timeSpentHours;
    this.parentSkillId = parentSkillId;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public ObjectId getParentSkillId() {
    return parentSkillId;
  }

  public void setParentSkillId(ObjectId parentSkillId) {
    this.parentSkillId = parentSkillId;
  }
}
