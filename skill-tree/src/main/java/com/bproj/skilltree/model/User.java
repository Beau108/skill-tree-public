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
 * An end user. Ties their firebase account to Skill Tree.
 */
@Document(collection = "users")
@ToString(onlyExplicitlyIncluded = true)
public class User {

  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  @ToString.Include
  private ObjectId id;
  @NotBlank
  private String firebaseId;
  @NotBlank
  @ToString.Include
  private String displayName;
  @NotBlank
  private String email;
  @ToString.Include
  private String profilePictureUrl;
  @CreatedDate
  @ToString.Include
  private Instant createdAt;
  @LastModifiedDate
  @ToString.Include
  private Instant updatedAt;

  public User() {}

  /**
   * Constructor with all required fields (rest filled by MongoDB).
   *
   * @param firebaseId The Id stored in firebase auth
   * @param displayName The displayName of the User
   * @param email The email tied to the firebase account
   * @param profilePictureUrl The user provided url for a profile picture.
   */
  public User(String firebaseId, String displayName, String email, String profilePictureUrl) {
    this.displayName = displayName;
    this.firebaseId = firebaseId;
    this.email = email;
    this.profilePictureUrl = profilePictureUrl;
  }
  
  /**
   * Create a User with DTO attributes only.
   *
   * @param displayName         The displayName of the User
   * @param profilePictureUrl   The profilePictureUrl of the User
   */
  public User(String displayName, String profilePictureUrl) {
    this.displayName = displayName;
    this.profilePictureUrl = profilePictureUrl;
  }

  public User(User other) {
      this.id = other.id;
      this.firebaseId = other.firebaseId;
      this.displayName = other.displayName;
      this.email = other.email;
      this.profilePictureUrl = other.profilePictureUrl;
      this.createdAt = other.createdAt;
      this.updatedAt = other.updatedAt;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getFirebaseId() {
    return firebaseId;
  }

  public void setFirebaseId(String firebaseId) {
    this.firebaseId = firebaseId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getProfilePictureUrl() {
    return profilePictureUrl;
  }

  public void setProfilePictureUrl(String newUrl) {
    this.profilePictureUrl = newUrl;
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
    if (!(o instanceof User)) {
      return false;
    }

    User other = (User) o;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
