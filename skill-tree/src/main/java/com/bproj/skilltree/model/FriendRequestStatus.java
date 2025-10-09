package com.bproj.skilltree.model;

/**
 * The status of a friend request.
 */
public enum FriendRequestStatus {
  PENDING, ACCEPTED, BLOCKED;

  /**
   * Create a FriendRequestStatus from a string. 
   *
   * @param s   The String used to generate the FriendRequestStatus
   * @return    The status derived from the String. Throws if unrecognized.
   */
  public static FriendRequestStatus fromString(String s) {
    String lowercase = s.toLowerCase();
    if (lowercase.equals("pending")) {
      return FriendRequestStatus.PENDING;
    } else if (lowercase.equals("accepted")) {
      return FriendRequestStatus.ACCEPTED;
    } else if (lowercase.equals("blocked")) {
      return FriendRequestStatus.BLOCKED;
    }
    throw new IllegalArgumentException("Invalid friend request status: " + s);
  }
}
