package com.bproj.skilltree.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Outgoing essential Achievement information. Includes Id so the User it is sent to (the owner of
 * this Achievement) can send back requests with this Achievement's Id for edit operations.
 */
public class AchievementResponse {
  private String id;
  private String treeId;
  private String title;
  private String backgroundUrl;
  private String description;
  private List<String> prerequisites;
  private boolean complete;
  private Instant completedAt;

  /**
   * Explicit value constructor.
   *
   * @param id The ID of the Achievement
   * @param treeId The ID of the Tree this Achievement belongs to
   * @param backgroundUrl The background URL for the Achievement
   * @param description The description of the Achievement
   * @param prerequisites List of prerequisite Achievement IDs
   * @param complete Whether the Achievement is complete
   * @param completedAt Timestamp when the Achievement was completed
   */
  @JsonCreator
  public AchievementResponse(
      @JsonProperty("id") String id,
      @JsonProperty("treeId") String treeId,
      @JsonProperty("title") String title,
      @JsonProperty("backgroundUrl") String backgroundUrl,
      @JsonProperty("description") String description,
      @JsonProperty("prerequisites") List<String> prerequisites,
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
