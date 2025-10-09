package com.bproj.skilltree.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.bson.types.ObjectId;

/**
 * Incoming essential Skill information.
 */
public class SkillRequest {
  private ObjectId id;
  @NotNull
  private ObjectId treeId;
  @NotBlank
  private String name;
  private String backgroundUrl;
  @NotNull
  @PositiveOrZero
  private double timeSpentHours;
  private ObjectId parentSkillId;

  /**
   * Explicit value constructor.
   *
   * @param id             The ID of the Skill
   * @param treeId         The ID of the Tree this Skill belongs to
   * @param name           The name of the Skill
   * @param backgroundUrl  The background URL for the Skill
   * @param timeSpentHours The time spent on this Skill in hours
   * @param parentSkillId  The ID of the parent Skill (null if root)
   */
  @JsonCreator
  public SkillRequest(
      @JsonProperty("id") ObjectId id,
      @JsonProperty("treeId") ObjectId treeId,
      @JsonProperty("name") String name,
      @JsonProperty("backgroundUrl") String backgroundUrl,
      @JsonProperty("timeSpentHours") double timeSpentHours,
      @JsonProperty("parentSkillId") ObjectId parentSkillId) {
    this.id = id;
    this.treeId = treeId;
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.timeSpentHours = timeSpentHours;
    this.parentSkillId = parentSkillId;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
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

  public void setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
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
}
