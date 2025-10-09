package com.bproj.skilltree.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a specific skill, 'owns' subskills, achievements, and an orientation.
 */
@Document(collection = "trees")
public class Tree {
  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId id;
  @JsonSerialize(using = ToStringSerializer.class)
  @NotBlank
  private ObjectId userId;
  @NotBlank
  private String name;
  private String backgroundUrl;
  private String description;
  @NotBlank
  private Visibility visibility;
  @CreatedDate
  private Instant createdAt;
  @LastModifiedDate
  private Instant updatedAt;
  
  public Tree() {}

  /**
   * Create a new Tree. Id, createdAt, and updatedAt filled automatically.
   *
   * @param userId  The Id of the User this Tree belongs to
   * @param name    The name of this tree
   * @param backgroundUrl   The image display in the background of this tree
   * @param description Details of the contents of the tree
   * @param visibility  Who can see this tree
   */
  public Tree(ObjectId userId, String name, String backgroundUrl, String description,
      Visibility visibility) {
    this.userId = userId;
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.description = description;
    this.visibility = visibility;
  }
  
  /**
   * Create a new Tree from DTO fields.
   *
   * @param name            The name of the Tree
   * @param backgroundUrl   The backgroundUrl of the Tree
   * @param description     The description of the Tree
   * @param visibility      The visibility of the Tree
   */
  public Tree(String name, String backgroundUrl, String description, Visibility visibility) {
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.description = description;
    this.visibility = visibility;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Tree)) {
      return false;
    }
    
    Tree otherTree = (Tree) o;
    return otherTree.getId() != null ? id.equals(otherTree.getId()) : false;
  }
}
