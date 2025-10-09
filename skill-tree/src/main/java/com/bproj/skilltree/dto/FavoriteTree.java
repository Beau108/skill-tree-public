package com.bproj.skilltree.dto;

import org.bson.types.ObjectId;

/**
 * Combination of TreeResponse and TreeStats. Only sent to the owning User.
 */
public class FavoriteTree {
  private ObjectId treeId;
  private String name;
  private String backgroundUrl;
  private double totalTimeLogged;
  private int totalSkills;
  private int totalAchievements;
  private int achievementsCompleted;

  /**
   * Explicit value constructor.
   *
   * @param treeId The Id of the Tree
   * @param name The name of the Tree
   * @param backgroundUrl The URL for the background image of this Tree
   * @param totalTimeLogged The total hours spent through activities for this tree
   * @param totalSkills The total number of Skills belonging to this Tree
   * @param totalAchievements The total number of Achievements belonging to this Tree
   * @param achievementsCompleted The total number of Achievements completed in this Tree
   */
  public FavoriteTree(ObjectId treeId, String name, String backgroundUrl, double totalTimeLogged,
      int totalSkills, int totalAchievements, int achievementsCompleted) {
    this.treeId = treeId;
    this.name = name;
    this.backgroundUrl = backgroundUrl;
    this.totalTimeLogged = totalTimeLogged;
    this.totalSkills = totalSkills;
    this.totalAchievements = totalAchievements;
    this.achievementsCompleted = achievementsCompleted;
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

  public double getTotalTimeLogged() {
    return totalTimeLogged;
  }

  public void setTotalTimeLogged(double totalTimeLogged) {
    this.totalTimeLogged = totalTimeLogged;
  }

  public int getTotalSkills() {
    return totalSkills;
  }

  public void setTotalSkills(int totalSkills) {
    this.totalSkills = totalSkills;
  }

  public int getTotalAchievements() {
    return totalAchievements;
  }

  public void setTotalAchievements(int totalAchievements) {
    this.totalAchievements = totalAchievements;
  }

  public int getAchievementsCompleted() {
    return achievementsCompleted;
  }

  public void setAchievementsCompleted(int achievementsCompleted) {
    this.achievementsCompleted = achievementsCompleted;
  }
}
