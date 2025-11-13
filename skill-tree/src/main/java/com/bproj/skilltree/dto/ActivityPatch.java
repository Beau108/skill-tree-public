package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.SkillWeight;
import com.bproj.skilltree.util.RegexPatterns;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class ActivityPatch {
    @Pattern(regexp = RegexPatterns.ACTIVITY_NAME)
    @Getter
    @Setter
    private Optional<String> name;
    @Pattern(regexp = RegexPatterns.ACTIVITY_DESCRIPTION)
    @Getter
    @Setter
    private Optional<String> description;
    @Positive
    @Getter
    @Setter
    private Optional<Double> duration;
    @Getter
    @Setter
    private Optional<List<SkillWeight>> skillWeights;
}
