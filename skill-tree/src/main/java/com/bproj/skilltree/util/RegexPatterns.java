package com.bproj.skilltree.util;

/**
 * All regex patterns used, stored as static Strings.
 */
public class RegexPatterns {
  private RegexPatterns() {}
  
  public static final String DISPLAY_NAME = "^[A-Za-z0-9._]{3,30}$";
  public static final String IMAGE_URL = "^(https?://)?([a-zA-Z0-9.-]+\\.)?skilltree\\.com(/.*)?$";
  public static final String ACHIEVEMENT_TITLE = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,50}$";
  public static final String ACHIEVEMENT_DESCRIPTION = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,500}$";
  public static final String ACTIVITY_NAME = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,50}$";
  public static final String ACTIVITY_DESCRIPTION = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,500}$";
  public static final String SKILL_NAME = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,50}$";
  public static final String TREE_NAME = "^[A-Za-z0-9._ ]{3,50}$";
  public static final String TREE_DESCRIPTION = "^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,500}$";
}
