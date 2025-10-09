package com.bproj.skilltree.dao;

import com.bproj.skilltree.model.FriendRequestStatus;
import com.bproj.skilltree.model.Friendship;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * DB operations for Friends.
 */
@Repository("mongoFriendshipRepository")
public interface FriendshipRepository extends MongoRepository<Friendship, ObjectId> {
  List<Friendship> findByRequesterIdAndStatus(ObjectId requesterId, FriendRequestStatus status);

  List<Friendship> findByAddresseeIdAndStatus(ObjectId addresseeId, FriendRequestStatus status);

  List<Friendship> findByRequesterId(ObjectId requesterId);

  List<Friendship> findByAddresseeId(ObjectId addresseeId);

  List<Friendship> findByRequesterIdOrAddresseeIdAndStatus(ObjectId requesterId,
      ObjectId addresseeId, FriendRequestStatus status);

  Optional<Friendship> findByRequesterIdOrAddresseeIdAndId(ObjectId requesterId,
      ObjectId addresseeId, ObjectId id);

  List<Friendship> findByRequesterIdOrAddresseeId(ObjectId requesterId, ObjectId addresseeId);

  Optional<Friendship> findByRequesterIdAndAddresseeId(ObjectId requesterId, ObjectId addresseeId);

  boolean existsByRequesterIdAndAddresseeId(ObjectId requesterId, ObjectId addresseeId);

  boolean existsByRequesterIdAndAddresseeIdAndStatus(ObjectId requesterId, ObjectId addresseeId,
      FriendRequestStatus status);

  void deleteByRequesterIdOrAddresseeIdAndId(ObjectId requesterId, ObjectId addresseeId,
      ObjectId id);
}
