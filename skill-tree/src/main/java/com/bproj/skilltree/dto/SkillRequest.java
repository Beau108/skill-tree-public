package com.bproj.skilltree.dto;

import com.bproj.skilltree.util.RegexPatterns;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import org.bson.types.ObjectId;

/**
 * Incoming essential Skill information.
 */
public class SkillRequest {
  @NotNull
  private String treeId;
  @Pattern(regexp = RegexPatterns.SKILL_NAME)
  @NotNull
  private String name;
  @Pattern(regexp = RegexPatterns.IMAGE_URL)
  @Nullable
  private String backgroundUrl;
  @NotNull
  @PositiveOrZero
  private double timeSpentHours;
  private String parentSkillId;

  /**
   * Explicit value constructor.
   *
   * @param treeId         The ID of the Tree this Skill belongs to
   * @param name           The name of the Skill
   * @param backgroundUrl  The background URL for the Skill
   * @param timeSpentHours The time spent on this Skill in hours
   * @param parentSkillId  The ID of the parent Skill (null if root)
   */
  @JsonCreator
  public SkillRequest(
      @JsonProperty("treeId") String treeId,
      @JsonProperty("name") String name,
      @JsonProperty("backgroundUrl") String backgroundUrl,
      @JsonProperty("timeSpentHours") double timeSpentHours,
      @JsonProperty("parentSkillId") String parentSkillId) {
    this.treeId = treeId;
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.timeSpentHours = timeSpentHours;
    this.parentSkillId = parentSkillId;
  }

  public String getTreeId() {
    return treeId;
  }

  public void setTreeId(String treeId) {
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

  public String getParentSkillId() {
    return parentSkillId;
  }

  public void setParentSkillId(String parentSkillId) {
    this.parentSkillId = parentSkillId;
  }
}
