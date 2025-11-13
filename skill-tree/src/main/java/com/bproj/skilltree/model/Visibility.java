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
  PRESET;

    public static Visibility fromString(String s) {
        return switch (s) {
            case "PRIVATE" -> Visibility.PRIVATE;
            case "FRIENDS" -> Visibility.FRIENDS;
            case "PUBLIC" -> Visibility.PUBLIC;
            case "PRESET" -> Visibility.PRESET;
            default -> throw new IllegalArgumentException("Unrecognized Visibility: " + s);
        };
    }
}
