package com.bproj.skilltree.service;

import com.bproj.skilltree.dao.AchievementRepository;
import com.bproj.skilltree.dao.OrientationRepository;
import com.bproj.skilltree.dao.SkillRepository;
import com.bproj.skilltree.dao.TreeRepository;
import com.bproj.skilltree.dao.UserRepository;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.NotFoundException;
import com.bproj.skilltree.model.AchievementLocation;
import com.bproj.skilltree.model.Orientation;
import com.bproj.skilltree.model.SkillLocation;
import com.bproj.skilltree.util.JsonMergePatchUtils;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implements business logic for 'orientations' collection.
 */
@Service
public class OrientationService {
  private final OrientationRepository orientationRepository;
  private final TreeRepository treeRepository;
  private final SkillRepository skillRepository;
  private final AchievementRepository achievementRepository;
  private final UserRepository userRepository;

  /**
   * Create an OrientationService.
   *
   * @param orientationRepository DB operations for Orientations
   * @param treeRepository DB operations for Trees
   * @param skillRepository DB operations for Skills
   * @param achievementRepository DB operations for Achievements
   * @param userRepository DB operations for Users
   */
  @Autowired
  public OrientationService(
      @Qualifier("mongoOrientationRepository") OrientationRepository orientationRepository,
      @Qualifier("mongoTreeRepository") TreeRepository treeRepository,
      @Qualifier("mongoSkillRepository") SkillRepository skillRepository,
      @Qualifier("mongoAchievementRepository") AchievementRepository achievementRepository,
      @Qualifier("mongoUserRepository") UserRepository userRepository) {
    this.orientationRepository = orientationRepository;
    this.treeRepository = treeRepository;
    this.skillRepository = skillRepository;
    this.achievementRepository = achievementRepository;
    this.userRepository = userRepository;
  }

  /**
   * Validate an Orientation. userId must point to an existing user. treeId must point to an
   * existing tree of userId. Skill/Achievement Locations must reference valid objects belonging to
   * the same user. X and Y coordinates must be >= 0.
   *
   * @param orientation The Orientation to be validated
   */
  private void validateOrientation(Orientation orientation) {
    ObjectId userId = orientation.getUserId();

    // userId
    if (!userRepository.existsById(userId)) {
      throw new BadRequestException("userId must point to an existing user.");
    }

    // treeId
    ObjectId treeId = orientation.getTreeId();
    if (!treeRepository.existsByUserIdAndId(userId, treeId)) {
      throw new BadRequestException("treeId must point to an existing tree owned by userId");
    }

    // skillLocations
    List<SkillLocation> skillLocations = orientation.getSkillLocations();
    for (SkillLocation sl : skillLocations) {
      if (!skillRepository.existsByUserIdAndId(userId, sl.getSkillId())) {
        throw new BadRequestException(
            "SkillLocation skillId must reference an existing skill owned by userId.");
      }
      if (sl.getX() < 0 || sl.getY() < 0) {
        throw new BadRequestException("X and Y location must be > 0.");
      }
    }

    // achievementLocations
    List<AchievementLocation> achievementLocations = orientation.getAchievementLocations();
    for (AchievementLocation al : achievementLocations) {
      if (!achievementRepository.existsByUserIdAndId(userId, al.getAchievementId())) {
        throw new BadRequestException(
            "AchievementLocation achievementId must reference an existing skill owned by userId.");
      }
      if (al.getX() < 0 || al.getY() < 0) {
        throw new BadRequestException("X and Y location must be > 0.");
      }
    }
  }

  public Orientation create(Orientation orientation) {
    validateOrientation(orientation);
    return orientationRepository.insert(orientation);
  }

  public boolean existsById(ObjectId orientationId) {
    return orientationRepository.existsById(orientationId);
  }

  /**
   * Find an Orientation by Id.
   *
   * @param orientationId The Id of the desired Orientation
   * @return The Orientation associated with the provided Id. Throws NFE otherwise
   */
  public Orientation findById(ObjectId orientationId) {
    Optional<Orientation> optionalOrientation = orientationRepository.findById(orientationId);
    if (optionalOrientation.isEmpty()) {
      throw new NotFoundException(Map.of("orientationId", orientationId.toString()));
    }
    return optionalOrientation.get();
  }

  /**
   * Find an Orientation by userId and Id.
   *
   * @param userId The Id of the User the Orientation belongs to
   * @param orientationId The Id of the Orientation
   * @return The Orientation matching userId and Id. Throws NFE otherwise.
   */
  public Orientation findByUserIdAndId(ObjectId userId, ObjectId orientationId) {
    Orientation orientation = orientationRepository.findByUserIdAndId(userId, orientationId)
        .orElseThrow(() -> new NotFoundException(
            Map.of("userId", userId.toString(), "orientationId", orientationId.toString())));
    return orientation;
  }

  /**
   * Find all Orientations for a specific user given their Id.
   *
   * @param userId The Id of the User.
   * @return A list of the specified User's Orientations
   */
  public List<Orientation> findByUserId(ObjectId userId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException(Map.of("userId", userId.toString()));
    }
    return orientationRepository.findByUserId(userId);
  }

  /**
   * Get an Orientation by its UserId and TreeId.
   *
   * @param userId The Id of the User the Orientation belongs to
   * @param treeId The Id of the Tree the Orientation belongs to
   * @return The Orientation with userId and treeId matching
   */
  public Orientation findByUserIdAndTreeId(ObjectId userId, ObjectId treeId) {
    if (!treeRepository.existsByUserIdAndId(userId, treeId)) {
      throw new NotFoundException(Map.of("userId", userId.toString(), "treeId", treeId.toString()));
    }
    return orientationRepository.findByUserIdAndTreeId(userId, treeId)
        .orElseThrow(() -> new NotFoundException(
            Map.of("userId", userId.toString(), "treeId", treeId.toString())));
  }

  /**
   * Update an Orientation by its userId and treeId.
   *
   * @param userId The Id of the User that owns the Orientation
   * @param treeId The Id of the Tree that the Orientation belongs to
   * @param updatedOrientation The new version of the Orientation
   * @return The updated Orientation
   */
  public Orientation update(ObjectId userId, ObjectId treeId, Orientation updatedOrientation) {
    updatedOrientation.setUserId(userId);
    updatedOrientation.setTreeId(treeId);
    validateOrientation(updatedOrientation);
    return orientationRepository.save(updatedOrientation);
  }

  /**
   * Partially update the given Orientation.
   *
   * @param userId The Id of the User that the Orientation belongs to
   * @param treeId The Id of the Tree this orientation belongs to
   * @param updates The updates to be applied to the Orientation
   * @return The updated Orientation.
   */
  public Orientation patch(ObjectId userId, ObjectId treeId, JsonMergePatch updates) {
    Orientation updated = JsonMergePatchUtils.applyMergePatch(updates,
        findByUserIdAndTreeId(userId, treeId), Orientation.class);
    validateOrientation(updated);
    updated.setUserId(userId);
    updated.setTreeId(treeId);
    return orientationRepository.save(updated);

  }

  public void deleteById(ObjectId id) {
    orientationRepository.deleteById(id);
  }

  public void deleteByUserId(ObjectId userId) {
    orientationRepository.deleteByUserId(userId);
  }
}
