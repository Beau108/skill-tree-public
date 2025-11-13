package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.*;
import com.bproj.skilltree.dto.FeedItem;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.mapper.AchievementMapper;
import com.bproj.skilltree.mapper.ActivityMapper;
import com.bproj.skilltree.mapper.TreeMapper;
import com.bproj.skilltree.model.*;
import com.bproj.skilltree.util.PatchUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implements business logic for 'users' collection.
 */
@Service
public class UserService {
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;
  private final AchievementRepository achievementRepository;
  private final ActivityRepository activityRepository;
  private final TreeRepository treeRepository;
  private final SkillRepository skillRepository;
  private final OrientationRepository orientationRepository;
  private final FriendshipRepository friendshipRepository;


  /**
   * Create a UserService object.
   *
   * @param userRepository DB ops for Users
   * @param achievementRepository DB ops for achs
   * @param activityRepository DB ops for activities
   * @param treeRepository DB ops for trees
   */
  @Autowired
  public UserService(@Qualifier("mongoUserRepository") UserRepository userRepository,
      @Qualifier("mongoAchievementRepository") AchievementRepository achievementRepository,
      @Qualifier("mongoActivityRepository") ActivityRepository activityRepository,
      @Qualifier("mongoTreeRepository") TreeRepository treeRepository,
      @Qualifier("mongoSkillRepository") SkillRepository skillRepository,
      @Qualifier("mongoOrientationRepository") OrientationRepository orientationRepository,
      @Qualifier("mongoFriendshipRepository") FriendshipRepository friendshipRepository) {
    this.userRepository = userRepository;
    this.achievementRepository = achievementRepository;
    this.activityRepository = activityRepository;
    this.treeRepository = treeRepository;
    this.skillRepository = skillRepository;
    this.orientationRepository = orientationRepository;
    this.friendshipRepository = friendshipRepository;
  }

  /**
   * Validates a User. Display name and email must be unique.
   *
   * @param user The User to be validated
   */
  private void validateUser(User user) {
    // firebaseId
    if (user.getFirebaseId() == null || user.getFirebaseId().isBlank()) {
      throw new BadRequestException("User must have a Firebase ID.");
    }

    if (user.getEmail() == null || user.getEmail().isBlank()) {
        throw new BadRequestException("User must have an email.");
    }

    // displayName uniqueness
    Optional<User> optionalUser = userRepository.findByDisplayName(user.getDisplayName());
    if (optionalUser.isPresent() && !optionalUser.get().getId().equals(user.getId())) {
      throw new BadRequestException("Display name is already taken.");
    }
  }

  /**
   * Create a user given a UserRequest DTO, firebaseId, and email.
   *
   * @param user the User to be created
   * @return The created User
   */
  public User create(User user, String firebaseId, String email) {
    logger.info("create(user={}, firebaseId={}, email={})", user, firebaseId, email);
    user.setFirebaseId(firebaseId);
    user.setEmail(email);
    validateUser(user);
    Instant now = Instant.now();
    user.setCreatedAt(now);
    user.setUpdatedAt(now);
    user.setId(new ObjectId());
    
    logger.info("userRepository.insert(user={})", user);
    return userRepository.insert(user);
  }

  public boolean existsById(ObjectId userId) {
    logger.info("existsById(userId={})", userId);
    logger.info("userRepository.existsById(userId={})", userId);
    return userRepository.existsById(userId);
  }

  public boolean existsByFirebaseId(String firebaseId) {
    logger.info("existsByFirebaseId(firebaseId={})", firebaseId);
    logger.info("userRepository.existsByFirebaseId(firebaseId={})", firebaseId);
    return userRepository.existsByFirebaseId(firebaseId);
  }

