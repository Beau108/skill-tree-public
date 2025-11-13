package com.bproj.skilltree.dto;

import com.bproj.skilltree.util.RegexPatterns;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class UserPatch {
    @Pattern(regexp = RegexPatterns.DISPLAY_NAME)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Optional<String> displayName = null;
    @Pattern(regexp = RegexPatterns.IMAGE_URL)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Optional<String> profilePictureUrl = null;
}
