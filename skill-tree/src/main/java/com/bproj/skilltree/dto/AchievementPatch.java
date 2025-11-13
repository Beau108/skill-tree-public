package com.bproj.skilltree.dto;

import com.bproj.skilltree.util.RegexPatterns;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public class AchievementPatch {
    @Pattern(regexp = RegexPatterns.ACHIEVEMENT_TITLE)
    @Getter
    @Setter
    private Optional<String> title;
    @Pattern(regexp = RegexPatterns.IMAGE_URL)
    @Getter
    @Setter
    private Optional<String> backgroundUrl;
    @Pattern(regexp = RegexPatterns.ACHIEVEMENT_DESCRIPTION)
    @Getter
    @Setter
    private Optional<String> description;
    @Getter
    @Setter
    private Optional<List<ObjectId>> prerequisites;
    @Getter
    @Setter
    private Optional<Boolean> complete;
}
