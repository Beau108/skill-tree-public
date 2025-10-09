package com.bproj.skilltree.dao;

import com.bproj.skilltree.model.Skill;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * DB operations for skills.
 */
@Repository("mongoSkillRepository")
public interface SkillRepository extends MongoRepository<Skill, ObjectId> {
  boolean existsByUserIdAndId(ObjectId userId, ObjectId id);
  
  Optional<Skill> findByUserIdAndId(ObjectId userId, ObjectId id);
  
  List<Skill> findByIdIn(List<ObjectId> ids);

  List<Skill> findByUserId(ObjectId userId);

  List<Skill> findByUserIdAndParentSkillId(ObjectId userId, ObjectId parentSkillId);

  List<Skill> findByUserIdAndParentSkillIdIsNull(ObjectId userId);
  
  List<Skill> findByTreeId(ObjectId treeId);
  
  List<Skill> findByTreeIdAndParentSkillIdIsNull(ObjectId treeId);
  
  List<Skill> findByUserIdAndTreeId(ObjectId userId, ObjectId treeId);

  List<Skill> findByName(String name);

  List<Skill> findByUserIdAndName(ObjectId userId, String name);

  List<Skill> findByUserIdAndParentSkillIdIsNotNull(ObjectId userId);
  
  void deleteByUserIdAndId(ObjectId userId, ObjectId id);
  
  void deleteByUserId(ObjectId userId);
}
