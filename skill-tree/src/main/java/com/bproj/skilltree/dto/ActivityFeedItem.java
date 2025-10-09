package com.bproj.skilltree.dto;

import java.time.Instant;
import java.util.List;

/**
 * A single Activity card that shows up in the Friend action feed.
 */
public class ActivityFeedItem implements FeedItem {
  private Instant postedAt;
  private String displayName;
  private String profilePictureUrl;
  private String name;
  private double duration;
  private String description;
  private List<WeightedSkill> weightedSkills;

  /**
   * Explicit value constructor.
   *
   * @param postedAt Timestamp when the Activity was created
   * @param displayName Display name of the User who created the Activity
   * @param profilePictureUrl Profile picture URL of the User
   * @param name Name of the Activity
   * @param duration Duration of the Activity
   * @param description Description of the Activity
   * @param weightedSkills List of weighted Skills associated with this Activity
   */
  public ActivityFeedItem(Instant postedAt, String displayName, String profilePictureUrl,
      String name, double duration, String description, List<WeightedSkill> weightedSkills) {
    this.postedAt = postedAt;
    this.displayName = displayName;
    this.profilePictureUrl = profilePictureUrl;
    this.name = name;
    this.duration = duration;
    this.description = description;
    this.weightedSkills = weightedSkills;
  }

  public String getType() {
    return "ACTIVITY";
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

  public double getDuration() {
    return duration;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<WeightedSkill> getWeightedSkills() {
    return weightedSkills;
  }

  public void setWeightedSkills(List<WeightedSkill> weightedSkills) {
    this.weightedSkills = weightedSkills;
  }
}
