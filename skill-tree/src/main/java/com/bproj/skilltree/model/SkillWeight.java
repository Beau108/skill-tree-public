package com.bproj.skilltree.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

/**
 * For Activities, the proportion of how much a specific skill was used.
 */
public class SkillWeight {
  @NotNull
  private ObjectId skillId;
  @NotNull
  @Min(0)
  @Max(1)
  private double weight;

  public SkillWeight() {
  }

  /**
   * Explicit value constructor.
   *
   * @param skillId The ID of the Skill
   * @param weight  The weight/proportion of how much this Skill was used (0-1)
   */
  @JsonCreator
  public SkillWeight(
      @JsonProperty("skillId") ObjectId skillId,
      @JsonProperty("weight") double weight) {
    this.skillId = skillId;
    this.weight = weight;
  }

  public ObjectId getSkillId() {
    return skillId;
  }

  public void setSkillId(ObjectId skillId) {
    this.skillId = skillId;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
}
