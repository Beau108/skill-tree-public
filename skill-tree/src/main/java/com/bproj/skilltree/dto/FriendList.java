package com.bproj.skilltree.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO meant to represent all of the User's friend requests - ACCEPTED, PENDING, and BLOCKED.
 * Friends are represented by UserResponses.
 */
public class FriendList {
  private List<UserResponse> incoming;
  private List<UserResponse> outgoing;
  private List<UserResponse> friends;
  private List<UserResponse> blocked;

  /**
   * Explicit value constructor.
   *
   * @param incoming List of incoming friend requests
   * @param outgoing List of outgoing friend requests
   * @param friends List of accepted friends
   * @param blocked List of blocked users
   */
  public FriendList(List<UserResponse> incoming, List<UserResponse> outgoing,
      List<UserResponse> friends, List<UserResponse> blocked) {
    this.incoming = incoming;
    this.outgoing = outgoing;
    this.friends = friends;
    this.blocked = blocked;
  }

  /**
   * Default constructor initializing empty lists.
   */
  public FriendList() {
    this.incoming = new ArrayList<UserResponse>();
    this.outgoing = new ArrayList<UserResponse>();
    this.friends = new ArrayList<UserResponse>();
    this.blocked = new ArrayList<UserResponse>();
  }

  public List<UserResponse> getIncoming() {
    return incoming;
  }

  public void setIncoming(List<UserResponse> incoming) {
    this.incoming = incoming;
  }

  public List<UserResponse> getOutgoing() {
    return outgoing;
  }

  public void setOutgoing(List<UserResponse> outgoing) {
    this.outgoing = outgoing;
  }

  public List<UserResponse> getFriends() {
    return friends;
  }

  public void setFriends(List<UserResponse> friends) {
    this.friends = friends;
  }

  public List<UserResponse> getBlocked() {
    return blocked;
  }

  public void setBlocked(List<UserResponse> blocked) {
    this.blocked = blocked;
  }
}
