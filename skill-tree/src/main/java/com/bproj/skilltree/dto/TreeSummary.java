package com.bproj.skilltree.dto;

import com.bproj.skilltree.util.RegexPatterns;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Used to display top-level Tree information to Friends. Hidden fields are Id and Visibility.
 */
public class TreeSummary {
  @Pattern(regexp = RegexPatterns.TREE_NAME)
  @NotNull
  private String name;
  @Pattern(regexp = RegexPatterns.IMAGE_URL)
  @Nullable
  private String backgroundUrl;
  @Pattern(regexp = RegexPatterns.TREE_DESCRIPTION)
  @Nullable
  private String description;
  
  public TreeSummary() {}

  /**
   * Explicit value constructor.
   *
   * @param name The name of the Tree
   * @param backgroundUrl The background URL for the Tree
   * @param description The description of the Tree
   */
  public TreeSummary(String name, String backgroundUrl, String description) {
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.description = description;
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


}
