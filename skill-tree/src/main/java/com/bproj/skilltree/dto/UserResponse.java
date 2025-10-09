package com.bproj.skilltree.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Outgoing essential User information.
 */
public class UserResponse {
  @NotBlank
  private String displayName;
  @NotBlank
  private String profilePictureUrl;
  
  public UserResponse() {}
  
  /**
   * Explicit value constructor.
   *
   * @param displayName         The display name of the User
   * @param profilePictureUrl   The profile picture url of the User
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
