package com.bproj.skilltree.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Incoming essential Achievement information. Includes Id because this DTO is used for create/edit
 * requests.
 */
public class AchievementRequest {
  private ObjectId id;
  @NotNull
  private ObjectId treeId;
  @NotBlank
  private String title;
  private String backgroundUrl;
  private String description;
  private List<ObjectId> prerequisites;
  @NotNull
  private boolean complete;
  private Instant completedAt;

  /**
   * Explicit value constructor.
   *
   * @param id The ID of the Achievement
   * @param treeId The ID of the Tree this Achievement belongs to
   * @param title The title of the Achievement
   * @param backgroundUrl The background URL for the Achievement
   * @param description The description of the Achievement
   * @param prerequisites List of prerequisite Achievement IDs
   * @param complete Whether the Achievement is complete
   * @param completedAt Timestamp when the Achievement was completed
   */
  @JsonCreator
  public AchievementRequest(@JsonProperty("id") ObjectId id,
      @JsonProperty("treeId") ObjectId treeId, @JsonProperty("title") String title,
      @JsonProperty("backgroundUrl") String backgroundUrl,
      @JsonProperty("description") String description,
      @JsonProperty("prerequisites") List<ObjectId> prerequisites,
      @JsonProperty("complete") boolean complete,
      @JsonProperty("completedAt") Instant completedAt) {
    this.id = id;
    this.treeId = treeId;
    this.title = title;
    this.backgroundUrl = backgroundUrl;
    this.description = description;
    this.prerequisites = prerequisites;
    this.complete = complete;
    this.completedAt = completedAt;
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

  public List<ObjectId> getPrerequisites() {
    return prerequisites;
  }

  public void setPrerequisites(List<ObjectId> prerequisites) {
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
