package com.bproj.skilltree.mapper;

import com.bproj.skilltree.dto.OrientationRequest;
import com.bproj.skilltree.model.Orientation;

/**
 * DTO conversion for Orientations.
 */
public class OrientationMapper {

  private OrientationMapper() {}

  public static Orientation toOrientation(OrientationRequest orientationRequest) {
    return new Orientation(orientationRequest.getTreeId(), orientationRequest.getSkillLocations(),
        orientationRequest.getAchievementLocations());
  }
}
