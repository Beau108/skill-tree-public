package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.AchievementRepository;
import com.bproj.skilltree.dao.OrientationRepository;
import com.bproj.skilltree.dao.TreeRepository;
import com.bproj.skilltree.dao.UserRepository;
import com.bproj.skilltree.dto.AchievementFeedItem;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.model.*;
import com.bproj.skilltree.util.PatchUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements for business logic for the 'achievements' collection.
 */
@Service
public class AchievementService {
  private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);
  private final AchievementRepository achievementRepository;
  private final UserRepository userRepository;
  private final TreeRepository treeRepository;
  private final OrientationRepository orientationRepository;


  /**
   * Create an AchievementService. user and tree repositories required for validation & query
   * support.
   *
   * @param achievementRepository DB operations for Achievements
   * @param userRepository DB operations for Users
   * @param treeRepository DB operations for Trees
   */
  @Autowired
  public AchievementService(
      @Qualifier("mongoAchievementRepository") AchievementRepository achievementRepository,
      @Qualifier("mongoUserRepository") UserRepository userRepository,
      @Qualifier("mongoTreeRepository") TreeRepository treeRepository,
      @Qualifier("mongoOrientationRepository") OrientationRepository orientationRepository) {
    this.achievementRepository = achievementRepository;
    this.userRepository = userRepository;
    this.treeRepository = treeRepository;
    this.orientationRepository = orientationRepository;
  }

  /**
   * Validates an Achievement. userId must reference an existing user. treeId must reference an
   * existing tree belonging to userId. title must be between 1-25 characters. description can't be
   * longer than 500 characters. backgroundUrl must belong to skilltree. prerequisites must be of
   * the same user and tree. completedAt must be null if incomplete.
   *
   * @param achievement The Achievement to be validated
   */
  private void validateAchievement(Achievement achievement) {
    // userId
    ObjectId userId = achievement.getUserId();
    if (!userRepository.existsById(userId)) {
      throw new BadRequestException("Achievement must reference an existing user.");
    }

    // treeId
    ObjectId treeId = achievement.getTreeId();
    Optional<Tree> optionalTree = treeRepository.findById(treeId);
    if (optionalTree.isEmpty()) {
      throw new BadRequestException("Achievement must reference an existing tree.");
    }
    Tree tree = optionalTree.get();
    if (!tree.getUserId().equals(userId)) {
      throw new BadRequestException("Achievement tree must be owned by the same user.");
    }

    // Prerequisites
    for (ObjectId prereqId : achievement.getPrerequisites()) {
      Optional<Achievement> optionalPrerequisite =
          achievementRepository.findByUserIdAndTreeIdAndId(userId, treeId, prereqId);
      if (optionalPrerequisite.isEmpty()) {
        throw new BadRequestException("Prerequisite must have matching userId and treeId.");
      }
    }

    // completedAt
    if (!achievement.isComplete() && achievement.getCompletedAt() != null) {
      throw new BadRequestException("Incomplete achievements cannot have completedAt set.");
    }
  }

  private boolean wouldCreateCycle(Achievement achievement, List<ObjectId> newPrerequisites) {
    Map<ObjectId, Achievement> achievementMap =
        achievementRepository.findByTreeId(achievement.getTreeId()).stream()
            .collect(Collectors.toMap(Achievement::getId, a -> a));

    ObjectId targetId = achievement.getId();
    Deque<ObjectId> stack = new ArrayDeque<>(newPrerequisites);
    while (!stack.isEmpty()) {
      ObjectId currentId = stack.pop();
      if (currentId == null) {
        continue;
      }
      if (currentId.equals(targetId)) {
        return true;
      }

      Achievement currentAch = achievementMap.get(currentId);
      if (currentAch == null) {
        continue;
      }
      for (ObjectId prereqId : currentAch.getPrerequisites()) {
        stack.push(prereqId);
      }
    }
    return false;
  }


  /**
   * Create a new Achievement.
   *
   * @param achievement The Achievement to be created
   * @param userId The Id of the User the Achievement belongs to
   * @return The created Achievement
   */
  @Transactional
  public Achievement create(Achievement achievement, ObjectId userId) {
    logger.info("create(achievement={}, userId={})", achievement, userId);
    achievement.setUserId(userId);
    validateAchievement(achievement);
    logger.info("achievementRepository.insert(achievement={})", achievement);
    Achievement createdAchievement = achievementRepository.insert(achievement);
    logger.info("orientationRepository.findByUserIdAndTreeId(userId={}, treeId={})", userId,
        achievement.getTreeId());
    Orientation orientation =
        orientationRepository.findByUserIdAndTreeId(userId, achievement.getTreeId())
            .orElseThrow(() -> new NotFoundException("orientations",
                Map.of("userId", userId.toString(), "treeId", achievement.getTreeId().toString())));
    orientation.getAchievementLocations()
        .add(new AchievementLocation(createdAchievement.getId(), 0, 0));
    logger.info("orientationRepository.save(orientation={})", orientation);
    orientationRepository.save(orientation);
    return createdAchievement;
  }

  public boolean existsById(ObjectId achievementId) {
    logger.info("existsById(achievementId={})", achievementId);
    logger.info("achievementRepository.existsById(achievementId={})", achievementId);
    return achievementRepository.existsById(achievementId);
  }

  public boolean existsByUserIdAndId(ObjectId userId, ObjectId id) {
    logger.info("existsByUserIdAndId(userId={}, id={})", userId, id);
    logger.info("achievementRepository.existsByUserIdAndId(userId={}, id={})", userId, id);
    return achievementRepository.existsByUserIdAndId(userId, id);
  }

  /**
   * Finds an Achievement by Id from the DAO. NFE thrown if not found.
   *
   * @param achievementId The Id of the desired Achievement
   * @return The Achievement associated with the Id. Otherwise throws.
   */
  public Achievement findById(ObjectId achievementId) {
    logger.info("findById(achievementId={})", achievementId);
    logger.info("achievementRepository.findById(achievementId={})", achievementId);
    return achievementRepository.findById(achievementId)
        .orElseThrow(() -> new NotFoundException("achievements",
            Map.of("achievementId", achievementId.toString())));
  }

  /**
   * Finds an Achievement by Id and UserId from the DAO. NFE thrown if not found.
   *
   * @param userId The Id of the User the Achievement belongs to
   * @param achievementId The Id of the Achievement
   * @return The Achievement associated with userId and Id. Throws otherwise.
   */
  public Achievement findByUserIdAndId(ObjectId userId, ObjectId achievementId) {
    logger.info("findByUserIdAndId(userId={}, achievementId={})", userId, achievementId);
    logger.info("achievementRepository.findByUserIdAndId(userId={}, achievementId={})", userId,
        achievementId);
    return achievementRepository.findByUserIdAndId(userId, achievementId)
        .orElseThrow(() -> new NotFoundException("achievements",
            Map.of("userId", userId.toString(), "achievementId", achievementId.toString())));
  }

  public List<Achievement> findAll() {
    logger.info("findAll()");
    logger.info("achievementRepository.findAll()");
    return achievementRepository.findAll();
  }

  /**
   * Returns all Achievements belonging to a specific User. Supports query parameters. 'next' and
   * 'complete' don't work together (handled in controller).
   *
   * @param userId The User we find Achievements for.
   * @param treeId (optional) The Tree returned Achievements must belong to.
   * @param next (optional) The returned achievements are incomplete with completed prerequisites.
   * @return A list of Achievements belonging to the provided userId, satisfying the query.
   */
  public List<Achievement> findByUserId(ObjectId userId, ObjectId treeId, Boolean next) {
    logger.info("findByUserId(userId={}, treeId={}, next={})", userId, treeId, next);
    if (treeId != null) {
      if (Boolean.TRUE.equals(next)) {
        return findByUserIdAndNext(userId, treeId);
      } else {
        logger.info("achievementRepository.findByUserIdAndTreeId(userId={}, treeId={})", userId,
            treeId);
        return achievementRepository.findByUserIdAndTreeId(userId, treeId);
      }
    }
    if (Boolean.TRUE.equals(next)) {
      return findByUserIdAndNext(userId, null);
    }
    logger.info("achievementRepository.findByUserId(userId={})", userId);
    return achievementRepository.findByUserId(userId);
  }

  /**
   * Handles query params treeId and next, sorts resulting list of Achievements.
   *
   * @param userId The Id of the User the Achievements belong to
   * @param treeId (optional) Returned Achievements belong to the matching Tree
   * @param next (optional) Returned Achievements have fully complete prerequisites and are
   *        incomplete themselves
   * @param sortMode How the resulting list will be sorted
   * @return The filtered and sorted list of Achievements
   */
  public List<Achievement> query(ObjectId userId, ObjectId treeId, Boolean next,
      AchievementSortMode sortMode) {
    logger.info("query(userId={}, treeId={}, next={}, sortMode={})", userId, treeId, next,
        sortMode);
    List<Achievement> achievements = findByUserId(userId, treeId, next);
    switch (sortMode) {
      case TITLE:
        return achievements.stream().sorted(Comparator.comparing(Achievement::getTitle)).toList();
      case COMPLETED_AT:
        List<Achievement> complete = achievements.stream().filter(Achievement::isComplete)
            .sorted(Comparator.comparing(Achievement::getCompletedAt).reversed()).toList();
        List<Achievement> incomplete = achievements.stream().filter(a -> !a.isComplete()).toList();
        complete.addAll(incomplete);
        return complete;
      case CREATED_AT:
        return achievements.stream()
            .sorted(Comparator.comparing(Achievement::getCreatedAt).reversed()).toList();
      default:
        throw new BadRequestException("Invalid AchievementSortMode.");
    }
  }

  /**
   * Find all achievements that have the provided one in their immediate prerequisites.
   *
   * @param userId The User the returned Achievements belong to
   * @param achievementId The Achievement we are finding children for
   * @return The children list for the provided Achievement. Throws otherwise.
   */
  public List<Achievement> findChildren(ObjectId userId, ObjectId achievementId) {
    logger.info("findChildren(userId={}, achievementId={})", userId, achievementId);
    logger.info("achievementRepository.existsByUserIdAndId(userId={}, achievementId={})", userId,
        achievementId);
    if (!achievementRepository.existsByUserIdAndId(userId, achievementId)) {
      throw new NotFoundException("achievements",
          Map.of("userId", userId.toString(), "achievementId", achievementId.toString()));
    }
    logger.info(
        "achievementRepository.findByUserIdAndPrerequisitesContaining(userId={}, achievementId={})",
        userId, achievementId);
    return achievementRepository.findByUserIdAndPrerequisitesContaining(userId, achievementId);
  }

  /**
   * Returns all Achievements belonging to a specific User that have their prerequisites completed.
   * Supports query parameter 'treeId'.
   *
   * @param userId The User the returned Achievements must belong to.
   * @param treeId (optional) The Tree the returned Achievements must belong to.
   * @return The Achievements matching the userId and possible the treeId.
   */
  public List<Achievement> findByUserIdAndNext(ObjectId userId, ObjectId treeId) {
    logger.info("findByUserIdAndNext(userId={}, treeId={})", userId, treeId);
    if (treeId != null && !treeRepository.existsByUserIdAndId(userId, treeId)) {
      throw new NotFoundException("trees",
          Map.of("userId", userId.toString(), "treeId", treeId.toString()));
    }
    if (treeId == null) {
      logger.info("achievementRepository.findByUserId(userId={})", userId);
    } else {
      logger.info("achievementRepository.findByUserIdAndTreeId(userId={}, treeId={})", userId,
          treeId);
    }
    List<Achievement> all = treeId == null ? achievementRepository.findByUserId(userId)
        : achievementRepository.findByUserIdAndTreeId(userId, treeId);

    Map<ObjectId, Boolean> completeMap =
        all.stream().collect(Collectors.toMap(Achievement::getId, Achievement::isComplete));

    List<Achievement> res = all.stream()
        .filter(a -> a.getPrerequisites().stream()
            .allMatch(prereqId -> Boolean.TRUE.equals(completeMap.get(prereqId))))
        .collect(Collectors.toList());
    return res.stream().filter(a -> !a.isComplete()).toList();
  }

  /**
   * Get the list of AchievementFeedItems for the given users over the past 'days' days.
   *
   * @param userIds The Ids of the Users whos Achievements are being collected
   * @param days The number of days in the past to look for Achievements
   * @return The List of AchievementFeedItems
   */
  public List<AchievementFeedItem> getAchievementFeedItemsByUserIds(List<ObjectId> userIds,
      int days) {
    logger.info("getAchievementFeedItemsByUserIds(userIds={}, days={})", userIds, days);
    if (userIds.isEmpty()) {
      return List.of();
    }
    Instant startInstant =
        LocalDate.now(ZoneOffset.UTC).minusDays(days - 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endInstant =
        LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    logger.info(
        "achievementRepository.findByUserIdInAndCompletedAtBetween(userIds={}, startInstant={}, endInstant={})",
        userIds, startInstant, endInstant);
    List<Achievement> achievements = achievementRepository
        .findByUserIdInAndCompletedAtBetween(userIds, startInstant, endInstant);
    logger.info("userRepository.findByIdIn(userIds={})", userIds);
    Map<ObjectId, User> userMap =
        userRepository.findByIdIn(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

    return achievements.stream().map(a -> {
      User user = userMap.get(a.getUserId());
      return new AchievementFeedItem(a.getCompletedAt(), user.getDisplayName(),
          user.getProfilePictureUrl(), a.getTitle(), a.getBackgroundUrl(), a.getDescription());
    }).toList();
  }


  public List<Achievement> findCompletedByUserId(ObjectId userId) {
    logger.info("findCompletedByUserId(userId={})", userId);
    logger.info("achievementRepository.findByUserIdAndComplete(userId={}, complete=true)", userId);
    return achievementRepository.findByUserIdAndComplete(userId, true);
  }

  public List<Achievement> findIncompleteByUserId(ObjectId userId) {
    logger.info("findIncompleteByUserId(userId={})", userId);
    logger.info("achievementRepository.findByUserIdAndComplete(userId={}, complete=false)", userId);
    return achievementRepository.findByUserIdAndComplete(userId, false);
  }

  public List<Achievement> findByTitle(String title) {
    logger.info("findByTitle(title={})", title);
    logger.info("achievementRepository.findByTitle(title={})", title);
    return achievementRepository.findByTitle(title);
  }

  public List<Achievement> findByUserIdAndTitle(ObjectId userId, String title) {
    logger.info("findByUserIdAndTitle(userId={}, title={})", userId, title);
    logger.info("achievementRepository.findByUserIdAndTitle(userId={}, title={})", userId, title);
    return achievementRepository.findByUserIdAndTitle(userId, title);
  }

  // O(n^2)
  private int cascadeIncompleteStatus(ObjectId achievementId) {
    Achievement root = achievementRepository.findById(achievementId)
        .orElseThrow(() -> new NotFoundException("achievements",
            Map.of("achievementId", achievementId.toString())));
    int achievementsTouched = 0;
    Deque<Achievement> stack = new ArrayDeque<>();
    stack.push(root);
    Map<ObjectId, Achievement> achievementMap = achievementRepository.findByTreeId(root.getTreeId())
        .stream().collect(Collectors.toMap(a -> a.getId(), a -> a));
    List<Achievement> modified = new ArrayList<>();
    while (!stack.isEmpty()) {
      Achievement current = stack.pop();
      modified.add(current);
      achievementsTouched++;
      List<Achievement> currentChildren = achievementMap.values().stream()
          .filter(a -> a.getPrerequisites().contains(current.getId())).toList();
      for (Achievement a : currentChildren) {
        stack.push(a);
      }
      current.setComplete(false);
      current.setCompletedAt(null);
    }
    achievementRepository.saveAll(modified);
    return achievementsTouched;
  }

  /**
   * Update an Achievement, requires userid and id provided.
   *
   * @param userId The Id of the User who's Achievement is being updated
   * @param achievementId The Id of the Achievement to be updated
   * @param updatedAchievement The updated version of the Achievement
   * @return The updated Achievement
   */
  @Transactional
  public Achievement update(ObjectId userId, ObjectId achievementId,
      Achievement updatedAchievement) {
    logger.info("update(userId={}, achievementId={}, updatedAchievement={})", userId, achievementId,
        updatedAchievement);
    logger.info("achievementRepository.findByUserIdAndId(userId={}, achievementId={})", userId,
        achievementId);
    Achievement existingAchievement = achievementRepository.findByUserIdAndId(userId, achievementId)
        .orElseThrow(() -> new NotFoundException("achievements",
            Map.of("userId", userId.toString(), "achievementId", achievementId.toString())));
    updatedAchievement.setId(achievementId);
    updatedAchievement.setUserId(userId);
    updatedAchievement.setTreeId(existingAchievement.getTreeId());
    validateAchievement(updatedAchievement);
    if (wouldCreateCycle(updatedAchievement, updatedAchievement.getPrerequisites())) {
      throw new BadRequestException("Making this change would create a circular Tree.");
    }
    // if an incomplete prerequisite is added, set complete=false and completedAt=null for this and
    // children
    updatedAchievement.getPrerequisites().forEach(objId -> {
      if (!existingAchievement.getPrerequisites().contains(objId)) {
        Achievement newPrerequisite = achievementRepository.findById(objId).orElseThrow(
            () -> new NotFoundException("achievements", Map.of("achievementId", objId.toString())));
        if (!newPrerequisite.isComplete()) {
          cascadeIncompleteStatus(updatedAchievement.getId());
        }
      }
    });
    // if this achievement has been changed complete -> incomplete, change all children to
    // incomplete and set completedAt=null.
    if (existingAchievement.isComplete() && !updatedAchievement.isComplete()) {
      cascadeIncompleteStatus(updatedAchievement.getId());
    }
    logger.info("achievementRepository.save(updatedAchievement={})", updatedAchievement);
    achievementRepository.save(updatedAchievement);
    return updatedAchievement;
  }

  /**
   * Partially update an Achievement.
   *
   * @param userId The user the Achievement belongs to
   * @param achievementId The Achievement's Id
   * @param updates Map of updates to be applied to the Achievement
   * @return The updated Achievement
   */
  @Transactional
  public Achievement patch(ObjectId userId, ObjectId achievementId, Map<String, Object> updates) {
    logger.info("patch(userId={}, achievementId={}, updates={})", userId, achievementId, updates);
    Achievement existingAchievement = findByUserIdAndId(userId, achievementId);
    Achievement updatedAchievement = PatchUtils.applyAchievementPatch(existingAchievement, updates);
    updatedAchievement.setId(achievementId);
    updatedAchievement.setUserId(userId);
    updatedAchievement.setTreeId(existingAchievement.getTreeId());
    validateAchievement(updatedAchievement);
    if (wouldCreateCycle(updatedAchievement, updatedAchievement.getPrerequisites())) {
      throw new BadRequestException("Making this change would create a circular Tree.");
    }
    // if an incomplete prerequisite is added, set complete=false and completedAt=null for this and
    // children.
    updatedAchievement.getPrerequisites().forEach(objId -> {
      if (!existingAchievement.getPrerequisites().contains(objId)) {
        Achievement newPrerequisite = achievementRepository.findById(objId).orElseThrow(
            () -> new NotFoundException("achievements", Map.of("achievementId", objId.toString())));
        if (!newPrerequisite.isComplete()) {
          cascadeIncompleteStatus(updatedAchievement.getId());
        }
      }
    });
    // if this achievement has been changed complete -> incomplete, change all children to
    // incomplete and set completedAt=null.
    if (existingAchievement.isComplete() && !updatedAchievement.isComplete()) {
      cascadeIncompleteStatus(updatedAchievement.getId());
    }
    logger.info("achievementRepository.save(updatedAchievement={})", updatedAchievement);
    return achievementRepository.save(updatedAchievement);
  }

  /**
   * Delete an Achievement by its Id. Also remove this Achievement from the prerequisite list of
   * each of its children.
   *
   * @param achievementId The Id of the Achievement to be deleted.
   */
  @Transactional
  public void deleteById(ObjectId achievementId) {
    logger.info("deleteById(achievementId={})", achievementId);
    logger.info("achievementRepository.findById(achievementId={})", achievementId);
    Achievement achievement = achievementRepository.findById(achievementId)
        .orElseThrow(() -> new NotFoundException("achievements",
            Map.of("achievementId", achievementId.toString())));
    List<ObjectId> prerequisites = achievement.getPrerequisites();
    logger.info(
        "achievementRepository.findByUserIdAndPrerequisitesContaining(userId={}, achievementId={})",
        achievement.getUserId(), achievementId);
    List<Achievement> children = achievementRepository
        .findByUserIdAndPrerequisitesContaining(achievement.getUserId(), achievementId);
    for (Achievement child : children) {
      Set<ObjectId> merged = new HashSet<>(child.getPrerequisites());
      merged.remove(achievementId);
      merged.addAll(prerequisites);
      child.setPrerequisites(new ArrayList<>(merged));
    }
    logger.info("achievementRepository.saveAll(children={})", children);
    achievementRepository.saveAll(children);
    logger.info("achievementRepository.deleteById(achievementId={})", achievementId);
    achievementRepository.deleteById(achievementId);
  }

  /**
   * Delete an Achievement matching the provided userId and Id.
   *
   * @param userId The Id of the User the Achievement belongs to
   * @param achievementId The Id of the Achievement
   */
  public void deleteByUserIdAndId(ObjectId userId, ObjectId achievementId) {
    logger.info("deleteByUserIdAndId(userId={}, achievementId={})", userId, achievementId);
    logger.info("achievementRepository.findByUserIdAndId(userId={}, achievementId={})", userId,
        achievementId);
    achievementRepository.findByUserIdAndId(userId, achievementId)
        .orElseThrow(() -> new NotFoundException("achievements",
            Map.of("userId", userId.toString(), "achievementId", achievementId.toString())));
    deleteById(achievementId);
  }

  public void deleteByUserId(ObjectId userId) {
    logger.info("deleteByUserId(userId={})", userId);
    logger.info("achievementRepository.deleteByUserId(userId={})", userId);
    achievementRepository.deleteByUserId(userId);
  }

  public void deleteByUserIdAndTreeId(ObjectId userId, ObjectId treeId) {
    logger.info("deleteByUserIdAndTreeId(userId={}, treeId={})", userId, treeId);
    logger.info("achievementRepository.deleteByUserIdAndTreeId(userId={}, treeId={})", userId,
        treeId);
    achievementRepository.deleteByUserIdAndTreeId(userId, treeId);
  }
}
