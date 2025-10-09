package com.bproj.skilltree.dto;

import java.time.Instant;

/**
 * Basic Achievement information. Used so User's that do not own this Achievement can see it's
 * details without having its raw Id exposed to them.
 */
public class AchievementSummary {
  private String title;
  private String backgroundUrl;
  private boolean complete;
  private Instant completedAt;

  /**
   * Explicit value constructor.
   *
   * @param title The title of the Achievement
   * @param backgroundUrl The background URL for the Achievement
   * @param complete Whether the Achievement is complete
   * @param completedAt Timestamp when the Achievement was completed
   */
  public AchievementSummary(String title, String backgroundUrl, boolean complete,
      Instant completedAt) {
    this.title = title;
    this.backgroundUrl = backgroundUrl;
    this.complete = complete;
    this.completedAt = completedAt;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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
}
