package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.SkillWeight;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Incoming essential Activity information. Uses SkillWeights (skillId, weight)
 * to avoid sending
 * more data than needed.
 */
public class ActivityRequest {
  private ObjectId id;
  @NotBlank
  private String name;
  private String description;
  @NotNull
  @Positive
  private double duration;
  @NotNull
  @Valid
  private List<SkillWeight> skillWeights;

  /**
   * Explicit value constructor.
   *
   * @param id           The ID of the Activity
   * @param name         The name of the Activity
   * @param description  The description of the Activity
   * @param duration     The duration of the Activity
   * @param skillWeights List of Skills and their weights for this Activity
   */
  @JsonCreator
  public ActivityRequest(
      @JsonProperty("id") ObjectId id,
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("duration") double duration,
      @JsonProperty("skillWeights") List<SkillWeight> skillWeights) {
    this.id = id;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
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
}
