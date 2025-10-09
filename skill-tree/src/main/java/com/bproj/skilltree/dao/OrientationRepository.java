package com.bproj.skilltree.dao;

import com.bproj.skilltree.model.Orientation;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * DB operations for Orientations.
 */
@Repository("mongoOrientationRepository")
public interface OrientationRepository extends MongoRepository<Orientation, ObjectId> {
  Optional<Orientation> findByTreeId(ObjectId treeId);

  Optional<Orientation> findByUserIdAndTreeId(ObjectId userId, ObjectId treeId);

  Optional<Orientation> findByUserIdAndId(ObjectId userId, ObjectId id);

  List<Orientation> findByUserId(ObjectId userId);
  
  void deleteByUserId(ObjectId userId);
}
