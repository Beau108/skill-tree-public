package com.bproj.skilltree.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import org.bson.types.ObjectId;

/**
 * For Orientations, where a Skill resides in a Tree view.
 */
@ToString(onlyExplicitlyIncluded = true)
public class SkillLocation {
  @NotNull
  @ToString.Include
  private ObjectId skillId;
  @NotNull
  @ToString.Include
  private double x;
  @NotNull
  @ToString.Include
  private double y;

  public SkillLocation() {
  }

  /**
   * Explicit value constructor.
   *
   * @param skillId The Id of the Skill
   * @param x       The x location of the Skill
   * @param y       The y location of the Skill
   */
  @JsonCreator
  public SkillLocation(
      @JsonProperty("skillId") ObjectId skillId,
      @JsonProperty("x") double x,
      @JsonProperty("y") double y) {
    this.skillId = skillId;
    this.x = x;
    this.y = y;
  }

  public ObjectId getSkillId() {
    return skillId;
  }

  public void setSkillId(ObjectId skillId) {
    this.skillId = skillId;
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
