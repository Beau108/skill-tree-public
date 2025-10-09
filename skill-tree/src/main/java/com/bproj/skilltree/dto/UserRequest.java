package com.bproj.skilltree.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming essential User information.
 */
public class UserRequest {
  @NotBlank
  private String displayName;
  private String profilePictureUrl;

  public UserRequest() {
  }

  /**
   * Explicit value constructor.
   *
   * @param displayName       The display name of the user
   * @param profilePictureUrl The profile picture url of the user
   */
  public UserRequest(String displayName, String profilePictureUrl) {
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
