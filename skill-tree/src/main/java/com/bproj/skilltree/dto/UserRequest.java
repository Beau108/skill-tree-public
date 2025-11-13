package com.bproj.skilltree.dto;

import com.bproj.skilltree.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Incoming essential User information.
 */
public class UserRequest {
  @Pattern(regexp = RegexPatterns.DISPLAY_NAME)
  @NotNull
  private String displayName;
  @Pattern(regexp = RegexPatterns.IMAGE_URL)
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
