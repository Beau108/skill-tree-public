package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.OrientationRepository;
import com.bproj.skilltree.dao.SkillRepository;
import com.bproj.skilltree.dao.TreeRepository;
import com.bproj.skilltree.dao.UserRepository;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements business logic for 'skills' collection.
 */
@Service
public class SkillService {
  private static final Logger logger = LoggerFactory.getLogger(SkillService.class);
  private final SkillRepository skillRepository;
  private final UserRepository userRepository;
  private final TreeRepository treeRepository;
  private final OrientationRepository orientationRepository;


  /**
   * Autowired constructor.
   *
   * @param skillRepository Skill DB operations
   * @param userRepository User DB operations
   * @param treeRepository Tree DB operations
   */
  @Autowired
  public SkillService(@Qualifier("mongoSkillRepository") SkillRepository skillRepository,
      @Qualifier("mongoUserRepository") UserRepository userRepository,
      @Qualifier("mongoTreeRepository") TreeRepository treeRepository,
      @Qualifier("mongoOrientationRepository") OrientationRepository orientationRepository) {
    this.skillRepository = skillRepository;
    this.userRepository = userRepository;
    this.treeRepository = treeRepository;
    this.orientationRepository = orientationRepository;
  }

  /**
   * Validates a Skill. userId must reference an existing user. treeId must reference an existing
   * tree belonging to userId. Name must be 1-25 characters. backgroundUrl must be from skilltree.
   * timeSpentHours must be >=0. parentSkillId, if not null, must references a skill of the same
   * tree and user.
   *
   * @param skill The Skill to be checked
   */
  private void validateSkill(Skill skill) {
    ObjectId userId = skill.getUserId();
    ObjectId treeId = skill.getTreeId();

    // userId
    if (!userRepository.existsById(userId)) {
      throw new BadRequestException("Skill must reference an existing user.");
    }

    // treeId
    Optional<Tree> optionalTree = treeRepository.findById(treeId);
    if (optionalTree.isEmpty()) {
      throw new BadRequestException("Skill must reference an existing tree.");
    }
    Tree tree = optionalTree.get();
    if (!tree.getUserId().equals(userId)) {
      throw new BadRequestException("Skill tree must be owned by the same user.");
    }

    // time
    if (skill.getTimeSpentHours() < 0) {
      throw new BadRequestException("Time spent must be greater than or equal to 0.");
    }

    // parentSkillId
    ObjectId parentSkillId = skill.getParentSkillId();
    if (parentSkillId != null) {
      Optional<Skill> optionalParent = skillRepository.findByUserIdAndId(userId, parentSkillId);
      if (optionalParent.isEmpty()) {
        throw new NotFoundException("skills",
            Map.of("userId", userId.toString(), "parentSkillId", parentSkillId.toString()));
      }
      Skill parent = optionalParent.get();
      if (!parent.getTreeId().equals(treeId)) {
        throw new BadRequestException("Parent skill must have matching treeId.");
      }
    }
  }

  private boolean wouldCreateCycle(Skill skill, ObjectId newParentId) {
    if (newParentId == null) {
      return false;
    }

    ObjectId target = skill.getId();
    Map<ObjectId, Skill> skillMap = skillRepository.findByTreeId(skill.getTreeId()).stream()
        .collect(Collectors.toMap(Skill::getId, s -> s));

    Deque<ObjectId> stack = new ArrayDeque<>();
    Set<ObjectId> visited = new HashSet<>();
    stack.push(newParentId);

    while (!stack.isEmpty()) {
      ObjectId currentId = stack.pop();
      if (currentId == null || !visited.add(currentId)) {
        continue;
      }
      if (currentId.equals(target)) {
        return true;
      }

      Skill current = skillMap.get(currentId);
      if (current == null) {
        continue;
      }
      stack.push(current.getParentSkillId());
    }

    return false;
  }


