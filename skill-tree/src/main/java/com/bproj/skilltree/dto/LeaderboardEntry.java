package com.bproj.skilltree.dto;

/**
 * DTO for a single User's appearance on a Leaderboard. Will probably always be sent in Lists.
 */
public class LeaderboardEntry {
  private String displayName;
  private String profilePictureUrl;
  private double timeLogged;
  private int achievementsCompleted;

  /**
   * Explicit value constructor.
   *
   * @param displayName Display name of the User
   * @param profilePictureUrl Profile picture URL of the User
   * @param timeLogged Total time logged by the User in hours
   * @param achievementsCompleted Number of Achievements completed by the User
   */
  public LeaderboardEntry(String displayName, String profilePictureUrl, double timeLogged,
      int achievementsCompleted) {
    this.displayName = displayName;
    this.profilePictureUrl = profilePictureUrl;
    this.timeLogged = timeLogged;
    this.achievementsCompleted = achievementsCompleted;
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

  public double getTimeLogged() {
    return timeLogged;
  }

  public void setTimeLogged(double timeLogged) {
    this.timeLogged = timeLogged;
  }

  public int getAchievementsCompleted() {
    return achievementsCompleted;
  }

  public void setAchievementsCompleted(int achievementsCompleted) {
    this.achievementsCompleted = achievementsCompleted;
  }
}
