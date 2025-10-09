package com.bproj.skilltree.dao;

import com.bproj.skilltree.model.Activity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * DB operations for Activities.
 */
@Repository("mongoActivityRepository")
public interface ActivityRepository extends MongoRepository<Activity, ObjectId> {
  boolean existsByUserIdAndId(ObjectId userId, ObjectId id);

  Optional<Activity> findByUserIdAndId(ObjectId userId, ObjectId id);

  List<Activity> findByUserId(ObjectId userId);

  List<Activity> findByUserIdAndSkillWeightsSkillId(ObjectId userId, ObjectId skillId);

  List<Activity> findByUserIdInAndCreatedAtBetween(List<ObjectId> userIds, Instant start,
      Instant end);

  void deleteByUserId(ObjectId userId);

  void deleteByUserIdAndId(ObjectId userId, ObjectId id);

  List<Activity> findByUserIdAndCreatedAtBetween(ObjectId userId, Instant start, Instant end);
}
