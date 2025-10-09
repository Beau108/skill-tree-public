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
 * An accomplishment associated with a Tree. Belongs to a User and a Tree.
 */
@Document(collection = "achievements")
public class Achievement {
  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId id;
  @JsonSerialize(using = ToStringSerializer.class)
  @NotBlank
  private ObjectId userId;
  @JsonSerialize(using = ToStringSerializer.class)
  @NotBlank
  private ObjectId treeId;
  @NotBlank
  private String title;
  private String backgroundUrl;
  private String description;
  @JsonSerialize(contentUsing = ToStringSerializer.class)
  @NotBlank
  private List<ObjectId> prerequisites = new ArrayList<ObjectId>();
  @NotBlank
  private boolean complete;
  private Instant completedAt;
  @CreatedDate
  private Instant createdAt;
  @LastModifiedDate
  private Instant updatedAt;

  public Achievement() {}


  /**
   * Explicit value constructor.
   *
   * @param userId The User this Achievement belongs to
   * @param treeId The Tree this Achievement belongs to
   * @param title The name of this Achievement
   * @param backgroundUrl The URL for this Achievements background
   * @param description A brief description of the requirements to complete this Achievement
   * @param prerequisites A list of ObjectIds referencing Achievements that must be completed before
   *        this one
   * @param complete Whether or not this Achievement is complete
   */
  public Achievement(ObjectId userId, ObjectId treeId, String title, String backgroundUrl,
      String description, List<ObjectId> prerequisites, boolean complete) {
    this.userId = userId;
    this.treeId = treeId;
    this.title = title;
    this.backgroundUrl = backgroundUrl;
    this.description = description;
    this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<ObjectId>();
    this.complete = complete;
  }

  /**
   * Constructor from DTO.
   *
   * @param treeId          The Id of the Tree this Achievement belongs to
   * @param backgroundUrl   The URL for the background image of this Achievement
   * @param description     The description of what it means to complete this Achievement
   * @param prerequisites   The prerequisite Achievements for this Achievement
   * @param complete        Whether this Achievement has been completed or not
   * @param completedAt     When this Achievement was completed. Null if incomplete
   */
  public Achievement(ObjectId treeId, String backgroundUrl, String description,
      List<ObjectId> prerequisites, boolean complete, Instant completedAt) {
    this.treeId = treeId;
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

  public ObjectId getUserId() {
    return userId;
  }

  public void setUserId(ObjectId userId) {
    this.userId = userId;
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

  public void setBackgroundUrl(String newUrl) {
    this.backgroundUrl = newUrl;
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Achievement)) {
      return false;
    }

    Achievement other = (Achievement) o;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
