package com.bproj.skilltree.mapper;

import com.bproj.skilltree.dto.FriendList;
import com.bproj.skilltree.dto.FriendshipUserResponse;
import com.bproj.skilltree.dto.LeaderboardEntry;
import com.bproj.skilltree.dto.UserResponse;
import com.bproj.skilltree.model.Achievement;
import com.bproj.skilltree.model.Activity;
import com.bproj.skilltree.model.Friendship;
import com.bproj.skilltree.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

/**
 * DTO conversion for Friends.
 */
public class FriendshipMapper {
  private FriendshipMapper() {}

  /**
   * Get the ACCEPTED, PENDING, and BLOCKED friend requests for the provided user.
   *
   * @param userId The Id of the target user
   * @param users The Users the User has friend requests with
   * @param friendRequests The requests the User is involved in
   * @return The grouped list of Friends (FriendList DTO)
   */
  public static FriendList friendList(ObjectId userId, List<User> users,
      List<Friendship> friendRequests) {
    if (users == null || friendRequests == null) {
      return new FriendList();
    }
    Map<ObjectId, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
    FriendList fl = new FriendList();
    ArrayList<FriendshipUserResponse> inbox = new ArrayList<>();
    ArrayList<FriendshipUserResponse> outbox = new ArrayList<>();
    ArrayList<FriendshipUserResponse> friendedUsers = new ArrayList<>();
    ArrayList<FriendshipUserResponse> blockedUsers = new ArrayList<>();

    for (Friendship f : friendRequests) {
      User friend = null;
      boolean incoming = false;
      if (f.getAddresseeId().equals(userId)) {
        friend = userMap.get(f.getRequesterId());
        incoming = true;
      } else if (f.getRequesterId().equals(userId)) {
        friend = userMap.get(f.getAddresseeId());
        incoming = false;
      } else {
        throw new IllegalStateException("userId {" + userId.toString()
            + "} was not found in one of the provided Friend objects.");
      }
      FriendshipUserResponse ur = new FriendshipUserResponse(f.getId().toString(),
          new UserResponse(friend.getDisplayName(), friend.getProfilePictureUrl()));
      switch (f.getStatus()) {
        case ACCEPTED:
          friendedUsers.add(ur);
          break;
        case PENDING:
          if (incoming) {
            inbox.add(ur);
          } else {
            outbox.add(ur);
          }
          break;
        case BLOCKED:
          blockedUsers.add(ur);
          break;
        default:
          throw new IllegalStateException(
              "Friend {" + f.getId() + "} doesn't have a proper status: " + f.getStatus());
      }
    }
    fl.setFriends(friendedUsers);
    fl.setBlocked(blockedUsers);
    fl.setIncoming(inbox);
    fl.setOutgoing(outbox);
    return fl;
  }

  /**
   * Return a LeaderboardEntry DTO given a User and their Achievements/Activities.
   *
   * @param user The target User
   * @param achievements The User's Achievements
   * @param activities The User's Activities
   * @return The User's LeaderboardEntry
   */
  public static LeaderboardEntry leaderboardEntry(User user, List<Achievement> achievements,
      List<Activity> activities) {
    if (user == null || achievements == null || activities == null) {
      return null;
    }

    double timeLogged = activities.stream().mapToDouble(Activity::getDuration).sum();

    int achievementsCompleted = (int) achievements.stream().filter(a -> a.isComplete()).count();

    return new LeaderboardEntry(user.getDisplayName(), user.getProfilePictureUrl(), timeLogged,
        achievementsCompleted);
  }
}
