package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.SkillWeight;
import com.bproj.skilltree.util.RegexPatterns;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Incoming essential Activity information. Uses SkillWeights (skillId, weight)
 * to avoid sending
 * more data than needed.
 */
public class ActivityRequest {
  @Pattern(regexp = RegexPatterns.ACTIVITY_NAME)
  @NotNull
  private String name;
  @Pattern(regexp = RegexPatterns.ACTIVITY_DESCRIPTION)
  @Nullable
  private String description;
  @Positive
  @NotNull
  private double duration;
  @NotNull
  private List<SkillWeight> skillWeights;

  /**
   * Explicit value constructor.
   *
   * @param name         The name of the Activity
   * @param description  The description of the Activity
   * @param duration     The duration of the Activity
   * @param skillWeights List of Skills and their weights for this Activity
   */
  @JsonCreator
  public ActivityRequest(
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("duration") double duration,
      @JsonProperty("skillWeights") List<SkillWeight> skillWeights) {
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.skillWeights = skillWeights;
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
