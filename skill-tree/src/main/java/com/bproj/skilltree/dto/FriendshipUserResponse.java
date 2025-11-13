package com.bproj.skilltree.dto;

import org.bson.types.ObjectId;

/**
 * UserResponse wrapper class containing a Friendship Id.
 */
public class FriendshipUserResponse {
  private String friendshipId;
  private UserResponse user;

  public FriendshipUserResponse(String friendshipId, String displayName,
      String profilePictureUrl) {
    this.friendshipId = friendshipId;
    this.user = new UserResponse(displayName, profilePictureUrl);
  }

  public FriendshipUserResponse(String friendshipId, UserResponse userResponse) {
    this.friendshipId = friendshipId;
    this.user = userResponse;
  }

  public String getFriendshipId() {
    return friendshipId;
  }

  public void setFriendshipId(String friendshipId) {
    this.friendshipId = friendshipId;
  }

  public UserResponse getUser() {
    return user;
  }

  public void setUser(UserResponse userResponse) {
    this.user = userResponse;
  }
}
