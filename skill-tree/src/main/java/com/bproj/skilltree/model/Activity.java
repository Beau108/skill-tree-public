package com.bproj.skilltree.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An activity completed by the User that used one or more of their Skills.
 */
@Document(collection = "activities")
public class Activity {
  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId id;
  @JsonSerialize(using = ToStringSerializer.class)
  @NotBlank
  private ObjectId userId;
  @NotBlank
  private String name;
  private String description;
  @NotBlank
  private double duration;
  @NotBlank
  @JsonSerialize(contentUsing = ToStringSerializer.class)
  private List<SkillWeight> skillWeights;
  @CreatedDate
  private Instant createdAt;
  @LastModifiedDate
  private Instant updatedAt;

  public Activity() {}

  /**
   * Explicit value constructor.
   *
   * @param userId The User this Activity belongs to
   * @param name The name of this Activity
   * @param description A brief summary of this Activity
   * @param duration How long the User participated in this Activity
   * @param skillWeights The proportion of each Skills use (and thus time spent)
   */
  public Activity(ObjectId userId, String name, String description, double duration,
      List<SkillWeight> skillWeights) {
    this.userId = userId;
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.skillWeights = skillWeights != null ? skillWeights : new ArrayList<SkillWeight>();
  }

  /**
   * Create Activity from DTO.
   *
   * @param name The name of this Activity
   * @param description A summary of what this Activity was
   * @param duration The duration of this activity
   * @param skillWeights The proportion of each Skill's use
   */
  public Activity(@NotBlank String name, String description, @NotBlank double duration,
      @NotBlank List<SkillWeight> skillWeights) {
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.skillWeights = skillWeights;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getDuration() {
    return duration;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public List<SkillWeight> getSkillWeights() {
    return skillWeights;
  }

  public void setSkillWeights(List<SkillWeight> skillWeights) {
    this.skillWeights = skillWeights;
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
    if (!(o instanceof Activity)) {
      return false;
    }

    Activity other = (Activity) o;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
