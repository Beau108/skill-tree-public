package com.bproj.skilltree.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * Shows the number of days in a row a User has logged an Activity and the number of Activities
 * logged per the last TODO days.
 */

public class RecentActivity {
  private int streak;
  private Map<LocalDate, Integer> dailyActivityCounts;

  public RecentActivity(int streak, Map<LocalDate, Integer> dailyActivityCounts) {
    this.streak = streak;
    this.dailyActivityCounts = dailyActivityCounts;
  }

  public int getStreak() {
    return streak;
  }

  public void setStreak(int streak) {
    this.streak = streak;
  }

  public Map<LocalDate, Integer> getDailyActivityCounts() {
    return dailyActivityCounts;
  }

  public void setDailyActivityCounts(Map<LocalDate, Integer> dailyActivityCounts) {
    this.dailyActivityCounts = dailyActivityCounts;
  }
}
