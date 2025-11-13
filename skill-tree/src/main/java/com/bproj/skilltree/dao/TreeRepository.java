package com.bproj.skilltree.dao;

import com.bproj.skilltree.model.Tree;
import com.bproj.skilltree.model.Visibility;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * The DB access for Trees.
 */
@Repository("mongoTreeRepository")
public interface TreeRepository extends MongoRepository<Tree, ObjectId> {
  boolean existsByUserIdAndId(ObjectId userId, ObjectId id);

  Optional<Tree> findByUserIdAndId(ObjectId userId, ObjectId id);

  boolean existsByUserIdAndName(ObjectId userId, String name);

  Optional<Tree> findByUserIdAndName(ObjectId userId, String name);

  List<Tree> findByUserId(ObjectId userId);
  
  Page<Tree> findByUserId(ObjectId userId, Pageable pageable);

  List<Tree> findByUserIdInAndCreatedAtBetween(List<ObjectId> userIds, Instant start, Instant end);

  List<Tree> findByUserIdIsNull();

  Page<Tree> findByUserIdIsNull(Pageable pageable);

  Page<Tree> findByVisibility(Visibility visibility, Pageable pageable);
  
  void deleteByUserId(ObjectId userId);
  
  void deleteByUserIdAndId(ObjectId userId, ObjectId id);
}
