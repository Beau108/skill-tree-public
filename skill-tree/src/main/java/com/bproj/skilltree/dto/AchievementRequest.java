package com.bproj.skilltree.dto;

import com.bproj.skilltree.util.RegexPatterns;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Incoming essential Achievement information.
 */
public class AchievementRequest {
  @NotNull
  private String treeId;
  @Pattern(regexp = RegexPatterns.ACHIEVEMENT_TITLE)
  @NotNull
  private String title;
  @Pattern(regexp = RegexPatterns.IMAGE_URL)
  @Nullable
  private String backgroundUrl;
  @Pattern(regexp = RegexPatterns.ACHIEVEMENT_DESCRIPTION)
  @Nullable
  private String description;
  @NotNull
  private List<String> prerequisites;
  @NotNull
  private boolean complete;
  @Nullable
  private Instant completedAt;

  /**
   * Explicit value constructor.
   *
   * @param treeId The ID of the Tree this Achievement belongs to
   * @param title The title of the Achievement
   * @param backgroundUrl The background URL for the Achievement
   * @param description The description of the Achievement
   * @param prerequisites List of prerequisite Achievement IDs
   * @param complete Whether the Achievement is complete
   * @param completedAt Timestamp when the Achievement was completed
   */
  @JsonCreator
  public AchievementRequest(
      @JsonProperty("treeId") String treeId, @JsonProperty("title") String title,
      @JsonProperty("backgroundUrl") String backgroundUrl,
      @JsonProperty("description") String description,
      @JsonProperty("prerequisites") List<String> prerequisites,
      @JsonProperty("complete") boolean complete,
      @JsonProperty("completedAt") Instant completedAt) {
    this.treeId = treeId;
    this.title = title;
    this.backgroundUrl = backgroundUrl;
    this.description = description;
    this.prerequisites = prerequisites;
    this.complete = complete;
    this.completedAt = completedAt;
  }

  public String getTreeId() {
    return treeId;
  }

  public void setTreeId(String treeId) {
    this.treeId = treeId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public void setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getPrerequisites() {
    return prerequisites;
  }

  public void setPrerequisites(List<String> prerequisites) {
    this.prerequisites = prerequisites;
  }

  public boolean isComplete() {
    return complete;
  }

  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }
}
