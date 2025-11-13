package com.bproj.skilltree.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A representation of how each Skill and Achievement is displayed in a Tree. Matches Ids to
 * locations.
 */
@Document(collection = "orientations")
@ToString(onlyExplicitlyIncluded = true)
public class Orientation {
  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  @ToString.Include
  private ObjectId id;
  @JsonSerialize(using = ToStringSerializer.class)
  @NotBlank
  @ToString.Include
  private ObjectId userId;
  @JsonSerialize(using = ToStringSerializer.class)
  @NotBlank
  @ToString.Include
  private ObjectId treeId;
  @JsonSerialize(contentUsing = ToStringSerializer.class)
  @ToString.Include
  private List<SkillLocation> skillLocations = new ArrayList<>();
  @JsonSerialize(contentUsing = ToStringSerializer.class)
  @ToString.Include
  private List<AchievementLocation> achievementLocations = new ArrayList<>();
  @CreatedDate
  @ToString.Include
  private Instant createdAt;
  @LastModifiedDate
  @ToString.Include
  private Instant updatedAt;

  public Orientation() {}

  /**
   * Explicit value constructor.
   *
   * @param userId The User the this Orientation belongs to
   * @param treeId The Id of the Tree this Orientation belongs to
   * @param skillLocations Mapping of Skills to x and y values (location)
   * @param achievementLocations mapping of Achievements to x and y values
   */
  public Orientation(ObjectId userId, ObjectId treeId, List<SkillLocation> skillLocations,
      List<AchievementLocation> achievementLocations) {
    this.userId = userId;
    this.treeId = treeId;
    this.skillLocations = skillLocations;
    this.achievementLocations = achievementLocations;
  }

  /**
   * 2-arg constructor for an Orientation made from a naked Tree.
   *
   * @param userId The owning User's Id (same as tree's userId)
   * @param treeId The Id of the Tree this Orientation belong to
   */
  public Orientation(ObjectId userId, ObjectId treeId) {
    this.userId = userId;
    this.treeId = treeId;
    this.skillLocations = List.of();
    this.achievementLocations = List.of();
  }

  /**
   * 3-arg constructor for creating an Orientation from an OrientationRequest.
   *
   * @param treeId The Id of the Tree this Orientation belongs to
   * @param skillLocations Mapping of Skills to x and y values
   * @param achievementLocations Mapping of Achievements to x and y values
   */
  public Orientation(ObjectId treeId, List<SkillLocation> skillLocations,
      List<AchievementLocation> achievementLocations) {
    this.treeId = treeId;
    this.skillLocations = skillLocations;
    this.achievementLocations = achievementLocations;
  }

  public Orientation(Orientation other) {
      this.id = other.id;
      this.treeId = other.treeId;
      this.userId = other.userId;
      this.skillLocations = new ArrayList<>(other.skillLocations);
      this.achievementLocations = new ArrayList<>(other.achievementLocations);
      this.updatedAt = other.updatedAt;
      this.createdAt = other.createdAt;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public ObjectId getUserId() {
    return userId;
  }

  public void setUserId(ObjectId userId) {
    this.userId = userId;
  }

  public ObjectId getTreeId() {
    return treeId;
  }

  public void setTreeId(ObjectId treeId) {
    this.treeId = treeId;
  }

  public List<SkillLocation> getSkillLocations() {
    return skillLocations;
  }

  public void setSkillLocations(List<SkillLocation> skillLocations) {
    this.skillLocations = skillLocations;
  }

  public List<AchievementLocation> getAchievementLocations() {
    return achievementLocations;
  }

  public void setAchievementLocations(List<AchievementLocation> achievementLocations) {
    this.achievementLocations = achievementLocations;
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
    if (!(o instanceof Orientation)) {
      return false;
    }

    Orientation other = (Orientation) o;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
