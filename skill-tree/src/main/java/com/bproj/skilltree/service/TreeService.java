package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.*;
import com.bproj.skilltree.dto.*;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.ForbiddenException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.mapper.TreeMapper;
import com.bproj.skilltree.model.*;
import com.bproj.skilltree.util.PatchUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements business logic for the 'trees' collection.
 */
@Service
public class TreeService {
  private static final Logger logger = LoggerFactory.getLogger(TreeService.class);
  private final TreeRepository treeRepository;
  private final UserRepository userRepository;
  private final SkillRepository skillRepository;
  private final AchievementRepository achievementRepository;
  private final OrientationRepository orientationRepository;
  private final FriendshipService friendService;


  /**
   * 4-arg constructor. Needs various repository references.
   *
   * @param treeRepository Tree db operations
   * @param userRepository user db operations
   * @param skillRepository skill db operations
   * @param achievementRepository achievement db operations
   */
  @Autowired
  public TreeService(@Qualifier("mongoTreeRepository") TreeRepository treeRepository,
      @Qualifier("mongoUserRepository") UserRepository userRepository,
      @Qualifier("mongoSkillRepository") SkillRepository skillRepository,
      @Qualifier("mongoAchievementRepository") AchievementRepository achievementRepository,
      @Qualifier("mongoOrientationRepository") OrientationRepository orientationRepository,
      FriendshipService friendService) {
    this.treeRepository = treeRepository;
    this.userRepository = userRepository;
    this.skillRepository = skillRepository;
    this.achievementRepository = achievementRepository;
    this.orientationRepository = orientationRepository;
    this.friendService = friendService;
  }

  /**
   * Validates a Tree. Must have a userId (unless preset). Cannot have a userId of
   * visibility=PRESET.
   *
   * @param tree The Tree to be validated.
   */
  private void validateTree(Tree tree) {
    // UserId
    if (tree.getVisibility() != Visibility.PRESET && !userRepository.existsById(tree.getUserId())) {
      throw new BadRequestException("Tree must have a valid userId reference: " + tree.getUserId().toString());
    }
    if (tree.getVisibility() == Visibility.PRESET && tree.getUserId() != null) {
      throw new BadRequestException("Preset trees cannot have attached userIds.");
    }
  }

  /**
   * Create a new Tree and also an Orientation for the Tree.
   *
   * @param tree The Tree to be created
   * @return The created Tree
   */
  @Transactional
  public Tree create(Tree tree, ObjectId userId) {
    logger.info("create(tree={}, userId={})", tree, userId);
    tree.setUserId(userId);
    validateTree(tree);
    logger.info("treeRepository.insert(tree={})", tree);
    Tree createdTree = treeRepository.insert(tree);
    logger.info("orientationRepository.insert(orientation=new Orientation(userId={}, treeId={}))", userId, createdTree.getId());
    Orientation orientation = new Orientation(userId, createdTree.getId());
    orientationRepository.insert(orientation);
    return createdTree;
  }

  public boolean existsById(ObjectId treeId) {
    logger.info("existsById(treeId={})", treeId);
    logger.info("treeRepository.existsById(treeId={})", treeId);
    return treeRepository.existsById(treeId);
  }

  public boolean existsByUserIdAndId(ObjectId userId, ObjectId treeId) {
    logger.info("existsByUserIdAndId(userId={}, treeId={})", userId, treeId);
    logger.info("treeRepository.existsByUserIdAndId(userId={}, treeId={})", userId, treeId);
    return treeRepository.existsByUserIdAndId(userId, treeId);
  }

  /**
   * Find a Tree by its Id. Throws NFE if not found. Admin only.
   *
   * @param treeId The Id of the Tree.
   * @return The Tree, if its found.
   */
  public Tree findById(ObjectId treeId) {
    logger.info("findById(treeId={})", treeId);
    logger.info("treeRepository.findById(treeId={})", treeId);
    return treeRepository.findById(treeId)
        .orElseThrow(() -> new NotFoundException("trees", Map.of("treeId", treeId.toString())));
  }

