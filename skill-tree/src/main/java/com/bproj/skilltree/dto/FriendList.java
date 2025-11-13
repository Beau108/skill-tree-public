package com.bproj.skilltree.dto;

import java.util.List;

/**
 * DTO meant to represent all of the User's friend requests - ACCEPTED, PENDING, and BLOCKED.
 * Friends are represented by UserResponses.
 */
public class FriendList {
  private List<FriendshipUserResponse> incoming;
  private List<FriendshipUserResponse> outgoing;
  private List<FriendshipUserResponse> friends;
  private List<FriendshipUserResponse> blocked;

  /**
   * Explicit value constructor.
   *
   * @param incoming List of incoming friend requests
   * @param outgoing List of outgoing friend requests
   * @param friends List of accepted friends
   * @param blocked List of blocked users
   */
  public FriendList(List<FriendshipUserResponse> incoming, List<FriendshipUserResponse> outgoing,
      List<FriendshipUserResponse> friends, List<FriendshipUserResponse> blocked) {
    this.incoming = incoming;
    this.outgoing = outgoing;
    this.friends = friends;
    this.blocked = blocked;
  }

  /**
   * Empty constructor.
   */
  public FriendList() {
    this.incoming = List.of();
    this.outgoing = List.of();
    this.friends = List.of();
    this.blocked = List.of();
  }

  public List<FriendshipUserResponse> getIncoming() {
    return incoming;
  }

  public void setIncoming(List<FriendshipUserResponse> incoming) {
    this.incoming = incoming;
  }

  public List<FriendshipUserResponse> getOutgoing() {
    return outgoing;
  }

  public void setOutgoing(List<FriendshipUserResponse> outgoing) {
    this.outgoing = outgoing;
  }

  public List<FriendshipUserResponse> getFriends() {
    return friends;
  }

  public void setFriends(List<FriendshipUserResponse> friends) {
    this.friends = friends;
  }

  public List<FriendshipUserResponse> getBlocked() {
    return blocked;
  }

  public void setBlocked(List<FriendshipUserResponse> blocked) {
    this.blocked = blocked;
  }
}
