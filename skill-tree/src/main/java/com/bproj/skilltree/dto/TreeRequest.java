package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

/**
 * Incoming essential Tree information. Only used to create/edit requests thus
 * making Id necessary.
 */
public class TreeRequest {
  private ObjectId id;
  @NotBlank
  private String name;
  private String backgroundUrl;
  private String description;
  @NotNull
  private Visibility visibility;

  /**
   * Explicit value constructor.
   *
   * @param id            The ID of the Tree
   * @param name          The name of the Tree
   * @param backgroundUrl The background URL for the Tree
   * @param description   The description of the Tree
   * @param visibility    The visibility setting of the Tree
   */
  @JsonCreator
  public TreeRequest(
      @JsonProperty("id") ObjectId id,
      @JsonProperty("name") String name,
      @JsonProperty("backgroundUrl") String backgroundUrl,
      @JsonProperty("description") String description,
      @JsonProperty("visibility") Visibility visibility) {
    this.id = id;
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
}