  /**
   * Find a Tree by its Id and owner. Throws NFE if not found.
   *
   * @param userId The Id of the owner.
   * @param treeId The Id of the tree.
   * @return The Tree associated with the provided owner and id
   */
  public Tree findByUserIdAndId(ObjectId userId, ObjectId treeId) {
    logger.info("findByUserIdAndId(userId={}, treeId={})", userId, treeId);
    logger.info("treeRepository.findByUserIdAndId(userId={}, treeId={})", userId, treeId);
    return treeRepository.findByUserIdAndId(userId, treeId)
        .orElseThrow(() -> new NotFoundException("trees",
            Map.of("userId", userId.toString(), "treeId", treeId.toString())));
  }

  /**
   * Find all Trees belonging to a specific User. Throws NFE if user DNE.
   *
   * @param userId The Id of the User
   * @return All Trees belonging to said user.
   */
  public List<Tree> findByUserId(ObjectId userId) {
    logger.info("findByUserId(userId={})", userId);
    logger.info("userRepository.existsById(userId={})", userId);
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("users", Map.of("userId", userId.toString()));
    }
    logger.info("treeRepository.findByUserId(userId={})", userId);
    return treeRepository.findByUserId(userId);
  }

  public Page<Tree> findPublicTrees(int page, int size) {
    logger.info("findPublicTrees(page={}, size={})", page, size);
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    logger.info("treeRepository.findByVisibility(visibility=PRESET, pageable={})", pageable);
    return treeRepository.findByVisibility(Visibility.PRESET, pageable);
  }

  /**
   * Updates a Tree given its owner, id, and new instance.
   *
   * @param userId The owner of the Tree
   * @param treeId The id of the Tree
   * @param tree The new Tree
   * @return The updated Tree
   */
  public Tree update(ObjectId userId, ObjectId treeId, Tree tree) {
    logger.info("update(userId={}, treeId={}, tree={})", userId, treeId, tree);
    Tree existingTree = findByUserIdAndId(userId, treeId);
    tree.setId(existingTree.getId());
    tree.setUserId(existingTree.getUserId());
    validateTree(tree);
    logger.info("treeRepository.save(tree={})", tree);
    return treeRepository.save(tree);
  }

  /**
   * Partially updates a Tree given its owner, id, and changes.
   *
   * @param userId The owner of the Tree
   * @param treeId The id of the Tree
   * @param updates The updates to be applied to the Tree
   * @return The updated Tree
   */
  public Tree patch(ObjectId userId, ObjectId treeId, Map<String, Object> updates) {
    logger.info("patch(userId={}, treeId={}, updates={})", userId, treeId, updates);
    Tree tree = findByUserIdAndId(userId, treeId);
    Tree updated = PatchUtils.applyTreePatch(tree, updates);
    validateTree(updated);
    logger.info("treeRepository.save(updated={})", updated);
    return treeRepository.save(updated);
  }

  /**
   * Delete a Tree given its Id. Also deletes all related entities.
   *
   * @param treeId The Id of the Tree to be deleted
   */
  @Transactional
  public void deleteById(ObjectId treeId) {
    logger.info("deleteById(treeId={})", treeId);
    logger.info("orientationRepository.deleteByTreeId(treeId={})", treeId);
    orientationRepository.deleteByTreeId(treeId);
    logger.info("skillRepository.deleteByTreeId(treeId={})", treeId);
    skillRepository.deleteByTreeId(treeId);
    logger.info("achievementRepository.deleteByTreeId(treeId={})", treeId);
    achievementRepository.deleteByTreeId(treeId);
    logger.info("treeRepository.deleteById(treeId={})", treeId);
    treeRepository.deleteById(treeId);
  }

  public void deleteByUserId(ObjectId userId) {
    logger.info("deleteByUserId(userId={})", userId);
    logger.info("treeRepository.findByUserId(userId={})", userId);
    treeRepository.findByUserId(userId).forEach(t -> deleteById(t.getId()));
  }

  /**
   * Delete a Tree given the userId and Id.
   *
   * @param userId Id of the User the Tree belongs to
   * @param treeId Id of the Tree
   */
  public void deleteByUserIdAndId(ObjectId userId, ObjectId treeId) {
    logger.info("deleteByUserIdAndId(userId={}, treeId={})", userId, treeId);
    logger.info("treeRepository.findByUserIdAndId(userId={}, treeId={})", userId, treeId);
    treeRepository.findByUserIdAndId(userId, treeId)
        .orElseThrow(() -> new NotFoundException("trees",
            Map.of("userId", userId.toString(), "treeId", treeId.toString())));
    deleteById(treeId);
  }

  // Begin non core operations

  /**
   * Get a TreeLayout given a treeId. Using DTOs in the Service is against best practice according
   * to some, but I figure it's fine here because the mirrored TreeLayout implementation for the
   * Service layer would be identical and the DTO is only used for the method return type.
   *
   * @param treeId The Id of the Tree a TreeLayout is returned for.
   * @return The TreeLayout of the Tree with treeId
   */
  public TreeLayout getLayoutById(ObjectId treeId) {
    logger.info("getLayoutById(treeId={})", treeId);
    // Fetch all required objects for TreeLayout construction
    logger.info("skillRepository.findByTreeId(treeId={})", treeId);
    List<Skill> skills = skillRepository.findByTreeId(treeId);
    logger.info("achievementRepository.findByTreeId(treeId={})", treeId);
    List<Achievement> achievements = achievementRepository.findByTreeId(treeId);
    logger.info("orientationRepository.findByTreeId(treeId={})", treeId);
    Orientation orientation = orientationRepository.findByTreeId(treeId).orElseThrow(
        () -> new NotFoundException("orientations", Map.of("treeId", treeId.toString())));
    return TreeMapper.toTreeLayout(skills, achievements, orientation);
  }

  /**
   * Get a TreeLayout given a treeId and a userId.
   *
   * @param userId The Id of the user the tree belongs to
   * @param treeId The Id of the tree
   * @return The Tree's layout
   */
  public TreeLayout getLayoutByUserIdAndId(ObjectId userId, ObjectId treeId) {
    logger.info("getLayoutByUserIdAndId(userId={}, treeId={})", userId, treeId);
    logger.info("treeRepository.existsByUserIdAndId(userId={}, treeId={})", userId, treeId);
    if (!treeRepository.existsByUserIdAndId(userId, treeId)) {
      throw new NotFoundException("trees",
          Map.of("userId", userId.toString(), "treeId", treeId.toString()));
    }
    return getLayoutById(treeId);
  }

  /**
   * Get a MeTreeLayout (layout + id) given a treeId.
   *
   * @param treeId The Id of the Tree
   * @return The MeTreeLayout DTO
   */
  public MeTreeLayout getMeLayoutById(ObjectId treeId) {
    logger.info("getMeLayoutById(treeId={})", treeId);
    logger.info("skillRepository.findByTreeId(treeId={})", treeId);
    List<Skill> skills = skillRepository.findByTreeId(treeId);
    logger.info("achievementRepository.findByTreeId(treeId={})", treeId);
    List<Achievement> achievements = achievementRepository.findByTreeId(treeId);
    logger.info("orientationRepository.findByTreeId(treeId={})", treeId);
    Orientation orientation = orientationRepository.findByTreeId(treeId).orElseThrow(
        () -> new NotFoundException("orientations", Map.of("treeId", treeId.toString())));
    return TreeMapper.toMeTreeLayout(skills, achievements, orientation);
  }

  /**
   * Get a MeTreeLayout given a userId and a treeId.
   *
   * @param userId The Id of the User the Tree belongs to
   * @param treeId The Id of the Tree
   * @return The MeTreeLayout DTO
   */
  public MeTreeLayout getMeLayoutByUserIdAndId(ObjectId userId, ObjectId treeId) {
    logger.info("getMeLayoutByUserIdAndId(userId={}, treeId={})", userId, treeId);
    logger.info("treeRepository.existsByUserIdAndId(userId={}, treeId={})", userId, treeId);
    if (!treeRepository.existsByUserIdAndId(userId, treeId)) {
      throw new NotFoundException("trees",
          Map.of("userId", userId.toString(), "treeId", treeId.toString()));
    }
    return getMeLayoutById(treeId);
  }

  /**
   * Gather a quick statistics summary on a Tree by its Id. timeSpentHours is only dependent on top
   * level skills of a Tree. This is because hours added to a leaf Skill are counted upwards as
   * well. 2 hours into JavaScript translates into 2 hours into Web Development.
   *
   * @param treeId The Id of the Tree stats are gathered for
   * @return The stats of the Tree
   */
  public TreeStats getStatsById(ObjectId treeId) {
    logger.info("getStatsById(treeId={})", treeId);
    logger.info("skillRepository.findByTreeId(treeId={})", treeId);
    List<Skill> skills = skillRepository.findByTreeId(treeId);
    int totalSkills = skills.size();
    double timeSpentHours = 0;
    for (Skill s : skills) {
      if (s.getParentSkillId() == null) {
        timeSpentHours += s.getTimeSpentHours();
      }
    }

    logger.info("achievementRepository.findByTreeId(treeId={})", treeId);
    List<Achievement> achievements = achievementRepository.findByTreeId(treeId);
    int totalAchievements = achievements.size();
    int completedAchievements =
        achievements.stream().filter(Achievement::isComplete).toList().size();

    return new TreeStats(timeSpentHours, totalSkills, totalAchievements, completedAchievements);
  }

  /**
   * Gather a Tree summary from userId and treeId.
   *
   * @param userId The User that owns the Tree
   * @param treeId The Id of the Tree
   * @return The Stats of the Tree with Id treeId and User userId
   */
  public TreeStats getStatsByUserIdAndId(ObjectId userId, ObjectId treeId) {
    logger.info("getStatsByUserIdAndId(userId={}, treeId={})", userId, treeId);
    logger.info("treeRepository.findByUserIdAndId(userId={}, treeId={})", userId, treeId);
    treeRepository.findByUserIdAndId(userId, treeId)
        .orElseThrow(() -> new NotFoundException("trees",
            Map.of("userId", userId.toString(), "treeId", treeId.toString())));
    return getStatsById(treeId);
  }

  /**
   * Return the provided User's favorite Tree's statistics.
   *
   * @param userId The Id of the User
   * @return The Tree statistics for the User's favorite Tree
   */
  public FavoriteTree getFavoriteTree(ObjectId userId) {
    logger.info("getFavoriteTree(userId={})", userId);
    logger.info("treeRepository.findByUserId(userId={})", userId);
    List<Tree> trees = treeRepository.findByUserId(userId);
    if (trees.isEmpty()) {
      return null;
    }
    Tree favorite = trees.get(0);
    double max = 0;
    for (Tree t : trees) {
      logger.info("skillRepository.findByTreeIdAndParentSkillIdIsNull(treeId={})", t.getId());
      List<Skill> rootSkills = skillRepository.findByTreeIdAndParentSkillIdIsNull(t.getId());
      double hours = rootSkills.stream().mapToDouble(Skill::getTimeSpentHours).sum();
      if (hours > max) {
        favorite = t;
        max = hours;
      }
    }
    TreeStats stats = getStatsById(favorite.getId());
    return new FavoriteTree(favorite.getId(), favorite.getName(), favorite.getBackgroundUrl(),
        stats.getTotalTimeLogged(), stats.getTotalSkills(), stats.getTotalAchievements(),
        stats.getAchievementsCompleted());
  }

  /**
   * Get aggregate TreeStats by userId.
   *
   * @param userId The User the TreeStats belong to
   * @return The stats of all of the user's trees combined
   */
  public TreeStats getStatsByUserId(ObjectId userId) {
    logger.info("getStatsByUserId(userId={})", userId);
    logger.info("treeRepository.findByUserId(userId={})", userId);
    List<ObjectId> treeIds =
        treeRepository.findByUserId(userId).stream().map(tree -> tree.getId()).toList();
    double timeSpentHours = 0;
    int totalSkills = 0;
    int totalAchievements = 0;
    int completedAchievements = 0;
    for (ObjectId treeId : treeIds) {
      TreeStats ts = getStatsById(treeId);
      timeSpentHours += ts.getTotalTimeLogged();
      totalSkills += ts.getTotalSkills();
      totalAchievements += ts.getTotalAchievements();
      completedAchievements += ts.getAchievementsCompleted();
    }
    return new TreeStats(timeSpentHours, totalSkills, totalAchievements, completedAchievements);
  }

  /**
   * Get the list of TreeFeedItems for the given users over the past 'days' days.
   *
   * @param userIds The Ids of the Users whos Trees are being collected
   * @param days The number of days in the past to look for Trees
   * @return The List of TreeFeedItems
   */
  public List<TreeFeedItem> getTreeFeedItemsByUserIds(List<ObjectId> userIds, int days) {
    logger.info("getTreeFeedItemsByUserIds(userIds={}, days={})", userIds, days);
    if (userIds.isEmpty()) {
      return List.of();
    }
    Instant startInstant =
        LocalDate.now(ZoneOffset.UTC).minusDays(days - 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endInstant =
        LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    logger.info("treeRepository.findByUserIdInAndCreatedAtBetween(userIds={}, startInstant={}, endInstant={})", userIds, startInstant, endInstant);
    List<Tree> trees =
        treeRepository.findByUserIdInAndCreatedAtBetween(userIds, startInstant, endInstant);
    logger.info("userRepository.findByIdIn(userIds={})", userIds);
    Map<ObjectId, User> userMap =
        userRepository.findByIdIn(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

    return trees.stream().map(t -> {
      User user = userMap.get(t.getUserId());
      return new TreeFeedItem(t.getCreatedAt(), user.getDisplayName(), user.getProfilePictureUrl(),
          t.getName(), t.getDescription(), t.getBackgroundUrl());
    }).toList();
  }

  private int getTreeNodeCount(ObjectId treeId) {
    if (!treeRepository.existsById(treeId)) {
      throw new NotFoundException("trees", Map.of("treeId", treeId.toString()));
    }

    return skillRepository.findByTreeId(treeId).size()
        + achievementRepository.findByTreeId(treeId).size();
  }

  /**
   * Find the total number of skills and achievements a User has. Required for limiting resource
   * usage.
   *
   * @param userId The user the node count will be performed on
   * @return The total number of nodes (skills + achievements) the user has
   */
  public int countUserNodes(ObjectId userId) {
    logger.info("countUserNodes(userId={})", userId);
    logger.info("skillRepository.findByUserId(userId={})", userId);
    logger.info("achievementRepository.findByUserId(userId={})", userId);
    int nodes = skillRepository.findByUserId(userId).size()
        + achievementRepository.findByUserId(userId).size();
    return nodes;
  }

  private boolean canCopyTree(ObjectId userId, ObjectId treeId) {
    if (!treeRepository.existsById(treeId)) {
      throw new BadRequestException("Tree does not exist.");
    }
    if (!userRepository.existsById(userId)) {
      throw new BadRequestException("User does not exist.");
    }
    if (countUserNodes(userId) + getTreeNodeCount(treeId) > 50) {
      throw new BadRequestException(
          "User does not have enough space. Maximum 50 skills and achievements allowed.");
    }

    Tree tree = treeRepository.findById(treeId)
        .orElseThrow(() -> new NotFoundException("trees", Map.of("treeId", treeId.toString())));
    switch (tree.getVisibility()) {
      case PRESET:
        return true;
      case PUBLIC:
        return true;
      case FRIENDS:
        if (!friendService.areFriends(userId, tree.getUserId())) {
          throw new ForbiddenException("You do not have access to this tree.");
        }
        return true;
      case PRIVATE:
        throw new ForbiddenException("You do not have access to this tree.");
      default:
        throw new BadRequestException("Tree does not have a valid visibility.");
    }
  }

  /**
   * Copy a PRESET, PUBLIC, or FRIENDS visibility tree to the provided user's account, if possible.
   *
   * @param userId The Id of the User the Tree will be copied to
   * @param treeId The Id of the Tree being copied
   * @return The copied Tree
   */
  @Transactional
  public Tree copyToUserAccount(ObjectId userId, ObjectId treeId) {
    logger.info("copyToUserAccount(userId={}, treeId={})", userId, treeId);
    // check if we can copy (throws if not allowed)
    canCopyTree(userId, treeId);

    // initialize old -> new id map
    Map<ObjectId, ObjectId> idMapping = new HashMap<ObjectId, ObjectId>();

    // create tree copy
    logger.info("treeRepository.findById(treeId={})", treeId);
    Tree tree = treeRepository.findById(treeId)
        .orElseThrow(() -> new NotFoundException("trees", Map.of("treeId", treeId.toString())));

    logger.info("treeRepository.insert(newTree=new Tree(userId={}, name={}, backgroundUrl={}, description={}, visibility=FRIENDS))", userId, tree.getName(), tree.getBackgroundUrl(), tree.getDescription());
    Tree newTree = treeRepository.insert(new Tree(userId, tree.getName(), tree.getBackgroundUrl(),
        tree.getDescription(), Visibility.FRIENDS));

    // create skill id mapping
    logger.info("skillRepository.findByTreeId(treeId={})", treeId);
    List<Skill> skills = skillRepository.findByTreeId(treeId);
    for (Skill s : skills) {
      idMapping.put(s.getId(), new ObjectId());
    }
    // create new skills
    List<Skill> newSkills = skills.stream().map(s -> {
      ObjectId newParentId =
          s.getParentSkillId() == null ? null : idMapping.get(s.getParentSkillId());
      Skill newSkill =
          new Skill(userId, newTree.getId(), s.getName(), s.getBackgroundUrl(), 0, newParentId);
      newSkill.setId(idMapping.get(s.getId()));
      return newSkill;
    }).toList();

    // create achievement id mapping
    logger.info("achievementRepository.findByTreeId(treeId={})", treeId);
    List<Achievement> achievements = achievementRepository.findByTreeId(treeId);
    for (Achievement a : achievements) {
      idMapping.put(a.getId(), new ObjectId());
    }
    // create new achievements
    List<Achievement> newAchievements = achievements.stream().map(a -> {
      List<ObjectId> newPrereqs = a.getPrerequisites() == null ? List.of()
          : a.getPrerequisites().stream().map(idMapping::get).toList();
      Achievement newAchievement = new Achievement(userId, newTree.getId(), a.getTitle(),
          a.getBackgroundUrl(), a.getDescription(), newPrereqs, false);
      newAchievement.setId(idMapping.get(a.getId()));
      return newAchievement;
    }).toList();

    // create new orientation
    logger.info("orientationRepository.findByTreeId(treeId={})", treeId);
    Orientation orientation = orientationRepository.findByTreeId(treeId).orElseThrow(
        () -> new NotFoundException("orientations", Map.of("treeId", treeId.toString())));
    List<SkillLocation> newSkillLocations = orientation.getSkillLocations().stream()
        .map(sl -> new SkillLocation(idMapping.get(sl.getSkillId()), sl.getX(), sl.getY()))
        .toList();
    List<AchievementLocation> newAchievementLocations = orientation.getAchievementLocations()
        .stream().map(al -> new AchievementLocation(idMapping.get(al.getAchievementId()), al.getX(),
            al.getY()))
        .toList();
    Orientation newOrientation =
        new Orientation(userId, newTree.getId(), newSkillLocations, newAchievementLocations);

    logger.info("skillRepository.saveAll(newSkills={})", newSkills);
    skillRepository.saveAll(newSkills);
    logger.info("achievementRepository.saveAll(newAchievements={})", newAchievements);
    achievementRepository.saveAll(newAchievements);
    logger.info("orientationRepository.save(newOrientation={})", newOrientation);
    orientationRepository.save(newOrientation);

    return newTree;
  }
}
