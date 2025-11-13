package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.Visibility;
import com.bproj.skilltree.util.RegexPatterns;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

public class TreePatch {
    @Pattern(regexp = RegexPatterns.TREE_NAME)
    @Getter
    @Setter
    private Optional<String> name;
    @Pattern(regexp = RegexPatterns.IMAGE_URL)
    @Getter
    @Setter
    private Optional<String> backgroundUrl;
    @Pattern(regexp = RegexPatterns.TREE_DESCRIPTION)
    @Getter
    @Setter
    private Optional<String> description;
    @Getter
    @Setter
    private Optional<Visibility> visibility;
}
