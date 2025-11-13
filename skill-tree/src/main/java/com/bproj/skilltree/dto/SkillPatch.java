package com.bproj.skilltree.dto;

import com.bproj.skilltree.util.RegexPatterns;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Optional;

public class SkillPatch {
    @Pattern(regexp = RegexPatterns.SKILL_NAME)
    @Getter
    @Setter
    private Optional<String> name;
    @Pattern(regexp = RegexPatterns.IMAGE_URL)
    @Getter
    @Setter
    private Optional<String> backgroundUrl;
    @Getter
    @Setter
    private Optional<ObjectId> parentSkillId;
}
