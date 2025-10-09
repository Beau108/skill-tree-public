package com.bproj.skilltree.model;

/**
 * List of possible options for an objects visibility.
 * PRIVATE: Only the user has access to this object
 * FRIENDS: Only the user's friends can see this object
 * PUBLIC: Everybody can see this object (via explore page)
 * PRESET: This object doesn't belong to a user.
 */
public enum Visibility {
  PRIVATE,
  FRIENDS,
  PUBLIC,
  PRESET
}
