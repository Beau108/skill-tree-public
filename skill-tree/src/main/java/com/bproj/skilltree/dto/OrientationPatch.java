package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.AchievementLocation;
import com.bproj.skilltree.model.SkillLocation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class OrientationPatch {
    @Getter
    @Setter
    private Optional<List<SkillLocation>> skillLocations;
    @Getter
    @Setter
    private Optional<List<AchievementLocation>> achievementLocation;
}
