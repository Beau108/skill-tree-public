package com.bproj.skilltree.mapper;

import com.bproj.skilltree.dto.UserRequest;
import com.bproj.skilltree.dto.UserResponse;
import com.bproj.skilltree.model.User;

/**
 * Uses static methods to map Users into User DTOs and User DTOs into Users.
 */
public class UserMapper {
  private UserMapper() {}

  /**
   * Create a User from a UserRequest.
   *
   * @param userRequest The DTO of the User
   * @return    User created from the DTO
   */
  public static User toUser(UserRequest userRequest) {
    if (userRequest == null) {
      return null;
    }
    return new User(userRequest.getDisplayName(), userRequest.getProfilePictureUrl());
  }
  
  /**
   * Create a UserResponse from a User.
   *
   * @param user    The User to be converted
   * @return    The UserResponse resulting from the conversion
   */
  public static UserResponse fromUser(User user) {
    if (user == null) {
      return null;
    }
    return new UserResponse(user.getDisplayName(), user.getProfilePictureUrl());
  }
}