  /**
   * Find a user by the specified Id.
   *
   * @param userId The Id of the desired User.
   * @return The User with id=Id. Throws NFE otherwise.
   */
  public User findById(ObjectId userId) {
    logger.info("findById(userId={})", userId);
    logger.info("userRepository.findById(userId={})", userId);
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("users", Map.of("userId", userId.toString())));
  }

  /**
   * Given a list of userIds, returns a list of associated Users.
   *
   * @param userIds The List of userIds for lookup
   * @return A list of Users with Ids matching those in the provided list
   */
  public List<User> findByIds(List<ObjectId> userIds) {
    logger.info("findByIds(userIds={})", userIds);
    logger.info("userRepository.findAllById(userIds={})", userIds);
    List<User> users = userRepository.findAllById(userIds);
    if (users.size() != userIds.size()) {
      throw new NotFoundException("users", Map.of("userIds", userIds.toString()));
    }
    return users;
  }

  public User findByFirebaseId(String firebaseId) {
    logger.info("findByFirebaseId(firebaseId={})", firebaseId);
    logger.info("userRepository.findByFirebaseId(firebaseId={})", firebaseId);
    return userRepository.findByFirebaseId(firebaseId)
        .orElseThrow(() -> new NotFoundException("users", Map.of("firebaseId", firebaseId)));
  }

  public User findByEmail(String email) {
    logger.info("findByEmail(email={})", email);
    logger.info("userRepository.findByEmail(email={})", email);
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("users", Map.of("email", email)));
  }

  public List<User> findAll() {
    logger.info("findAll()");
    logger.info("userRepository.findAll()");
    return userRepository.findAll();
  }

  /**
   * Should never be called by public end points. Admin only.
   *
   * @param updatedUser The updated User
   * @return The persisted updated User
   */
  public User update(ObjectId userId, User updatedUser) {
    logger.info("update(userId={}, updatedUser={})", userId, updatedUser);
    logger.info("userRepository.findById(userId={})", userId);
    User existingUser = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("users", Map.of("userId", userId.toString())));
    updatedUser.setId(userId);
    updatedUser.setFirebaseId(existingUser.getFirebaseId());
    updatedUser.setEmail(existingUser.getEmail());
    validateUser(updatedUser);
    logger.info("userRepository.save(updatedUser={})", updatedUser);
    return userRepository.save(updatedUser);
  }

  /**
   * Partially update a User.
   *
   * @param userId The Id of the User to be updated
   * @param updates The updates to be applied to the User
   * @return The updated User. Throws NFE otherwise.
   */
  public User patch(ObjectId userId, Map<String, Object> updates) {
    logger.info("patch(userId={}, updates={})", userId, updates);
    logger.info("userRepository.findById(userId={})", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("users", Map.of("userId", userId.toString())));
    User updated = PatchUtils.applyUserPatch(user, updates);
    validateUser(updated);
    logger.info("userRepository.save(updated={})", updated);
    return userRepository.save(updated);
  }

  private FeedItem convertToFeedItem(Object obj, User user) {
    if (obj instanceof Achievement achievement) {
      return AchievementMapper.toAchievementFeedItem(achievement, user);
    } else if (obj instanceof Activity activity) {
      List<Skill> skills = skillRepository
          .findByIdIn(activity.getSkillWeights().stream().map(SkillWeight::getSkillId).toList());
      return ActivityMapper.toActivityFeedItem(activity, skills, user);
    } else if (obj instanceof Tree tree) {
      return TreeMapper.toTreeFeedItem(tree, user);
    } else {
      throw new IllegalArgumentException("Unknown feed item type: " + obj.getClass());
    }
  }

  /**
   * Return an paginated actions feed for the User associated with the provided userId.
   *
   * @param userId The Id of the User
   * @param page The page number of the page to be returned
   * @param size The size of each page
   * @return The Paginated actions feed
   */
  public Page<FeedItem> getUserActionsFeed(ObjectId userId, int page, int size) {
    logger.info("getUserActivityFeed(userId={}, page={}, size={})", userId, page, size);
    logger.info("userRepository.findById(userId={})", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("users", Map.of("userId", userId.toString())));
    logger.info("achievementRepository.findByUserId(userId={})", userId);
    logger.info("activityRepository.findByUserId(userId={})", userId);
    logger.info("treeRepository.findByUserId(userId={})", userId);
    List<FeedItem> combined = Stream
        .of(achievementRepository.findByUserId(userId), activityRepository.findByUserId(userId),
            treeRepository.findByUserId(userId))
        .flatMap(List::stream).map(obj -> convertToFeedItem(obj, user))
        .sorted(Comparator.comparing(FeedItem::getPostedAt).reversed()).toList();
    Pageable pageable = PageRequest.of(page, size);
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), combined.size());
    return new PageImpl<>(combined.subList(start, end), pageable, combined.size());
  }

  /**
   * Delete a User by their Id. Removes all related entities as well.
   *
   * @param userId The Id of the User to be deleted
   */
  @Transactional
  public void deleteById(ObjectId userId) {
    logger.info("deleteById(userId={})", userId);
    logger.info("activityRepository.deleteByUserId(userId={})", userId);
    activityRepository.deleteByUserId(userId);
    logger.info("friendshipRepository.deleteByRequesterIdOrAddresseeId(userId={}, userId={})",
        userId, userId);
    friendshipRepository.deleteByRequesterIdOrAddresseeId(userId, userId);
    logger.info("skillRepository.deleteByUserId(userId={})", userId);
    skillRepository.deleteByUserId(userId);
    logger.info("achievementRepository.deleteByUserId(userId={})", userId);
    achievementRepository.deleteByUserId(userId);
    logger.info("orientationRepository.deleteByUserId(userId={})", userId);
    orientationRepository.deleteByUserId(userId);
    logger.info("treeRepository.deleteByUserId(userId={})", userId);
    treeRepository.deleteByUserId(userId);
    logger.info("userRepository.deleteById(userId={})", userId);
    userRepository.deleteById(userId);
  }
}
