package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.SkillRepository;
import com.bproj.skilltree.dao.TreeRepository;
import com.bproj.skilltree.dao.UserRepository;
import com.bproj.skilltree.dto.SkillResponse;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.mapper.SkillMapper;
import com.bproj.skilltree.model.Skill;
import com.bproj.skilltree.model.SkillSortMode;
import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.util.JsonMergePatchUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implements business logic for 'skills' collection.
 */
@Service
public class SkillService {
  private final SkillRepository skillRepository;
  private final UserRepository userRepository;
  private final TreeRepository treeRepository;
  private static final String NAME_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,25}$";
  private static final String URL_REGEX =
      "^(https?://)?([a-zA-Z0-9.-]+\\.)?skilltree\\.com(/.*)?$";

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
      @Qualifier("mongoTreeRepository") TreeRepository treeRepository) {
    this.skillRepository = skillRepository;
    this.userRepository = userRepository;
    this.treeRepository = treeRepository;
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
      throw new BadRequestException("userId must reference an existing user.");
    }

    // treeId
    Optional<Tree> optionalTree = treeRepository.findById(treeId);
    if (optionalTree.isEmpty()) {
      throw new BadRequestException("treeId must reference an existing tree.");
    }
    Tree tree = optionalTree.get();
    if (!tree.getUserId().equals(userId)) {
      throw new BadRequestException(
          "A skill's tree must be owned by the same user that owns the skill.");
    }

    // name
    Pattern ptName = Pattern.compile(NAME_REGEX);
    Matcher mtName = ptName.matcher(skill.getName());
    if (!mtName.matches()) {
      throw new BadRequestException("Skill name must be 1-25 characters.");
    }

    // URL
    Pattern ptUrl = Pattern.compile(URL_REGEX);
    if (skill.getBackgroundUrl() != null) {
      Matcher mtUrl = ptUrl.matcher(skill.getBackgroundUrl());
      if (!mtUrl.matches()) {
        throw new BadRequestException("URL must belong to skilltree");
      }
    }

    // time
    if (skill.getTimeSpentHours() < 0) {
      throw new BadRequestException("Time spent must be >= 0.");
    }

    // parentSkillId
    ObjectId parentSkillId = skill.getParentSkillId();
    if (parentSkillId != null) {
      Optional<Skill> optionalParent = skillRepository.findByUserIdAndId(userId, parentSkillId);
      if (optionalParent.isEmpty()) {
        throw new NotFoundException(
            Map.of("userId", userId.toString(), "skillId (parent)", parentSkillId.toString()));
      }
      Skill parent = optionalParent.get();
      if (!parent.getTreeId().equals(treeId)) {
        throw new BadRequestException("Parent skill must have a matching treeId.");
      }
    }
  }

  public Skill create(Skill skill) {
    validateSkill(skill);
    return skillRepository.insert(skill);
  }

  public SkillResponse createResponse(Skill skill) {
    return SkillMapper.fromSkill(create(skill));
  }

  /**
   * Create a SkillResponse DTO by inserting a new Skill with a provided userId.
   *
   * @param userId The Id of the User the new Skill belongs to
   * @param skill The new Skill
   * @return The created Skill's SkillResponse representation
   */
  public SkillResponse createResponse(ObjectId userId, Skill skill) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException(Map.of("userId", userId.toString()));
    }
    skill.setUserId(userId);
    return createResponse(skill);
  }

  public boolean existsById(ObjectId skillId) {
    return skillRepository.existsById(skillId);
  }

  public boolean existsByUserIdAndId(ObjectId userId, ObjectId id) {
    return skillRepository.existsByUserIdAndId(userId, id);
  }

  /**
   * Find a Skill by Id.
   *
   * @param skillId The Id of the desired Skill
   * @return The found Skill. Throws NFE otherwise.
   */
  public Skill getEntityById(ObjectId skillId) {
    return skillRepository.findById(skillId)
        .orElseThrow(() -> new NotFoundException(Map.of("skillId", skillId.toString())));
  }

  public SkillResponse getResponseById(ObjectId skillId) {
    return SkillMapper.fromSkill(getEntityById(skillId));
  }

  public List<Skill> getAllEntities() {
    return skillRepository.findAll();
  }

  public List<SkillResponse> getAllResponses() {
    return getAllEntities().stream().map(SkillMapper::fromSkill).toList();
  }

  /**
   * Find all Skills for a given userId.
   *
   * @param userId The Id for matching Skills
   * @return The List of Skills with a matching userId
   */
  public List<Skill> getEntitiesByUserId(ObjectId userId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException(Map.of("userId", userId.toString()));
    }
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
  public List<Skill> getEntitiesByUserId(ObjectId userId, ObjectId parentSkillId, Boolean root) {
    if (parentSkillId != null) {
      return skillRepository.findByUserIdAndParentSkillId(userId, parentSkillId);
    }
    if (Boolean.TRUE.equals(root)) {
      return skillRepository.findByUserIdAndParentSkillIdIsNull(userId);
    } else if (Boolean.FALSE.equals(root)) {
      return skillRepository.findByUserIdAndParentSkillIdIsNotNull(userId);
    }
    return skillRepository.findByUserId(userId);
  }

  public List<SkillResponse> getResponsesByUserId(ObjectId userId) {
    return getEntitiesByUserId(userId).stream().map(SkillMapper::fromSkill).toList();
  }

  public List<SkillResponse> getResponsesByUserId(ObjectId userId, ObjectId parentSkillId,
      Boolean root) {
    return getEntitiesByUserId(userId, parentSkillId, root).stream().map(SkillMapper::fromSkill)
        .toList();
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
  public List<Skill> entityQuery(ObjectId userId, ObjectId parentSkillId, Boolean root,
      SkillSortMode sortMode) {
    List<Skill> skills = getEntitiesByUserId(userId, parentSkillId, root);
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
        throw new BadRequestException("SkillSortMode: " + sortMode.toString() + " not recognized.");
    }
  }

  public List<SkillResponse> responseQuery(ObjectId userId, ObjectId parentSkillId, Boolean root,
      SkillSortMode sortMode) {
    return entityQuery(userId, parentSkillId, root, sortMode).stream().map(SkillMapper::fromSkill)
        .toList();
  }

  /**
   * Finds an existing skill by UserId and SkillId.
   *
   * @param userId The id of the user
   * @param skillId The id of the skill
   * @return The skill with matching userid and id. Null if not found.
   */
  public Skill getEntityByUserIdAndId(ObjectId userId, ObjectId skillId) {
    return skillRepository.findByUserIdAndId(userId, skillId)
        .orElseThrow(() -> new NotFoundException(
            Map.of("userId", userId.toString(), "skillId", skillId.toString())));
  }

  public SkillResponse getResponseByUserIdAndId(ObjectId userId, ObjectId skillId) {
    return SkillMapper.fromSkill(getEntityByUserIdAndId(userId, skillId));
  }

  public Skill update(Skill updatedSkill) {
    validateSkill(updatedSkill);
    return skillRepository.save(updatedSkill);
  }

  /**
   * 'Safe update'. Updates a Skill given a userId, skillId, and Skill.
   *
   * @param userId The Id of the User the Skill belongs to
   * @param skillId The Id of the Skill begin updated
   * @param updatedSkill The updated version of the Skill entity
   * @return The Skill that was updated
   */
  public Skill update(ObjectId userId, ObjectId skillId, Skill updatedSkill) {
    if (!skillRepository.existsByUserIdAndId(userId, skillId)) {
      throw new NotFoundException(
          Map.of("userId", userId.toString(), "skillId", skillId.toString()));
    }
    updatedSkill.setUserId(userId);
    updatedSkill.setId(skillId);
    validateSkill(updatedSkill);
    return skillRepository.save(updatedSkill);
  }

  public SkillResponse updateResponse(ObjectId userId, ObjectId skillId, Skill updatedSkill) {
    return SkillMapper.fromSkill(update(userId, skillId, updatedSkill));
  }
  
  /**
   * Partially update a Skill.
   *
   * @param userId The User this Skill belongs to
   * @param skillId The Id of the Skill to be updated
   * @param updates The updates to be applied to the Skill
   * @return The updated Skill
   */
  public Skill patch(ObjectId userId, ObjectId skillId, JsonMergePatch updates) {
    Skill skill =
        skillRepository.findByUserIdAndId(userId, skillId).orElseThrow(() -> new NotFoundException(
            Map.of("userId", userId.toString(), "skillId", skillId.toString())));
    Skill updated = JsonMergePatchUtils.applyMergePatch(updates, skill, Skill.class);
    updated.setId(skillId);
    updated.setUserId(userId);
    validateSkill(updated);
    return skillRepository.save(updated);
  }
  
  public SkillResponse patchResponse(ObjectId userId, ObjectId skillId, JsonMergePatch updates) {
    return SkillMapper.fromSkill(patch(userId, skillId, updates));
  }

  public void deleteById(ObjectId skillId) {
    skillRepository.deleteById(skillId);
  }

  public void deleteByUserId(ObjectId userId) {
    skillRepository.deleteByUserId(userId);
  }
  
  public void deleteByUserIdAndId(ObjectId userId, ObjectId skillId) {
    skillRepository.deleteByUserIdAndId(userId, skillId);
  }
}
