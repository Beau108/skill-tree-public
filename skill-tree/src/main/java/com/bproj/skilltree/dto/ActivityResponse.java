package com.bproj.skilltree.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Outgoing essential Activity information. Uses WeightedSkills to avoid lookups on the client side.
 */
public class ActivityResponse {
  private String id;
  private String name;
  private String description;
  private double duration;
  private List<WeightedSkill> weightedSkills;

  /**
   * Explicit value constructor.
   *
   * @param id The ID of the Activity
   * @param name The name of the Activity
   * @param description The description of the Activity
   * @param duration The duration of the Activity
   * @param weightedSkills List of weighted Skills with full Skill information
   */
  @JsonCreator
  public ActivityResponse(@JsonProperty("id") String id, @JsonProperty("name") String name,
      @JsonProperty("description") String description, @JsonProperty("duration") double duration,
      @JsonProperty("weightedSkills") List<WeightedSkill> weightedSkills) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.weightedSkills = weightedSkills;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
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

  public List<WeightedSkill> getWeightedSkills() {
    return weightedSkills;
  }

  public void setWeightedSkills(List<WeightedSkill> weightedSkills) {
    this.weightedSkills = weightedSkills;
  }
}
