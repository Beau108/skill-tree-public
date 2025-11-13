package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.Visibility;
import com.bproj.skilltree.util.RegexPatterns;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.bson.types.ObjectId;

/**
 * Incoming essential Tree information.
 */
public class TreeRequest {
  @Pattern(regexp = RegexPatterns.TREE_NAME)
  @NotNull
  private String name;
  @Pattern(regexp = RegexPatterns.IMAGE_URL)
  @Nullable
  private String backgroundUrl;
  @Pattern(regexp = RegexPatterns.TREE_DESCRIPTION)
  @Nullable
  private String description;
  @NotNull
  private Visibility visibility;

  /**
   * Explicit value constructor.
   *
   * @param name          The name of the Tree
   * @param backgroundUrl The background URL for the Tree
   * @param description   The description of the Tree
   * @param visibility    The visibility setting of the Tree
   */
  @JsonCreator
  public TreeRequest(
      @JsonProperty("name") String name,
      @JsonProperty("backgroundUrl") String backgroundUrl,
      @JsonProperty("description") String description,
      @JsonProperty("visibility") Visibility visibility) {
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.description = description;
    this.visibility = visibility;
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
