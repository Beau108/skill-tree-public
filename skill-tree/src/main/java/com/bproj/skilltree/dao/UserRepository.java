package com.bproj.skilltree.dao;

import com.bproj.skilltree.model.User;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * DB Operations for Users.
 */
@Repository("mongoUserRepository")
public interface UserRepository extends MongoRepository<User, ObjectId> {
  Optional<User> findByFirebaseId(String firebaseId);

  Optional<User> findByEmail(String email);

  Optional<User> findByDisplayName(String displayName);
  
  List<User> findByIdIn(List<ObjectId> ids);
  
  boolean existsByDisplayName(String displayName);
  
  boolean existsByFirebaseId(String firebaseId);
}
