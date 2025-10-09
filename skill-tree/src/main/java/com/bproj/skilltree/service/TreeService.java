package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.AchievementRepository;
import com.bproj.skilltree.dao.OrientationRepository;
import com.bproj.skilltree.dao.SkillRepository;
import com.bproj.skilltree.dao.TreeRepository;
import com.bproj.skilltree.dao.UserRepository;
import com.bproj.skilltree.dto.FavoriteTree;
import com.bproj.skilltree.dto.MeTreeLayout;
import com.bproj.skilltree.dto.TreeFeedItem;
import com.bproj.skilltree.dto.TreeLayout;
import com.bproj.skilltree.dto.TreeResponse;
import com.bproj.skilltree.dto.TreeStats;
import com.bproj.skilltree.dto.TreeSummary;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.ForbiddenException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.mapper.TreeMapper;
import com.bproj.skilltree.model.Achievement;
import com.bproj.skilltree.model.AchievementLocation;
import com.bproj.skilltree.model.Orientation;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.model.SkillLocation;
import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.model.User;
import com.bproj.skilltree.model.Visibility;
import com.bproj.skilltree.util.JsonMergePatchUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements business logic for the 'trees' collection.
 */
@Service
public class TreeService {
  private final TreeRepository treeRepository;
  private final UserRepository userRepository;
  private final SkillRepository skillRepository;
  private final AchievementRepository achievementRepository;
  private final OrientationRepository orientationRepository;
  private final FriendshipService friendService;