  /**
   * Create a new Skill. Also add it to the owning Tree's Orientation. (hard coded to 0, 0)
   *
   * @param skill The Skill to be created
   * @param userId The Id of the User the Skill will belong to
   * @return The created Skill
   */
  @Transactional
  public Skill create(Skill skill, ObjectId userId) {
    logger.info("create(skill={}, userId={})", skill, userId);
    logger.info("userRepository.existsById(userId={})", userId);
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("users", Map.of("userId", userId.toString()));
    }
    skill.setUserId(userId);
    validateSkill(skill);
    logger.info("skillRepository.insert(skill={})", skill);
    Skill createdSkill = skillRepository.insert(skill);
    logger.info("orientationRepository.findByUserIdAndTreeId(userId={}, treeId={})", userId, skill.getTreeId());
    Orientation orientation = orientationRepository.findByUserIdAndTreeId(userId, skill.getTreeId())
        .orElseThrow(() -> new NotFoundException("orientations",
            Map.of("userId", userId.toString(), "treeId", skill.getTreeId().toString())));
    orientation.getSkillLocations().add(new SkillLocation(createdSkill.getId(), 0, 0));
    logger.info("orientationRepository.save(orientation={})", orientation);
    orientationRepository.save(orientation);
    return createdSkill;
  }

  public boolean existsById(ObjectId skillId) {
    logger.info("existsById(skillId={})", skillId);
    logger.info("skillRepository.existsById(skillId={})", skillId);
    return skillRepository.existsById(skillId);
  }

  public boolean existsByUserIdAndId(ObjectId userId, ObjectId id) {
    logger.info("existsByUserIdAndId(userId={}, id={})", userId, id);
    logger.info("skillRepository.existsByUserIdAndId(userId={}, id={})", userId, id);
    return skillRepository.existsByUserIdAndId(userId, id);
  }

  /**
   * Find a Skill by Id.
   *
   * @param skillId The Id of the desired Skill
   * @return The found Skill. Throws NFE otherwise.
   */
  public Skill findById(ObjectId skillId) {
    logger.info("findById(skillId={})", skillId);
    logger.info("skillRepository.findById(skillId={})", skillId);
    return skillRepository.findById(skillId)
        .orElseThrow(() -> new NotFoundException("skills", Map.of("skillId", skillId.toString())));
  }

  public List<Skill> findAll() {
    logger.info("findAll()");
    logger.info("skillRepository.findAll()");
    return skillRepository.findAll();
  }

  /**
   * Find all Skills for a given userId.
   *
   * @param userId The Id for matching Skills
   * @return The List of Skills with a matching userId
   */
  public List<Skill> findByUserId(ObjectId userId) {
    logger.info("findByUserId(userId={})", userId);
    logger.info("userRepository.existsById(userId={})", userId);
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("users", Map.of("userId", userId.toString()));
    }
    logger.info("skillRepository.findByUserId(userId={})", userId);
    return skillRepository.findByUserId(userId);
  }

  /**
   * Return all Skills belonging to a specified User. Query parameters 'parentSkillId' and 'root'
   * supported, however, they are incompatible with each other.
   *
   * @param userId The Id of the User
   * @param parentSkillId The Id of the parentSkill we are finding children for
   * @param root Whether or not the skills returned are to be root skills.
   * @return The List of Skills matching the userId and query parameters.
   */
  public List<Skill> findByUserId(ObjectId userId, ObjectId parentSkillId, Boolean root) {
    logger.info("findByUserId(userId={}, parentSkillId={}, root={})", userId, parentSkillId, root);
    if (parentSkillId != null) {
      logger.info("skillRepository.findByUserIdAndParentSkillId(userId={}, parentSkillId={})", userId, parentSkillId);
      return skillRepository.findByUserIdAndParentSkillId(userId, parentSkillId);
    }
    if (Boolean.TRUE.equals(root)) {
      logger.info("skillRepository.findByUserIdAndParentSkillIdIsNull(userId={})", userId);
      return skillRepository.findByUserIdAndParentSkillIdIsNull(userId);
    } else if (Boolean.FALSE.equals(root)) {
      logger.info("skillRepository.findByUserIdAndParentSkillIdIsNotNull(userId={})", userId);
      return skillRepository.findByUserIdAndParentSkillIdIsNotNull(userId);
    }
    logger.info("skillRepository.findByUserId(userId={})", userId);
    return skillRepository.findByUserId(userId);
  }

  /**
   * Filter and sort skills owned by the User with userId. Note that the sorting on RECENTLY_USED
   * lazily uses Skill.getUpdatedAt(). This saves lots of work for the server but results in
   * changing a Skill name to count as "using" that Skill.
   *
   * @param userId The Id of the User
   * @param parentSkillId The Id of the parent Skill for all returned Skills
   * @param root Whether or not returned Skills will be root Skills
   * @param sortMode How the final List will be sorted.
   * @return The filtered and sorted List of Skills
   */
  public List<Skill> findAndSortSkills(ObjectId userId, ObjectId parentSkillId, Boolean root,
      SkillSortMode sortMode) {
    logger.info("findAndSortSkills(userId={}, parentSkillId={}, root={}, sortMode={})", userId, parentSkillId, root, sortMode);
    List<Skill> skills = findByUserId(userId, parentSkillId, root);
    if (sortMode == null) {
      sortMode = SkillSortMode.NAME;
    }
    switch (sortMode) {
      case CREATED_AT:
        return skills.stream().sorted(Comparator.comparing(Skill::getCreatedAt).reversed())
            .toList();
      case TIME_SPENT:
        return skills.stream().sorted(Comparator.comparing(Skill::getTimeSpentHours).reversed())
            .toList();
      case RECENTLY_USED:
        return skills.stream().sorted(Comparator.comparing(Skill::getUpdatedAt).reversed())
            .toList();
      case NAME:
        return skills.stream().sorted(Comparator.comparing(Skill::getName)).toList();
      default:
        throw new BadRequestException("Invalid SkillSortMode.");
    }
  }

  /**
   * Finds an existing skill by UserId and SkillId.
   *
   * @param userId The id of the user
   * @param skillId The id of the skill
   * @return The skill with matching userid and id. Null if not found.
   */
  public Skill findByUserIdAndId(ObjectId userId, ObjectId skillId) {
    logger.info("findByUserIdAndId(userId={}, skillId={})", userId, skillId);
    logger.info("skillRepository.findByUserIdAndId(userId={}, skillId={})", userId, skillId);
    return skillRepository.findByUserIdAndId(userId, skillId)
        .orElseThrow(() -> new NotFoundException("skills",
            Map.of("userId", userId.toString(), "skillId", skillId.toString())));
  }

  /**
   * 'Safe update'. Updates a Skill given a userId, skillId, and Skill.
   *
   * @param userId The Id of the User the Skill belongs to
   * @param skillId The Id of the Skill begin updated
   * @param updatedSkill The updated version of the Skill entity
   * @return The Skill that was updated
   */
  @Transactional
  public Skill update(ObjectId userId, ObjectId skillId, Skill updatedSkill) {
    logger.info("update(userId={}, skillId={}, updatedSkill={})", userId, skillId, updatedSkill);
    Skill existingSkill = findByUserIdAndId(userId, skillId);

    updatedSkill.setId(existingSkill.getId());
    updatedSkill.setTreeId(existingSkill.getTreeId());
    updatedSkill.setUserId(userId);
    updatedSkill.setTimeSpentHours(existingSkill.getTimeSpentHours());

    validateSkill(updatedSkill);
    if (wouldCreateCycle(updatedSkill, updatedSkill.getParentSkillId())) {
      throw new BadRequestException(
          "This parentSkillId would create a cycle within the Skill's Tree.");
    }

    // parent change, subtract hours of this skill from old parent.
    if (!Objects.equals(existingSkill.getParentSkillId(), (updatedSkill.getParentSkillId()))) {
      if (existingSkill.getParentSkillId() != null) {
        addHours(existingSkill.getParentSkillId(), existingSkill.getTimeSpentHours() * -1);
      }
      if (updatedSkill.getParentSkillId() != null) {
        addHours(updatedSkill.getParentSkillId(), updatedSkill.getTimeSpentHours());
      }
    }

    logger.info("skillRepository.save(updatedSkill={})", updatedSkill);
    return skillRepository.save(updatedSkill);
  }

  /**
   * Partially update a Skill.
   *
   * @param userId The User this Skill belongs to
   * @param skillId The Id of the Skill to be updated
   * @param updates The updates to be applied to the Skill
   * @return The updated Skill
   */
  @Transactional
  public Skill patch(ObjectId userId, ObjectId skillId, Map<String, Object> updates) {
    logger.info("patch(userId={}, skillId={}, updates={})", userId, skillId, updates);
    logger.info("skillRepository.findByUserIdAndId(userId={}, skillId={})", userId, skillId);
    Skill existingSkill = skillRepository.findByUserIdAndId(userId, skillId)
        .orElseThrow(() -> new NotFoundException("skills",
            Map.of("userId", userId.toString(), "skillId", skillId.toString())));
    Skill updatedSkill = PatchUtils.applySkillPatch(existingSkill, updates);
    updatedSkill.setId(skillId);
    updatedSkill.setUserId(userId);
    validateSkill(updatedSkill);
    if (wouldCreateCycle(updatedSkill, updatedSkill.getParentSkillId())) {
      throw new BadRequestException(
          "This parentSkillId would create a cycle within the Skill's Tree.");
    }
    if (!Objects.equals(existingSkill.getParentSkillId(), (updatedSkill.getParentSkillId()))) {
      if (existingSkill.getParentSkillId() != null) {
        addHours(existingSkill.getParentSkillId(), existingSkill.getTimeSpentHours() * -1);
      }
      if (updatedSkill.getParentSkillId() != null) {
        addHours(updatedSkill.getParentSkillId(), updatedSkill.getTimeSpentHours());
      }
    }
    logger.info("skillRepository.save(updatedSkill={})", updatedSkill);
    return skillRepository.save(updatedSkill);
  }

  /**
   * Add hours to a Skill and its predecessors.
   *
   * @param skillId The Id of the Skill hours are added to
   * @param hours The amount of hours added to the Skill
   * @return The number of Skills hours were added to in the process
   */
  @Transactional
  public int addHours(ObjectId skillId, double hours) {
    logger.info("addHours(skillId={}, hours={})", skillId, hours);
    int skillsTouched = 0;

    logger.info("skillRepository.findById(skillId={})", skillId);
    Optional<Skill> currentOptional = skillRepository.findById(skillId);
    while (currentOptional.isPresent()) {
      Skill current = currentOptional.get();
      current.setTimeSpentHours(current.getTimeSpentHours() + hours);
      logger.info("skillRepository.save(current={})", current);
      skillRepository.save(current);
      skillsTouched++;

      ObjectId parentId = current.getParentSkillId();
      if (parentId == null) {
        break;
      }
      logger.info("skillRepository.findById(parentId={})", parentId);
      currentOptional = skillRepository.findById(parentId);
    }
    return skillsTouched;
  }

  /**
   * Remove a Skill by its Id. Also, set the children's parentSkillId to this Skill's parentSkillId
   * and recalculate the timeSpentHours for the parent Skill.
   *
   * @param skillId The Id of the Skill being deleted
   */
  @Transactional
  public void deleteById(ObjectId skillId) {
    logger.info("deleteById(skillId={})", skillId);
    logger.info("skillRepository.findById(skillId={})", skillId);
    Skill skill = skillRepository.findById(skillId)
        .orElseThrow(() -> new NotFoundException("skills", Map.of("skillId", skillId.toString())));

    // Recalculate parent timeSpentHours post delete, change subskills parent this -> this.parent
    logger.info("skillRepository.findByParentSkillId(skillId={})", skillId);
    List<Skill> subSkills = skillRepository.findByParentSkillId(skillId);
    for (Skill subSkill : subSkills) {
      subSkill.setParentSkillId(skill.getParentSkillId());
    }
    double hourDifference = skill.getTimeSpentHours()
        - subSkills.stream().collect(Collectors.summingDouble(Skill::getTimeSpentHours));
    if (skill.getParentSkillId() != null && skillRepository.existsById(skill.getParentSkillId())) {
      addHours(skill.getParentSkillId(), hourDifference * -1);
    }
    logger.info("skillRepository.saveAll(subSkills={})", subSkills);
    skillRepository.saveAll(subSkills);

    // Remove this skill from its Tree's Orientation
    logger.info("orientationRepository.findByUserIdAndTreeId(userId={}, treeId={})", skill.getUserId(), skill.getTreeId());
    Orientation orientation =
        orientationRepository.findByUserIdAndTreeId(skill.getUserId(), skill.getTreeId())
            .orElseThrow(() -> new NotFoundException("orientations",
                Map.of("treeId", skill.getTreeId().toString())));
    orientation.getSkillLocations().removeIf(sl -> sl.getSkillId().equals(skillId));
    logger.info("orientationRepository.save(orientation={})", orientation);
    orientationRepository.save(orientation);
    logger.info("skillRepository.deleteById(skillId={})", skillId);
    skillRepository.deleteById(skillId);
  }

  // No extra logic needed.
  public void deleteByUserId(ObjectId userId) {
    logger.info("deleteByUserId(userId={})", userId);
    logger.info("skillRepository.deleteByUserId(userId={})", userId);
    skillRepository.deleteByUserId(userId);
  }


  /**
   * Remove a Skill with matching userId and Id. Logic in the Transactional method above is applied.
   *
   * @param userId The Id of the User the Skill belongs to
   * @param skillId The Id of the Skill
   */
  public void deleteByUserIdAndId(ObjectId userId, ObjectId skillId) {
    logger.info("deleteByUserIdAndId(userId={}, skillId={})", userId, skillId);
    logger.info("skillRepository.findByUserIdAndId(userId={}, skillId={})", userId, skillId);
    Skill skill = skillRepository.findByUserIdAndId(userId, skillId)
        .orElseThrow(() -> new NotFoundException("skills",
            Map.of("userId", userId.toString(), "skillId", skillId.toString())));
    deleteById(skill.getId());
  }
}
