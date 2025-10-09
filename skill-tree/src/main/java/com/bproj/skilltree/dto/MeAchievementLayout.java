package com.bproj.skilltree.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Information required for rendering a single Achievement plus the Id for
 * editing/deleting
 * purposes.
 */
public class MeAchievementLayout {
  @NotNull
  private ObjectId id;

  @NotBlank
  private String title;

  private double x;

  private double y;

  private String backgroundUrl;

  private boolean complete;

  private Instant completedAt;

  private List<ObjectId> prerequisites;

  /**
   * Explicit value constructor.
   *
   * @param id            The Id of the Achievement
   * @param title         The title of the Achievement
   * @param x             The x location of the Achievement
   * @param y             The y location of the Achievmeent
   * @param backgroundUrl The background image url for the Achievement
   * @param complete      Whether or not this Achievement is complete
   * @param completedAt   When this Achievement was completed
   * @param prerequisites The prerequisite Achievements for this Achievement
   */
  public MeAchievementLayout(ObjectId id, String title, double x, double y, String backgroundUrl,
      boolean complete, Instant completedAt, List<ObjectId> prerequisites) {
    this.id = id;
    this.title = title;
    this.x = x;
    this.y = y;
    this.backgroundUrl = backgroundUrl;
    this.complete = complete;
    this.completedAt = completedAt;
    this.prerequisites = prerequisites;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public boolean isComplete() {
    return complete;
  }

  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }

  public List<ObjectId> getPrerequisites() {
    return prerequisites;
  }

  public void setPrerequisites(List<ObjectId> prerequisites) {
    this.prerequisites = prerequisites;
  }
}