  private static final String NAME_REGEX = "^[A-Za-z0-9._]{3,15}$";
  private static final String DESCRIPTION_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,500}$";
  private static final String URL_REGEX = "^(https?://)?([a-zA-Z0-9.-]+\\.)?skilltree\\.com(/.*)?$";


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
   * Validates a Tree. userId must reference an existing user, unless the visibility is PRESET, then
   * userId must be null. name must be 1-15 characters, no special symbols. backgroundUrl must be
   * from skilltree. description has max length of 500.
   *
   * @param tree The Tree to be validated.
   */
  private void validateTree(Tree tree) {
    // UserId
    if (tree.getVisibility() != Visibility.PRESET && !userRepository.existsById(tree.getUserId())) {
      throw new BadRequestException("Tree must have a valid reference in userId");
    }
    if (tree.getVisibility() == Visibility.PRESET && tree.getUserId() != null) {
      throw new BadRequestException("Preset trees cannot have attached userIds");
    }

    // Name
    Pattern ptName = Pattern.compile(NAME_REGEX);
    Matcher mtName = ptName.matcher(tree.getName());
    if (!mtName.matches()) {
      throw new BadRequestException("Invalid tree name: " + tree.getName());
    }

    // Description
    Pattern ptDescription = Pattern.compile(DESCRIPTION_REGEX);
    Matcher mtDescription = ptDescription.matcher(tree.getDescription());
    if (!mtDescription.matches()) {
      throw new BadRequestException(
          "Invalid description. Must be < 500 characters. Try getting rid of weird characters.");
    }

    // URL
    Pattern ptUrl = Pattern.compile(URL_REGEX);
    Matcher mtUrl = ptUrl.matcher(tree.getBackgroundUrl());
    if (!mtUrl.matches()) {
      throw new BadRequestException("Invalid background url: " + tree.getBackgroundUrl());
    }
  }

  /**
   * Create a new Tree and also an Orientation for the Tree.
   *
   * @param tree The Tree to be created
   * @return The created Tree
   */
  public Tree create(Tree tree) {
    validateTree(tree);
    Orientation orientation = new Orientation(tree.getUserId(), tree.getId());
    orientationRepository.insert(orientation);
    return treeRepository.insert(tree);
  }

  public TreeResponse createResponse(Tree tree) {
    return TreeMapper.fromTree(create(tree));
  }

  public boolean existsById(ObjectId treeId) {
    return treeRepository.existsById(treeId);
  }

  public boolean existsByUserIdAndId(ObjectId userId, ObjectId treeId) {
    return treeRepository.existsByUserIdAndId(userId, treeId);
  }

  /**
   * Find a Tree by its Id. Throws NFE if not found. Admin only.
   *
   * @param treeId The Id of the Tree.
   * @return The Tree, if its found.
   */
  public Tree getEntityById(ObjectId treeId) {
    Optional<Tree> optionalTree = treeRepository.findById(treeId);
    if (optionalTree.isEmpty()) {
      throw new NotFoundException(Map.of("treeId", treeId.toString()));
    }
    return optionalTree.get();
  }

  public TreeResponse getResponseById(ObjectId treeId) {
    return TreeMapper.fromTree(getEntityById(treeId));
  }

  /**
   * Find a Tree by its Id and owner. Throws NFE if not found.
   *
   * @param userId The Id of the owner.
   * @param treeId The Id of the tree.
   * @return The Tree associated with the provided owner and id
   */
  public Tree getEntityByUserIdAndId(ObjectId userId, ObjectId treeId) {
    Optional<Tree> optionalTree = treeRepository.findByUserIdAndId(userId, treeId);
    if (optionalTree.isEmpty()) {
      throw new NotFoundException(Map.of("userId", userId.toString(), "treeId", treeId.toString()));
    }
    return optionalTree.get();
  }

  public TreeResponse getResponseByUserIdAndId(ObjectId userId, ObjectId treeId) {
    return TreeMapper.fromTree(getEntityByUserIdAndId(userId, treeId));
  }

  /**
   * Find all Trees belonging to a specific User. Throws NFE if user DNE.
   *
   * @param userId The Id of the User
   * @return All Trees belonging to said user.
   */
  public List<Tree> getEntitiesByUserId(ObjectId userId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException(Map.of("userId", userId.toString()));
    }
    return treeRepository.findByUserId(userId);
  }

  public List<TreeResponse> getResponsesByUserId(ObjectId userId) {
    return getEntitiesByUserId(userId).stream().map(TreeMapper::fromTree).toList();
  }

  public List<TreeSummary> getSummariesByUserId(ObjectId userId) {
    return getEntitiesByUserId(userId).stream().map(TreeMapper::getTreeSummary).toList();
  }

  public Page<Tree> findPublicTrees(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return treeRepository.findByVisibility(Visibility.PRESET, pageable);
  }

  public Page<TreeResponse> findPublicTreeResponses(int page, int size) {
    return findPublicTrees(page, size).map(TreeMapper::fromTree);
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
    Tree existingTree = getEntityByUserIdAndId(userId, treeId);
    tree.setId(existingTree.getId());
    tree.setUserId(existingTree.getUserId());
    validateTree(tree);
    return treeRepository.save(tree);
  }

  public TreeResponse updateResponse(ObjectId userId, ObjectId treeId, Tree tree) {
    return TreeMapper.fromTree(update(userId, treeId, tree));
  }

  /**
   * Partially updates a Tree given its owner, id, and changes.
   *
   * @param userId The owner of the Tree
   * @param treeId The id of the Tree
   * @param updates The updates to be applied to the Tree
   * @return The updated Tree
   */
  public Tree patch(ObjectId userId, ObjectId treeId, JsonMergePatch updates) {
    Optional<Tree> optionalTree = treeRepository.findByUserIdAndId(userId, treeId);
    if (optionalTree.isEmpty()) {
      throw new NotFoundException(Map.of("userId", userId.toString(), "treeId", treeId.toString()));
    }

    Tree tree = optionalTree.get();
    Tree updated = JsonMergePatchUtils.applyMergePatch(updates, tree, Tree.class);
    validateTree(updated);
    return treeRepository.save(updated);
  }

  public TreeResponse patchResponse(ObjectId userId, ObjectId treeId, JsonMergePatch updates) {
    return TreeMapper.fromTree(patch(userId, treeId, updates));
  }

  public void deleteById(ObjectId treeId) {
    treeRepository.deleteById(treeId);
  }

  public void deleteByUserId(ObjectId userId) {
    List<Tree> userTrees = getEntitiesByUserId(userId);
    treeRepository.deleteAll(userTrees);
  }

  /**
   * Delete a Tree given the userId and Id.
   *
   * @param userId Id of the User the Tree belongs to
   * @param treeId Id of the Tree
   */
  public void deleteByUserIdAndId(ObjectId userId, ObjectId treeId) {
    Tree tree = getEntityByUserIdAndId(userId, treeId);
    treeRepository.delete(tree);
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
    // Fetch all required objects for TreeLayout construction
    List<Skill> skills = skillRepository.findByTreeId(treeId);
    List<Achievement> achievements = achievementRepository.findByTreeId(treeId);
    Orientation orientation = orientationRepository.findByTreeId(treeId)
        .orElseThrow(() -> new NotFoundException(Map.of("treeId", treeId.toString())));
    return TreeMapper.buildTreeLayout(skills, achievements, orientation);
  }

  /**
   * Get a TreeLayout given a treeId and a userId.
   *
   * @param userId The Id of the user the tree belongs to
   * @param treeId The Id of the tree
   * @return The Tree's layout
   */
  public TreeLayout getLayoutByUserIdAndId(ObjectId userId, ObjectId treeId) {
    if (!treeRepository.existsByUserIdAndId(userId, treeId)) {
      throw new NotFoundException(Map.of("userId", userId.toString(), "treeId", treeId.toString()));
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
    List<Skill> skills = skillRepository.findByTreeId(treeId);
    List<Achievement> achievements = achievementRepository.findByTreeId(treeId);
    Orientation orientation = orientationRepository.findByTreeId(treeId)
        .orElseThrow(() -> new NotFoundException(Map.of("treeId", treeId.toString())));
    return TreeMapper.buildMeLayout(skills, achievements, orientation);
  }

  /**
   * Get a MeTreeLayout given a userId and a treeId.
   *
   * @param userId The Id of the User the Tree belongs to
   * @param treeId The Id of the Tree
   * @return The MeTreeLayout DTO
   */
  public MeTreeLayout getMeLayoutByUserIdAndId(ObjectId userId, ObjectId treeId) {
    if (!treeRepository.existsByUserIdAndId(userId, treeId)) {
      throw new NotFoundException(Map.of("userId", userId.toString(), "treeId", treeId.toString()));
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
    List<Skill> skills = skillRepository.findByTreeId(treeId);
    int totalSkills = skills.size();
    double timeSpentHours = 0;
    for (Skill s : skills) {
      if (s.getParentSkillId() == null) {
        timeSpentHours += s.getTimeSpentHours();
      }
    }

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
    treeRepository.findByUserIdAndId(userId, treeId).orElseThrow(() -> new NotFoundException(
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
    List<Tree> trees = treeRepository.findByUserId(userId);
    if (trees.isEmpty()) {
      return null;
    }
    Tree favorite = trees.get(0);
    double max = 0;
    for (Tree t : trees) {
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
  public List<TreeFeedItem> getFeedItems(List<ObjectId> userIds, int days) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    Instant startInstant =
        LocalDate.now(ZoneOffset.UTC).minusDays(days - 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endInstant =
        LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    List<Tree> trees =
        treeRepository.findByUserIdInAndCreatedAtBetween(userIds, startInstant, endInstant);
    Map<ObjectId, User> userMap =
        userRepository.findByIdIn(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

    return trees.stream().map(t -> {
      User user = userMap.get(t.getUserId());
      return new TreeFeedItem(t.getCreatedAt(), user.getDisplayName(), user.getProfilePictureUrl(),
          t.getName(), t.getDescription(), t.getBackgroundUrl());
    }).toList();
  }

  private int sizeOfTree(ObjectId treeId) {
    if (!treeRepository.existsById(treeId)) {
      throw new NotFoundException(Map.of("treeId", treeId.toString()));
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
  public int totalNodesForUser(ObjectId userId) {
    int nodes = skillRepository.findByUserId(userId).size()
        + achievementRepository.findByUserId(userId).size();
    return nodes;
  }

  private boolean canCopy(ObjectId userId, ObjectId treeId) {
    if (!treeRepository.existsById(treeId)) {
      throw new BadRequestException("Cannot copy, treeId does not exist.");
    }
    if (!userRepository.existsById(userId)) {
      throw new BadRequestException("Cannot copy, userId does not exist.");
    }
    if (totalNodesForUser(userId) + sizeOfTree(treeId) > 50) {
      throw new BadRequestException(
          "Cannot copy, user does not have enough space (50 skills + achievements max)");
    }

    Tree tree = treeRepository.findById(treeId)
        .orElseThrow(() -> new NotFoundException(Map.of("treeId", treeId.toString())));
    switch (tree.getVisibility()) {
      case PRESET:
        return true;
      case PUBLIC:
        return true;
      case FRIENDS:
        if (!friendService.areFriends(userId, tree.getUserId())) {
          throw new ForbiddenException("You do not have access to this Tree.");
        }
        return true;
      case PRIVATE:
        throw new ForbiddenException("You do not have access to this Tree.");
      default:
        throw new BadRequestException("Tree doesn't have a valid visibility.");
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
    // check if we can copy (throws if not allowed)
    canCopy(userId, treeId);

    // initialize old -> new id map
    Map<ObjectId, ObjectId> idMapping = new HashMap<ObjectId, ObjectId>();

    // create tree copy
    Tree tree = treeRepository.findById(treeId)
        .orElseThrow(() -> new NotFoundException(Map.of("treeId", treeId.toString())));

    Tree newTree = treeRepository.insert(new Tree(userId, tree.getName(), tree.getBackgroundUrl(),
        tree.getDescription(), Visibility.FRIENDS));

    // create skill id mapping
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
    Orientation orientation = orientationRepository.findByTreeId(treeId)
        .orElseThrow(() -> new NotFoundException(Map.of("treeId", treeId.toString())));
    List<SkillLocation> newSkillLocations = orientation.getSkillLocations().stream()
        .map(sl -> new SkillLocation(idMapping.get(sl.getSkillId()), sl.getX(), sl.getY()))
        .toList();
    List<AchievementLocation> newAchievementLocations = orientation.getAchievementLocations()
        .stream().map(al -> new AchievementLocation(idMapping.get(al.getAchievementId()), al.getX(),
            al.getY()))
        .toList();
    Orientation newOrientation =
        new Orientation(userId, newTree.getId(), newSkillLocations, newAchievementLocations);

    skillRepository.saveAll(newSkills);
    achievementRepository.saveAll(newAchievements);
    orientationRepository.save(newOrientation);

    return newTree;
  }
}
