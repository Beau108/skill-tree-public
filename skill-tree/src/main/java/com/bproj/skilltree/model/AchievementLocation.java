package com.bproj.skilltree.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

/**
 * For Orientations, represents the location of an Achievement in a Tree view.
 */
public class AchievementLocation {
  @NotNull
  private ObjectId achievementId;
  @NotNull
  private double x;
  @NotNull
  private double y;

  public AchievementLocation() {
  }

  /**
   * Explicit value constructor.
   *
   * @param achievementId The Id of the Achievement
   * @param x             The x location of the Achievement
   * @param y             The y location of the Achievement
   */
  @JsonCreator
  public AchievementLocation(
      @JsonProperty("achievementId") ObjectId achievementId,
      @JsonProperty("x") double x,
      @JsonProperty("y") double y) {
    this.achievementId = achievementId;
    this.x = x;
    this.y = y;
  }

  public ObjectId getAchievementId() {
    return achievementId;
  }

  public void setAchievementId(ObjectId achievementId) {
    this.achievementId = achievementId;
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
