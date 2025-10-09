package com.bproj.skilltree.dto;

/**
 * Statistics for 1+ Tree(s).
 */
public class TreeStats {
  private double totalTimeLogged;
  private int totalSkills;
  private int totalAchievements;
  private int achievementsCompleted;

  public TreeStats() {}

  /**
   * Explicit value constructor.
   *
   * @param totalTimeLogged Total time logged across all Skills in hours
   * @param totalSkills Total number of Skills
   * @param totalAchievements Total number of Achievements
   * @param achievementsCompleted Number of completed Achievements
   */
  public TreeStats(double totalTimeLogged, int totalSkills, int totalAchievements,
      int achievementsCompleted) {
    this.totalTimeLogged = totalTimeLogged;
    this.totalSkills = totalSkills;
    this.totalAchievements = totalAchievements;
    this.achievementsCompleted = achievementsCompleted;
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
