package com.bproj.skilltree.dto;

import java.time.Instant;

/**
 * Interface for any object that can appear in a Friend Activity feed.
 */
public interface FeedItem {
  public String getType();

  public String getDisplayName();

  public String getProfilePictureUrl();
  
  public Instant getPostedAt();

}
