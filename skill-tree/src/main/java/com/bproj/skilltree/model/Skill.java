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
 * A skill. Tied to a User and a Tree. Has a name, tracks hours spent, and optionally a background
 * image URL. image url.
 */
@Document(collection = "skills")
@ToString(onlyExplicitlyIncluded = true)
public class Skill {
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
  @NotBlank
  @ToString.Include
  private String name;
  @ToString.Include
  private String backgroundUrl;
  @NotBlank
  @ToString.Include
  private double timeSpentHours;
  @JsonSerialize(using = ToStringSerializer.class)
  @ToString.Include
  private ObjectId parentSkillId;
  @CreatedDate
  @ToString.Include
  private Instant createdAt;
  @LastModifiedDate
  @ToString.Include
  private Instant updatedAt;


  public Skill() {}

  /**
   * Explicit value constructor. Attributes not included are auto generated.
   *
   * @param userId The Id of the User this Skill belongs to.
   * @param treeId The Id of the Tree this Skill belongs to.
   * @param name The name of this skill
   * @param backgroundUrl The URL for the background image of this skill
   * @param timeSpentHours The amount of time the User has logged for this skill through Activities
   * @param parentSkillId The Skill that this Skill is a subskill to. Null if this skill belongs at
   *        the top of the Tree.
   */
  public Skill(ObjectId userId, ObjectId treeId, String name, String backgroundUrl,
      double timeSpentHours, ObjectId parentSkillId) {
    this.userId = userId;
    this.treeId = treeId;
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.timeSpentHours = timeSpentHours;
    this.parentSkillId = parentSkillId;
  }

  /**
   * From DTO constructor. Requires field assignment post creation.
   *
   * @param treeId          The Id of the Tree this Skill belongs to
   * @param name            The name of the Skill
   * @param backgroundUrl   The url for the background image of this skill
   * @param timeSpentHours  The amount of time the User has logged for this Skill
   * @param parentSkillId   The Skill that this Skill is a subskill to
   */
  public Skill(ObjectId treeId, String name, String backgroundUrl, double timeSpentHours,
      ObjectId parentSkillId) {
    this.treeId = treeId;
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.timeSpentHours = timeSpentHours;
    this.parentSkillId = parentSkillId;
  }

  public Skill(Skill other) {
      this.id = other.id;
      this.userId = other.userId;
      this.treeId = other.treeId;
      this.name = other.name;
      this.backgroundUrl = other.backgroundUrl;
      this.timeSpentHours = other.timeSpentHours;
      this.parentSkillId = other.parentSkillId;
      this.createdAt = other.createdAt;
      this.updatedAt = other.updatedAt;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId skillId) {
    this.id = skillId;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public void setBackgroundUrl(String newUrl) {
    this.backgroundUrl = newUrl;
  }

  public double getTimeSpentHours() {
    return timeSpentHours;
  }

  public void setTimeSpentHours(double timeSpentHours) {
    this.timeSpentHours = timeSpentHours;
  }

  public ObjectId getParentSkillId() {
    return parentSkillId;
  }

  public void setParentSkillId(ObjectId parentSkillId) {
    this.parentSkillId = parentSkillId;
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
    if (!(o instanceof Skill)) {
      return false;
    }
    Skill other = (Skill) o;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
