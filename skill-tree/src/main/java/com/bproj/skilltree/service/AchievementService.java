package com.bproj.skilltree.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.bproj.skilltree.dao.AchievementRepository;
import com.bproj.skilltree.dao.TreeRepository;
import com.bproj.skilltree.dao.UserRepository;
import com.bproj.skilltree.dto.AchievementFeedItem;
import com.bproj.skilltree.dto.AchievementResponse;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.mapper.AchievementMapper;
import com.bproj.skilltree.model.Achievement;
import com.bproj.skilltree.model.AchievementSortMode;
import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.model.User;
import com.bproj.skilltree.util.JsonMergePatchUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;

/**
 * Implements for business logic for the 'achievements' collection.
 */
@Service
public class AchievementService {
  private final AchievementRepository achievementRepository;
  private final UserRepository userRepository;
  private final TreeRepository treeRepository;
  private static final String TITLE_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,50}$";
  private static final String DESCRIPTION_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,500}$";
  private static final String URL_REGEX = "^(https?://)?([a-zA-Z0-9.-]+\\.)?skilltree\\.com(/.*)?$";

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
      @Qualifier("mongoTreeRepository") TreeRepository treeRepository) {
    this.achievementRepository = achievementRepository;
    this.userRepository = userRepository;
    this.treeRepository = treeRepository;
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
      throw new BadRequestException("userId must reference an existing user.");
    }

    // treeId
    ObjectId treeId = achievement.getTreeId();
    Optional<Tree> optionalTree = treeRepository.findById(treeId);
    if (optionalTree.isEmpty()) {
      throw new BadRequestException("treeId must reference an existing tree.");
    }
    Tree tree = optionalTree.get();
    if (!tree.getUserId().equals(userId)) {
      throw new BadRequestException(
          "An Achievement's tree must be owned by the same user as the Achievement.");
    }

    // title
    Pattern ptTitle = Pattern.compile(TITLE_REGEX);
    Matcher mtTitle = ptTitle.matcher(achievement.getTitle());
    if (!mtTitle.matches()) {
      throw new BadRequestException("Title must be 1-50 characters.");
    }

    // description
    if (achievement.getDescription() != null) {
      Pattern ptDesc = Pattern.compile(DESCRIPTION_REGEX);
      Matcher mtDesc = ptDesc.matcher(achievement.getDescription());
      if (!mtDesc.matches()) {
        throw new BadRequestException("Description must be less than 500 characters.");
      }
    }

    // URL
    Pattern ptUrl = Pattern.compile(URL_REGEX);
    Matcher mtUrl = ptUrl.matcher(achievement.getBackgroundUrl());
    if (!mtUrl.matches()) {
      throw new BadRequestException("URL must belong to skilltree");
    }

    // Prerequisites
    for (ObjectId prereqId : achievement.getPrerequisites()) {
      Optional<Achievement> optionalPrerequisite =
          achievementRepository.findByUserIdAndTreeIdAndId(userId, treeId, prereqId);
      if (optionalPrerequisite.isEmpty()) {
        throw new BadRequestException(
            "Prerequisite could not be found with matching userId and treeId.");
      }
    }

    // completedAt
    if (!achievement.isComplete() && achievement.getCompletedAt() != null) {
      throw new BadRequestException("Incomplete achievements must have completedAt set to null.");
    }
  }

  public Achievement create(Achievement achievement) {
    validateAchievement(achievement);
    return achievementRepository.insert(achievement);
  }

  public AchievementResponse createResponse(Achievement achievement) {
    return AchievementMapper.fromAchievement(create(achievement));
  }

  public AchievementResponse createResponse(ObjectId userId, Achievement achievement) {
    achievement.setUserId(userId);
    return createResponse(achievement);
  }

  public boolean existsById(ObjectId achievementId) {
    return achievementRepository.existsById(achievementId);
  }

  public boolean existsByUserIdAndId(ObjectId userId, ObjectId id) {
    return achievementRepository.existsByUserIdAndId(userId, id);
  }

  /**
   * Finds an Achievement by Id from the DAO. NFE thrown if not found.
   *
   * @param achievementId The Id of the desired Achievement
   * @return The Achievement associated with the Id. Otherwise throws.
   */
  public Achievement getEntityById(ObjectId achievementId) {
    return achievementRepository.findById(achievementId).orElseThrow(
        () -> new NotFoundException(Map.of("achievementId", achievementId.toString())));
  }

  public AchievementResponse getResponseById(ObjectId achievementId) {
    return AchievementMapper.fromAchievement(getEntityById(achievementId));
  }

  /**
   * Finds an Achievement by Id and UserId from the DAO. NFE thrown if not found.
   *
   * @param userId The Id of the User the Achievement belongs to
   * @param achievementId The Id of the Achievement
   * @return The Achievement associated with userId and Id. Throws otherwise.
   */
  public Achievement getEntityByUserIdAndId(ObjectId userId, ObjectId achievementId) {
    return achievementRepository.findByUserIdAndId(userId, achievementId)
        .orElseThrow(() -> new NotFoundException(
            Map.of("userId", userId.toString(), "achievementId", achievementId.toString())));
  }

  public AchievementResponse getResponseByUserIdAndId(ObjectId userId, ObjectId achievementId) {
    return AchievementMapper.fromAchievement(getEntityByUserIdAndId(userId, achievementId));
  }

  public List<Achievement> getAllEntities() {
    return achievementRepository.findAll();
  }

  public List<Achievement> getEntityByUserId(ObjectId userId) {
    return achievementRepository.findByUserId(userId);
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
  public List<Achievement> getEntityByUserId(ObjectId userId, ObjectId treeId, Boolean next) {
    if (treeId != null) {
      if (Boolean.TRUE.equals(next)) {
        return findByUserIdAndNext(userId, treeId);
      } else {
        return achievementRepository.findByUserIdAndTreeId(userId, treeId);
      }
    }
    if (Boolean.TRUE.equals(next)) {
      return findByUserIdAndNext(userId, null);
    }
    return achievementRepository.findByUserId(userId);
  }

  public List<AchievementResponse> getResponseByUserId(ObjectId userId) {
    return getEntityByUserId(userId).stream().map(AchievementMapper::fromAchievement).toList();
  }

  public List<AchievementResponse> getResponseByUserId(ObjectId userId, ObjectId treeId,
      Boolean next) {
    return getEntityByUserId(userId, treeId, next).stream().map(AchievementMapper::fromAchievement)
        .toList();
  }

  /**
   * Find all achievements that have the provided one in their immediate prerequisites.
   *
   * @param userId The User the returned Achievements belong to
   * @param achievementId The Achievement we are finding children for
   * @return The children list for the provided Achievement. Throws otherwise.
   */
  public List<Achievement> findChildren(ObjectId userId, ObjectId achievementId) {
    if (!achievementRepository.existsByUserIdAndId(userId, achievementId)) {
      Map<String, String> query =
          Map.of("userId", userId.toString(), "achievementId", achievementId.toString());
      throw new NotFoundException(query);
    }
    return achievementRepository.findByUserIdAndPrerequisitesContaining(userId, achievementId);
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
  public List<Achievement> queryEntities(ObjectId userId, ObjectId treeId, Boolean next,
      AchievementSortMode sortMode) {
    List<Achievement> achievements = getEntityByUserId(userId, treeId, next);
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
        throw new BadRequestException(
            "AchievementSortMode " + sortMode.toString() + " not recognized.");
    }
  }

  public List<AchievementResponse> queryResponses(ObjectId userId, ObjectId treeId, Boolean next,
      AchievementSortMode sortMode) {
    return queryEntities(userId, treeId, next, sortMode).stream()
        .map(AchievementMapper::fromAchievement).toList();
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
    if (treeId != null && !treeRepository.existsByUserIdAndId(userId, treeId)) {
      throw new NotFoundException(Map.of("userId", userId.toString(), "treeId", treeId.toString()));
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
  public List<AchievementFeedItem> getFeedItems(List<ObjectId> userIds, int days) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    Instant startInstant =
        LocalDate.now(ZoneOffset.UTC).minusDays(days - 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endInstant =
        LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    List<Achievement> achievements = achievementRepository
        .findByUserIdInAndCompletedAtBetween(userIds, startInstant, endInstant);
    Map<ObjectId, User> userMap =
        userRepository.findByIdIn(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

    return achievements.stream().map(a -> {
      User user = userMap.get(a.getUserId());
      return new AchievementFeedItem(a.getCompletedAt(), user.getDisplayName(),
          user.getProfilePictureUrl(), a.getTitle(), a.getBackgroundUrl(), a.getDescription());
    }).toList();
  }


  public List<Achievement> findCompletedByUserId(ObjectId userId) {
    return achievementRepository.findByUserIdAndComplete(userId, true);
  }

  public List<Achievement> findIncompleteByUserId(ObjectId userId) {
    return achievementRepository.findByUserIdAndComplete(userId, false);
  }

  public List<Achievement> findByTitle(String title) {
    return achievementRepository.findByTitle(title);
  }

  public List<Achievement> findByUserIdAndTitle(ObjectId userId, String title) {
    return achievementRepository.findByUserIdAndTitle(userId, title);
  }

  public Achievement update(Achievement updatedAchievement) {
    return achievementRepository.save(updatedAchievement);
  }

  /**
   * Update an Achievement, requires userid and id provided.
   *
   * @param userId The Id of the User who's Achievement is being updated
   * @param achievementId The Id of the Achievement to be updated
   * @param updatedAchievement The updated version of the Achievement
   * @return The updated Achievement
   */
  public Achievement update(ObjectId userId, ObjectId achievementId,
      Achievement updatedAchievement) {
    if (!achievementRepository.existsByUserIdAndId(userId, achievementId)) {
      throw new NotFoundException(
          Map.of("userId", userId.toString(), "achievementId", achievementId.toString()));
    }
    updatedAchievement.setId(achievementId);
    validateAchievement(updatedAchievement);
    achievementRepository.save(updatedAchievement);
    return updatedAchievement;
  }

  /**
   * Update an Achievement and return the AchievementResponse. Requires userId and Id.
   *
   * @param userId The Id of the User the Achievement belongs to
   * @param achievementId The Id of the Achievement
   * @param updatedAchievement The updated version of the Achievement
   * @return The AchievementResponse DTO
   */
  public AchievementResponse updateResponse(ObjectId userId, ObjectId achievementId,
      Achievement updatedAchievement) {
    return AchievementMapper.fromAchievement(update(userId, achievementId, updatedAchievement));
  }

  /**
   * Partially update an Achievement.
   *
   * @param userId The user the Achievement belongs to
   * @param achievementId The Achievement's Id
   * @param updates Map of updates to be applied to the Achievement
   * @return The updated Achievement
   */
  public Achievement patch(ObjectId userId, ObjectId achievementId, JsonMergePatch updates) {
    Optional<Achievement> optionalAchievement =
        achievementRepository.findByUserIdAndId(userId, achievementId);
    if (optionalAchievement.isEmpty()) {
      Map<String, String> query =
          Map.of("userId", userId.toString(), "achievementId", achievementId.toString());
      throw new NotFoundException(query);
    }

    Achievement achievement = optionalAchievement.get();
    Achievement updated =
        JsonMergePatchUtils.applyMergePatch(updates, achievement, Achievement.class);
    updated.setId(achievementId);
    validateAchievement(updated);
    return achievementRepository.save(updated);
  }

  /**
   * Partially update an Achievement and return the AchievementResponse.
   *
   * @param userId The Id of the User the Achievement belongs to
   * @param achievementId The Id of the Achievement
   * @param updates The updates to be applied to the Achievement
   * @return The AchievementResponse DTO of the updated Achievement
   */
  public AchievementResponse patchResponse(ObjectId userId, ObjectId achievementId,
      JsonMergePatch updates) {
    return AchievementMapper.fromAchievement(patch(userId, achievementId, updates));
  }

  public void deleteById(ObjectId achievementId) {
    achievementRepository.deleteById(achievementId);
  }

  public void deleteByUserId(ObjectId userId) {
    achievementRepository.deleteByUserId(userId);
  }

  public void deleteByUserIdAndTreeId(ObjectId userId, ObjectId treeId) {
    achievementRepository.deleteByUserIdAndTreeId(userId, treeId);
  }
}
