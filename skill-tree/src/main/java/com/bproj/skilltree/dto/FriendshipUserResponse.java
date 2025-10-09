package com.bproj.skilltree.dto;

import org.bson.types.ObjectId;

/**
 * UserResponse wrapper class containing a Friendship Id.
 */
public class FriendshipUserResponse {
  private ObjectId friendshipId;
  private UserResponse user;

  public FriendshipUserResponse(ObjectId friendshipId, String displayName,
      String profilePictureUrl) {
    this.friendshipId = friendshipId;
    this.user = new UserResponse(displayName, profilePictureUrl);
  }

  public ObjectId getFriendshipId() {
    return friendshipId;
  }

  public void setFriendshipId(ObjectId friendshipId) {
    this.friendshipId = friendshipId;
  }

  public UserResponse getUser() {
    return user;
  }

  public void setUser(UserResponse userResponse) {
    this.user = userResponse;
  }
}
