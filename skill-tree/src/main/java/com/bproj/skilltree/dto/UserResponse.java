package com.bproj.skilltree.dto;

/**
 * Outgoing essential User information.
 */
public class UserResponse {
  private String displayName;
  private String profilePictureUrl;

  public UserResponse() {}

  /**
   * Explicit value constructor.
   *
   * @param displayName The display name of the User
   * @param profilePictureUrl The profile picture url of the User
   */
  public UserResponse(String displayName, String profilePictureUrl) {
    this.displayName = displayName;
    this.profilePictureUrl = profilePictureUrl;
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
}
