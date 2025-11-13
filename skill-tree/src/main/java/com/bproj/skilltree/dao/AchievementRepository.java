package com.bproj.skilltree.dao;

import com.bproj.skilltree.model.Achievement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * DB operations for Achievements.
 */
@Repository("mongoAchievementRepository")
public interface AchievementRepository extends MongoRepository<Achievement, ObjectId> {
  boolean existsByUserIdAndId(ObjectId userId, ObjectId id);

  Optional<Achievement> findByUserIdAndId(ObjectId userId, ObjectId id);

  List<Achievement> findByUserId(ObjectId userId);

  List<Achievement> findByUserIdAndPrerequisitesContaining(ObjectId userId,
      ObjectId prerequisiteId);

  List<Achievement> findByTreeId(ObjectId treeId);

  List<Achievement> findByUserIdAndTreeId(ObjectId userId, ObjectId treeId);

  Optional<Achievement> findByUserIdAndTreeIdAndId(ObjectId userId, ObjectId treeId, ObjectId id);

  List<Achievement> findByUserIdAndComplete(ObjectId userId, boolean complete);

  List<Achievement> findByUserIdAndTreeIdAndComplete(ObjectId userId, ObjectId treeId,
      boolean complete);

  List<Achievement> findByTitle(String title);

  List<Achievement> findByUserIdAndTitle(ObjectId userId, String title);

  List<Achievement> findByUserIdInAndCompletedAtBetween(List<ObjectId> userIds,
      Instant start, Instant end);

  void deleteByUserId(ObjectId userId);

  void deleteByUserIdAndTreeId(ObjectId userId, ObjectId treeId);
  
  void deleteByTreeId(ObjectId treeId);
}
