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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implements business logic for 'friends' collection.
 */
@Service
public class FriendshipService {
  private static final Logger logger = LoggerFactory.getLogger(FriendshipService.class);
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
      throw new BadRequestException("Cannot create friend relationship with yourself.");
    }

    // both users exist
    if (!userRepository.existsById(requesterId)) {
      throw new BadRequestException("Requester must reference an existing user.");
    }
    if (!userRepository.existsById(addresseeId)) {
      throw new BadRequestException("Addressee must reference an existing user.");
    }

    // this friendship doesnt already exist
    if (friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId, addresseeId)) {
      Friendship f =
          friendshipRepository.findByRequesterIdAndAddresseeId(requesterId, addresseeId).get();
      if (!f.getId().equals(friend.getId())) {
        throw new BadRequestException("Friend request already exists.");
      }
    }
    if (friendshipRepository.existsByRequesterIdAndAddresseeId(addresseeId, requesterId)) {
      Friendship f =
          friendshipRepository.findByRequesterIdAndAddresseeId(addresseeId, requesterId).get();
      if (!f.getId().equals(friend.getId())) {
        throw new BadRequestException("Friend request already exists.");
      }
    }
  }

  private boolean doesFriendshipExist(ObjectId user1, ObjectId user2) {
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
    logger.info("addFriend(userId={}, displayName={})", userId, displayName);
    logger.info("userRepository.findByDisplayName(displayName={})", displayName);
    ObjectId friendId = userRepository.findByDisplayName(displayName)
        .orElseThrow(() -> new NotFoundException("users", Map.of("displayName", displayName)))
        .getId();
    if (doesFriendshipExist(userId, friendId)) {
      throw new BadRequestException("Friend request already exists.");
    }
    Friendship friendship = new Friendship();
    friendship.setAddresseeId(friendId);
    friendship.setRequesterId(userId);
    friendship.setStatus(FriendRequestStatus.PENDING);
    validateFriendship(friendship);
    logger.info("friendshipRepository.insert(friendship={})", friendship);
    friendshipRepository.insert(friendship);
    return friendship;
  }

  public boolean existsById(ObjectId friendId) {
    logger.info("existsById(friendId={})", friendId);
    logger.info("friendshipRepository.existsById(friendId={})", friendId);
    return friendshipRepository.existsById(friendId);
  }

  public boolean existsByRequesterIdAndAddresseeId(ObjectId requesterId, ObjectId addresseeId) {
    logger.info("existsByRequesterIdAndAddresseeId(requesterId={}, addresseeId={})", requesterId, addresseeId);
    logger.info("friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId={}, addresseeId={})", requesterId, addresseeId);
    return friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId, addresseeId);
  }

  /**
   * Finds a Friend by Id.
   *
   * @param friendId The Id of the desired Friend
   * @return The Friend. Throws NFE otherwise.
   */
  public Friendship findById(ObjectId friendId) {
    logger.info("findById(friendId={})", friendId);
    logger.info("friendshipRepository.findById(friendId={})", friendId);
    Optional<Friendship> optionalFriend = friendshipRepository.findById(friendId);
    if (optionalFriend.isEmpty()) {
      throw new NotFoundException("friendships", Map.of("friendshipId", friendId.toString()));
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
    logger.info("findByRequesterIdAndAddresseeId(requesterId={}, addresseeId={})", requesterId, addresseeId);
    logger.info("friendshipRepository.findByRequesterIdAndAddresseeId(requesterId={}, addresseeId={})", requesterId, addresseeId);
    return friendshipRepository.findByRequesterIdAndAddresseeId(requesterId, addresseeId)
        .orElseThrow(() -> new NotFoundException("friendships",
            Map.of("requesterId", requesterId.toString(), "addresseeId", addresseeId.toString())));
  }

  public List<Friendship> findAll() {
    logger.info("findAll()");
    logger.info("friendshipRepository.findAll()");
    return friendshipRepository.findAll();
  }

  /**
   * Returns all of a User's Friends.
   *
   * @param userId The Id of the User
   * @return List of Friends.
   */
  public List<Friendship> findByUserId(ObjectId userId) {
    logger.info("findByUserId(userId={})", userId);
    logger.info("friendshipRepository.findByRequesterIdOrAddresseeId(userId={}, userId={})", userId, userId);
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
    logger.info("findByUserId(userId={}, status={})", userId, status);
    List<Friendship> friends = findByUserId(userId);
    if (status == null) {
      return friends;
    }
    logger.info("friendshipRepository.findByRequesterIdOrAddresseeIdAndStatus(userId={}, userId={}, status={})", userId, userId, status);
    return friendshipRepository.findByRequesterIdOrAddresseeIdAndStatus(userId, userId, status);
  }

  /**
   * Returns the Ids of all friends of the given User where status=ACCEPTED.
   *
   * @param userId The Id of the User
   * @return List of Ids belonging to that User's friends.
   */
  public List<ObjectId> getFriendIds(ObjectId userId) {
    logger.info("getFriendIds(userId={})", userId);
    logger.info("friendshipRepository.findByRequesterIdOrAddresseeIdAndStatus(userId={}, userId={}, status=ACCEPTED)", userId, userId);
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
    logger.info("areFriends(user1={}, user2={})", user1, user2);
    logger.info("friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(user1={}, user2={}, status=ACCEPTED)", user1, user2);
    logger.info("friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(user2={}, user1={}, status=ACCEPTED)", user2, user1);
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
  public Friendship findFriendshipByUserPair(ObjectId user1, ObjectId user2) {
    logger.info("findFriendshipByUserPair(user1={}, user2={})", user1, user2);
    logger.info("friendshipRepository.findByRequesterIdAndAddresseeId(user1={}, user2={})", user1, user2);
    Optional<Friendship> sender1 =
        friendshipRepository.findByRequesterIdAndAddresseeId(user1, user2);
    logger.info("friendshipRepository.findByRequesterIdAndAddresseeId(user2={}, user1={})", user2, user1);
    Optional<Friendship> sender2 =
        friendshipRepository.findByRequesterIdAndAddresseeId(user2, user1);

    if (sender1.isPresent()) {
      return sender1.get();
    } else if (sender2.isPresent()) {
      return sender2.get();
    } else {
      throw new NotFoundException("friendships",
          Map.of("user1", user1.toString(), "user2", user2.toString()));
    }
  }

  private List<User> getOtherUsersInFrienships(ObjectId userId, List<Friendship> friendships) {
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
    logger.info("getFriendList(userId={})", userId);
    logger.info("friendshipRepository.findByRequesterIdOrAddresseeId(userId={}, userId={})", userId, userId);
    List<Friendship> friendships =
        friendshipRepository.findByRequesterIdOrAddresseeId(userId, userId);
    List<User> otherUsers = getOtherUsersInFrienships(userId, friendships);
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
  private boolean isValidStatusChange(ObjectId userId, Friendship friendship,
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
    logger.info("changeStatus(userId={}, friendshipId={}, status={})", userId, friendshipId, status);
    logger.info("friendshipRepository.findByRequesterIdOrAddresseeIdAndId(userId={}, userId={}, friendshipId={})", userId, userId, friendshipId);
    Friendship friendship =
        friendshipRepository.findByRequesterIdOrAddresseeIdAndId(userId, userId, friendshipId)
            .orElseThrow(() -> new NotFoundException("friendships",
                Map.of("userId", userId.toString(), "friendshipId", friendshipId.toString())));
    if (!isValidStatusChange(userId, friendship, status)) {
      throw new BadRequestException("Invalid friendship status change.");
    }

    friendship.setStatus(status);
    validateFriendship(friendship);
    logger.info("friendshipRepository.save(friendship={})", friendship);
    return friendshipRepository.save(friendship);
  }

  public void deleteById(ObjectId friendId) {
    logger.info("deleteById(friendId={})", friendId);
    logger.info("friendshipRepository.deleteById(friendId={})", friendId);
    friendshipRepository.deleteById(friendId);
  }

  public void deleteByUserIdAndId(ObjectId userId, ObjectId friendshipId) {
    logger.info("deleteByUserIdAndId(userId={}, friendshipId={})", userId, friendshipId);
    logger.info("friendshipRepository.deleteByRequesterIdOrAddresseeIdAndId(userId={}, userId={}, friendshipId={})", userId, userId, friendshipId);
    friendshipRepository.deleteByRequesterIdOrAddresseeIdAndId(userId, userId, friendshipId);
  }
}
