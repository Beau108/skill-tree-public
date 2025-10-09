package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.FriendshipRepository;
import com.bproj.skilltree.dao.UserRepository;
import com.bproj.skilltree.dto.FriendList;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.mapper.FriendshipMapper;
import com.bproj.skilltree.model.FriendRequestStatus;
import com.bproj.skilltree.model.Friendship;
import com.bproj.skilltree.model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;



/**
 * Implements business logic for 'friends' collection.
 */
@Service
public class FriendshipService {
  private final FriendshipRepository friendshipRepository;
  private final UserRepository userRepository;

  @Autowired
  public FriendshipService(
      @Qualifier("mongoFriendshipRepository") FriendshipRepository friendshipRepository,
      @Qualifier("mongoUserRepository") UserRepository userRepository) {
    this.friendshipRepository = friendshipRepository;
    this.userRepository = userRepository;
  }

  /**
   * Validates a Friend. requesterId and addresseeId must point to two different, existing users. No
   * duplicate sets of requester and addressee.
   *
   * @param friend The Friend to be validated
   */
  private void validateFriendship(Friendship friend) {
    ObjectId requesterId = friend.getRequesterId();
    ObjectId addresseeId = friend.getAddresseeId();

    // can't be friends with yourself
    if (requesterId.equals(addresseeId)) {
      throw new BadRequestException("No friend relationships with yourself.");
    }

    // both users exist
    if (!userRepository.existsById(requesterId)) {
      throw new BadRequestException("requesterId must reference an existing user.");
    }
    if (!userRepository.existsById(addresseeId)) {
      throw new BadRequestException("addresseeId must reference an existing user.");
    }

    // this friendship doesnt already exist
    if (friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId, addresseeId)) {
      Friendship f =
          friendshipRepository.findByRequesterIdAndAddresseeId(requesterId, addresseeId).get();
      if (!f.getId().equals(friend.getId())) {
        throw new BadRequestException("No duplicate friend requests.");
      }
    }
    if (friendshipRepository.existsByRequesterIdAndAddresseeId(addresseeId, requesterId)) {
      Friendship f =
          friendshipRepository.findByRequesterIdAndAddresseeId(addresseeId, requesterId).get();
      if (!f.getId().equals(friend.getId())) {
        throw new BadRequestException("No duplicate friend requests.");
      }
    }
  }

  private boolean friendshipExists(ObjectId user1, ObjectId user2) {
    Optional<Friendship> check1 =
        friendshipRepository.findByRequesterIdAndAddresseeId(user1, user2);
    Optional<Friendship> check2 =
        friendshipRepository.findByRequesterIdAndAddresseeId(user2, user1);
    return check1.isPresent() || check2.isPresent();
  }

  /**
   * Send a friend request given the id of the sender and display name of the addressee.
   *
   * @param userId The Id of the user sending the friend request
   * @param displayName The display name of the user receiving the friend request
   * @return The new Friendship relationship
   */
  public Friendship addFriend(ObjectId userId, String displayName) {
    ObjectId friendId = userRepository.findByDisplayName(displayName)
        .orElseThrow(() -> new NotFoundException(Map.of("displayName", displayName))).getId();
    if (friendshipExists(userId, friendId)) {
      throw new BadRequestException("There is already a friend request for " + displayName);
    }
    Friendship friendship = new Friendship();
    friendship.setAddresseeId(friendId);
    friendship.setRequesterId(userId);
    friendship.setStatus(FriendRequestStatus.PENDING);
    validateFriendship(friendship);
    friendshipRepository.insert(friendship);
    return friendship;
  }

  public boolean existsById(ObjectId friendId) {
    return friendshipRepository.existsById(friendId);
  }

  public boolean existsByRequesterIdAndAddresseeId(ObjectId requesterId, ObjectId addresseeId) {
    return friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId, addresseeId);
  }

  /**
   * Finds a Friend by Id.
   *
   * @param friendId The Id of the desired Friend
   * @return The Friend. Throws NFE otherwise.
   */
  public Friendship findById(ObjectId friendId) {
    Optional<Friendship> optionalFriend = friendshipRepository.findById(friendId);
    if (optionalFriend.isEmpty()) {
      throw new NotFoundException(Map.of("friendId", friendId.toString()));
    }
    return optionalFriend.get();
  }

  /**
   * Find a Friend given the sender and receiver Ids.
   *
   * @param requesterId The sender Id
   * @param addresseeId The receiver Id
   * @return The Friend between the two Users. Throws NFE otherwise.
   */
  public Friendship findByRequesterIdAndAddresseeId(ObjectId requesterId, ObjectId addresseeId) {
    Optional<Friendship> optionalFriend =
        friendshipRepository.findByRequesterIdAndAddresseeId(requesterId, addresseeId);
    if (optionalFriend.isEmpty()) {
      throw new NotFoundException(
          Map.of("requesterId", requesterId.toString(), "addresseeId", addresseeId.toString()));
    }
    return optionalFriend.get();
  }

  public List<Friendship> findAll() {
    return friendshipRepository.findAll();
  }

  /**
   * Returns all of a User's Friends.
   *
   * @param userId The Id of the User
   * @return List of Friends.
   */
  public List<Friendship> findByUserId(ObjectId userId) {
    List<Friendship> res = new ArrayList<>();
    res.addAll(friendshipRepository.findByRequesterIdOrAddresseeId(userId, userId));
    return res;
  }

  /**
   * Returns all of a User's Friends. Supports optional query parameter 'status'
   *
   * @param userId The Id of the User
   * @param status The status of the returned Friends.
   * @return A list of Friends of the User with Status=status (if status was provided)
   */
  public List<Friendship> findByUserId(ObjectId userId, FriendRequestStatus status) {
    List<Friendship> friends = findByUserId(userId);
    if (status == null) {
      return friends;
    }
    return friendshipRepository.findByRequesterIdOrAddresseeIdAndStatus(userId, userId, status);
  }

  /**
   * Returns the Ids of all friends of the given User where status=ACCEPTED.
   *
   * @param userId The Id of the User
   * @return List of Ids belonging to that User's friends.
   */
  public List<ObjectId> getFriendIds(ObjectId userId) {
    List<Friendship> accepted = friendshipRepository.findByRequesterIdOrAddresseeIdAndStatus(userId,
        userId, FriendRequestStatus.ACCEPTED);
    List<ObjectId> friendIds = new ArrayList<ObjectId>();
    for (Friendship a : accepted) {
      if (!a.getAddresseeId().equals(userId)) {
        friendIds.add(a.getAddresseeId());
      } else {
        friendIds.add(a.getRequesterId());
      }
    }
    return friendIds;
  }

  /**
   * See whether or not two Users are friends.
   *
   * @param user1 The first user
   * @param user2 The seconds user
   * @return True if the users are friends, false otherwise.
   */
  public boolean areFriends(ObjectId user1, ObjectId user2) {
    return friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(user1, user2,
        FriendRequestStatus.ACCEPTED)
        || friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(user2, user1,
            FriendRequestStatus.ACCEPTED);
  }

  /**
   * Find the Friend relationship between two users.
   *
   * @param user1 The first user
   * @param user2 The second user
   * @return The Friend relationship between the two users. Throws NFE otherwise.
   */
  public Friendship findPair(ObjectId user1, ObjectId user2) {
    Optional<Friendship> sender1 =
        friendshipRepository.findByRequesterIdAndAddresseeId(user1, user2);
    Optional<Friendship> sender2 =
        friendshipRepository.findByRequesterIdAndAddresseeId(user2, user1);

    if (sender1.isPresent()) {
      return sender1.get();
    } else if (sender2.isPresent()) {
      return sender2.get();
    } else {
      Map<String, String> query =
          Map.of("friendId1", user1.toString(), "friendId2", user2.toString());
      throw new NotFoundException(query);
    }
  }

  public Friendship update(Friendship updatedFriend) {
    validateFriendship(updatedFriend);
    return friendshipRepository.save(updatedFriend);
  }

  private List<User> getOtherUsers(ObjectId userId, List<Friendship> friendships) {
    List<ObjectId> friendIds = friendships.stream().map(f -> {
      if (f.getAddresseeId().equals(userId)) {
        return f.getRequesterId();
      } else if (f.getRequesterId().equals(userId)) {
        return f.getAddresseeId();
      }
      throw new IllegalStateException(String.format("Friendship %s does not contain user %s",
          f.getId().toString(), userId.toString()));
    }).toList();
    return userRepository.findByIdIn(friendIds);
  }

  /**
   * Return the given User's FriendList DTO.
   *
   * @param userId The Id of the User
   * @return The FriendList for the provided User
   */
  public FriendList getFriendList(ObjectId userId) {
    List<Friendship> friendships =
        friendshipRepository.findByRequesterIdOrAddresseeId(userId, userId);
    List<User> otherUsers = getOtherUsers(userId, friendships);
    return FriendshipMapper.friendList(userId, otherUsers, friendships);
  }

  /**
   * Determines whether a user is allowed to change the status of a friendship.
   *
   * @param userId The ID of the user attempting the change.
   * @param friendship The Friendship entity being updated.
   * @param newStatus The desired new status.
   * @return true if the change is valid; false otherwise.
   */
  private boolean validChange(ObjectId userId, Friendship friendship,
      FriendRequestStatus newStatus) {
    FriendRequestStatus current = friendship.getStatus();
    if (current == FriendRequestStatus.BLOCKED) {
      return false;
    }

    if (current == newStatus) {
      return false;
    }

    if (current == FriendRequestStatus.PENDING) {
      if (newStatus == FriendRequestStatus.ACCEPTED) {
        return friendship.getAddresseeId().equals(userId);
      }
      if (newStatus == FriendRequestStatus.BLOCKED) {
        return true;
      }
      return false;
    }

    if (current == FriendRequestStatus.ACCEPTED) {
      if (newStatus == FriendRequestStatus.BLOCKED) {
        return true;
      }
      return false;
    }
    return false;
  }

  /**
   * Change the status of a Friendship relationship.
   *
   * @param userId The User performing the status change
   * @param friendshipId The Id of the Friendship relationship
   * @param status The new status of the Friendship
   * @return The updated Friendship
   */
  public Friendship changeStatus(ObjectId userId, ObjectId friendshipId,
      FriendRequestStatus status) {
    Friendship friendship =
        friendshipRepository.findByRequesterIdOrAddresseeIdAndId(userId, userId, friendshipId)
            .orElseThrow(() -> new NotFoundException(
                Map.of("userId", userId.toString(), "friendshipId", friendshipId.toString())));
    if (!validChange(userId, friendship, status)) {
      throw new BadRequestException("Invalid friendship status change.");
    }

    friendship.setStatus(status);
    friendship.setUpdatedAt(Instant.now());
    validateFriendship(friendship);
    return friendshipRepository.save(friendship);
  }

  public void deleteById(ObjectId friendId) {
    friendshipRepository.deleteById(friendId);
  }

  public void deleteByUserIdAndId(ObjectId userId, ObjectId friendshipId) {
    friendshipRepository.deleteByRequesterIdOrAddresseeIdAndId(userId, userId, friendshipId);
  }
}
