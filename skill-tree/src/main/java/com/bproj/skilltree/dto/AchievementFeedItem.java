package com.bproj.skilltree.dto;

import java.time.Instant;

/**
 * A single Achievement card that shows up in the Friend actions feed.
 */
public class AchievementFeedItem implements FeedItem {
  private Instant postedAt;
  private String displayName;
  private String profilePictureUrl;
  private String title;
  private String backgroundUrl;
  private String description;

  /**
   * Explicit value constructor.
   *
   * @param postedAt Timestamp when the Achievement was completed
   * @param displayName Display name of the User who completed the Achievement
   * @param profilePictureUrl Profile picture URL of the User
   * @param title Title of the Achievement
   * @param backgroundUrl Background URL for the Achievement
   * @param description Description of the Achievement
   */
  public AchievementFeedItem(Instant postedAt, String displayName, String profilePictureUrl,
      String title, String backgroundUrl, String description) {
    this.postedAt = postedAt;
    this.displayName = displayName;
    this.profilePictureUrl = profilePictureUrl;
    this.title = title;
    this.backgroundUrl = backgroundUrl;
    this.description = description;
  }

  public String getType() {
    return "ACHIEVEMENT";
  }

  public Instant getPostedAt() {
    return postedAt;
  }

  public void setPostedAt(Instant postedAt) {
    this.postedAt = postedAt;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getProfilePictureUrl() {
    return profilePictureUrl;
  }

  public void setProfilePictureUrl(String profilePictureUrl) {
    this.profilePictureUrl = profilePictureUrl;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }



}
