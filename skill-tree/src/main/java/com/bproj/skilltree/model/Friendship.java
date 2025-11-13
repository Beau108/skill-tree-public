package com.bproj.skilltree.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * A friend request sent from one user to another.
 */
@Document(collection = "friends")
@ToString(onlyExplicitlyIncluded = true)
public class Friendship {
  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  @ToString.Include
  private ObjectId id;
  @NotBlank
  @ToString.Include
  private ObjectId requesterId;
  @NotBlank
  @ToString.Include
  private ObjectId addresseeId;
  @NotBlank
  @ToString.Include
  private FriendRequestStatus status;
  @CreatedDate
  @ToString.Include
  private Instant createdAt;
  @LastModifiedDate
  @ToString.Include
  private Instant updatedAt;

  public Friendship() {}

  /**
   * Explicit value constructor.
   *
   * @param requesterId The Id of the User who sent this request
   * @param addresseeId The Id of the User receiving this request
   * @param status      The status (PENDING | ACCEPTED | BLOCKED) of this request
   */
  public Friendship(ObjectId requesterId, ObjectId addresseeId, FriendRequestStatus status) {
    this.requesterId = requesterId;
    this.addresseeId = addresseeId;
    this.status = status;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public ObjectId getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(ObjectId requesterId) {
    this.requesterId = requesterId;
  }

  public ObjectId getAddresseeId() {
    return addresseeId;
  }

  public void setAddresseeId(ObjectId addresseeId) {
    this.addresseeId = addresseeId;
  }

  public FriendRequestStatus getStatus() {
    return status;
  }

  public void setStatus(FriendRequestStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Friendship)) {
      return false;
    }

    Friendship other = (Friendship) o;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
