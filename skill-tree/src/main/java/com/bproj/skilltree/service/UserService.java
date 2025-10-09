package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.AchievementRepository;
import com.bproj.skilltree.dao.ActivityRepository;
import com.bproj.skilltree.dao.SkillRepository;
import com.bproj.skilltree.dao.TreeRepository;
import com.bproj.skilltree.dao.UserRepository;
import com.bproj.skilltree.dto.FeedItem;
import com.bproj.skilltree.dto.UserResponse;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.mapper.AchievementMapper;
import com.bproj.skilltree.mapper.ActivityMapper;
import com.bproj.skilltree.mapper.TreeMapper;
import com.bproj.skilltree.mapper.UserMapper;
import com.bproj.skilltree.model.Achievement;
import com.bproj.skilltree.model.Activity;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.model.User;
import com.bproj.skilltree.util.JsonMergePatchUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implements business logic for 'users' collection.
 */
@Service
public class UserService {
  private final UserRepository userRepository;
  private final AchievementRepository achievementRepository;
  private final ActivityRepository activityRepository;
  private final TreeRepository treeRepository;
  private final SkillRepository skillRepository;

  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
  private static final String DISPLAY_NAME_REGEX = "^[A-Za-z0-9._]{3,15}$";
  private static final String URL_REGEX = "^(https?://)?([a-zA-Z0-9.-]+\\.)?skilltree\\.com(/.*)?$";

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
      @Qualifier("mongoSkillRepository") SkillRepository skillRepository) {
    this.userRepository = userRepository;
    this.achievementRepository = achievementRepository;
    this.activityRepository = activityRepository;
    this.treeRepository = treeRepository;
    this.skillRepository = skillRepository;
  }

  /**
   * Validates a User. DisplayName must be unique. DisplayName must be 3-15 alphanumeric characters,
   * '.', or '_'. Email must be a proper email. URL must be under a skilltree domain.
   *
   * @param user The User to be validated
   */
  private void validateUser(User user) {
    // firebaseId
    if (user.getFirebaseId() == null || user.getFirebaseId().isBlank()) {
      throw new BadRequestException("User must have a Firebase ID.");
    }

    // displayName uniqueness
    Optional<User> optionalUser = userRepository.findByDisplayName(user.getDisplayName());
    if (optionalUser.isPresent() && !optionalUser.get().getId().equals(user.getId())) {
      throw new BadRequestException("The display name {" + user.getDisplayName() + "} is taken.");
    }

    // displayName format
    Pattern ptName = Pattern.compile(DISPLAY_NAME_REGEX);
    if (!ptName.matcher(user.getDisplayName()).matches()) {
      throw new BadRequestException("The display name {" + user.getDisplayName()
          + "} is invalid. Must be 3-15 alphanumeric characters, '.' and '_' allowed.");
    }

    // email uniqueness
    Optional<User> existingByEmail = userRepository.findByEmail(user.getEmail());
    if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(user.getId())) {
      throw new BadRequestException("Email " + user.getEmail() + " is already in use.");
    }

    // email format
    Pattern ptEmail = Pattern.compile(EMAIL_REGEX);
    if (!ptEmail.matcher(user.getEmail()).matches()) {
      throw new BadRequestException("Invalid email: " + user.getEmail());
    }

    // profile picture URL format (optional)
    if (user.getProfilePictureUrl() != null) {
      Pattern ptUrl = Pattern.compile(URL_REGEX);
      if (!ptUrl.matcher(user.getProfilePictureUrl()).matches()) {
        throw new BadRequestException(
            "Invalid profile picture URL: " + user.getProfilePictureUrl());
      }
    }
  }

  /**
   * Create a user given a UserRequest DTO, firebaseId, and email.
   *
   * @param user the User to be created
   * @return The created User
   */
  public UserResponse create(User user) {
    validateUser(user);
    return UserMapper.fromUser(userRepository.insert(user));
  }

  public boolean existsById(ObjectId userId) {
    return userRepository.existsById(userId);
  }

  public boolean existsByFirebaseId(String firebaseId) {
    return userRepository.existsByFirebaseId(firebaseId);
  }

  /**
   * Find a user by the specified Id.
   *
   * @param userId The Id of the desired User.
   * @return The User with id=Id. Throws NFE otherwise.
   */
  public User getEntityById(ObjectId userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(Map.of("userId", userId.toString())));
  }

  /**
   * Get a UserResponse by Id.
   *
   * @param userId The Id of the user
   * @return The UserResponse
   */
  public UserResponse getResponseById(ObjectId userId) {
    User user = getEntityById(userId);
    return UserMapper.fromUser(user);
  }

  /**
   * Given a list of userIds, returns a list of associated Users.
   *
   * @param userIds The List of userIds for lookup
   * @return A list of Users with Ids matching those in the provided list
   */
  public List<User> getEntitiesByIds(List<ObjectId> userIds) {
    List<User> users = userRepository.findAllById(userIds);
    if (users.size() != userIds.size()) {
      throw new NotFoundException(Map.of("userIds", userIds.toString()));
    }
    return users;
  }

  public List<UserResponse> getResponsesByIds(List<ObjectId> userIds) {
    return getEntitiesByIds(userIds).stream().map(UserMapper::fromUser).toList();
  }

  public User findByFirebaseId(String firebaseId) {
    return userRepository.findByFirebaseId(firebaseId)
        .orElseThrow(() -> new NotFoundException(Map.of("firebaseId", firebaseId)));
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException(Map.of("email", email)));
  }

  public List<User> findAllEntities() {
    return userRepository.findAll();
  }

  public List<UserResponse> findAllResponses() {
    return findAllEntities().stream().map(UserMapper::fromUser).toList();
  }

  /**
   * Should never be called by public end points. Admin only.
   *
   * @param updatedUser The updated User
   * @return The persisted updated User
   */
  public User update(ObjectId userId, User updatedUser) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException(Map.of("userId", userId.toString()));
    }
    updatedUser.setId(userId);
    validateUser(updatedUser);
    return userRepository.save(updatedUser);
  }

  /**
   * Update a User, providing Id separately.
   *
   * @param userId The Id of the User being updated
   * @param user The updated User information, missing id, firebaseId, and email
   * @return The updated User object
   */
  public UserResponse updateResponse(ObjectId userId, User user) {
    User existingUser = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(Map.of("userId", userId.toString())));
    user.setId(userId);
    user.setFirebaseId(existingUser.getFirebaseId());
    user.setEmail(existingUser.getEmail());
    validateUser(user);
    return UserMapper.fromUser(userRepository.save(user));
  }

  /**
   * Partially update a User.
   *
   * @param userId The Id of the User to be updated
   * @param updates The updates to be applied to the User
   * @return The updated User. Throws NFE otherwise.
   */
  public User patch(ObjectId userId, JsonMergePatch updates) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(Map.of("userId", userId.toString())));
    User updated = JsonMergePatchUtils.applyMergePatch(updates, user, User.class);
    validateUser(updated);
    return userRepository.save(updated);
  }

  /**
   * Partially update a User and return the UserResponse DTO.
   *
   * @param userId The Id of the User to be updated
   * @param updates The updates to be applied to the User
   * @return The updated UserResponse. Throws NFE otherwise.
   */
  public UserResponse patchResponse(ObjectId userId, JsonMergePatch updates) {
    return UserMapper.fromUser(patch(userId, updates));
  }

  private FeedItem toFeedItem(Object obj, User user) {
    if (obj instanceof Achievement achievement) {
      return AchievementMapper.toAchievementFeedItem(achievement, user);
    } else if (obj instanceof Activity activity) {
      List<Skill> skills = skillRepository
          .findByIdIn(activity.getSkillWeights().stream().map(sw -> sw.getSkillId()).toList());
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
  public Page<FeedItem> myActivityFeed(ObjectId userId, int page, int size) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(Map.of("userId", userId.toString())));
    List<FeedItem> combined = Stream
        .of(achievementRepository.findByUserId(userId), activityRepository.findByUserId(userId),
            treeRepository.findByUserId(userId))
        .flatMap(List::stream).map(obj -> toFeedItem(obj, user))
        .sorted(Comparator.comparing(FeedItem::getPostedAt).reversed()).toList();
    Pageable pageable = PageRequest.of(page, size);
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), combined.size());
    return new PageImpl<>(combined.subList(start, end), pageable, combined.size());
  }

  public void deleteById(ObjectId userId) {
    userRepository.deleteById(userId);
  }
}
