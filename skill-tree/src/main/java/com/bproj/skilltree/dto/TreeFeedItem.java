package com.bproj.skilltree.dto;

import java.time.Instant;

/**
 * A single Tree card in the Friend actions feed.
 */
public class TreeFeedItem implements FeedItem {
  private Instant postedAt;
  private String displayName;
  private String profilePictureUrl;
  private String name;
  private String description;
  private String backgroundUrl;

  /**
   * Explicit value constructor.
   *
   * @param postedAt Timestamp when the Tree was created
   * @param displayName Display name of the User who created the Tree
   * @param profilePictureUrl Profile picture URL of the User
   * @param name Name of the Tree
   * @param description Description of the Tree
   * @param backgroundUrl Background URL for the Tree
   */
  public TreeFeedItem(Instant postedAt, String displayName, String profilePictureUrl, String name,
      String description, String backgroundUrl) {
    this.postedAt = postedAt;
    this.displayName = displayName;
    this.profilePictureUrl = profilePictureUrl;
    this.name = name;
    this.description = description;
    this.backgroundUrl = backgroundUrl;
  }

  public String getType() {
    return "TREE";
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
}
