package com.bproj.skilltree.dto;

import java.time.Instant;
import java.util.List;

/**
 * How a single Achievement will be shown. Prerequisites referenced by Title.
 */
public class AchievementLayout {
  private List<String> prerequisites;
  private String description;
  private String backgroundUrl;
  private boolean complete;
  private Instant completedAt;
  private double x;
  private double y;

  /**
   * Explicit value constructor.
   *
   * @param prerequisites List of prerequisite Achievement titles
   * @param description Description of the Achievement
   * @param backgroundUrl Background URL for the Achievement
   * @param complete Whether the Achievement is complete
   * @param completedAt Timestamp when the Achievement was completed
   * @param x X-coordinate for rendering position
   * @param y Y-coordinate for rendering position
   */
  public AchievementLayout(List<String> prerequisites, String description, String backgroundUrl,
      boolean complete, Instant completedAt, double x, double y) {
    this.prerequisites = prerequisites;
    this.description = description;
    this.backgroundUrl = backgroundUrl;
    this.complete = complete;
    this.completedAt = completedAt;
    this.x = x;
    this.y = y;
  }
  
  public List<String> getPrerequisites() {
    return prerequisites;
  }
  
  public void setPrerequisites(List<String> prerequisites) {
    this.prerequisites = prerequisites;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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
