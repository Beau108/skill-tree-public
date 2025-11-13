package com.bproj.skilltree.mapper;

import com.bproj.skilltree.dto.SkillRequest;
import com.bproj.skilltree.dto.SkillResponse;
import com.bproj.skilltree.dto.SkillSummary;
import com.bproj.skilltree.model.Skill;
import org.bson.types.ObjectId;

/**
 * Skill DTO conversions.
 */
public class SkillMapper {
  private SkillMapper() {}

  /**
   * Create a Skill from a SkillRequest DTO.
   *
   * @param skillRequest The DTO the Skill is made from
   * @return The created Skill
   */
  public static Skill toSkill(SkillRequest skillRequest) {
    if (skillRequest == null) {
      return null;
    }

    return new Skill(new ObjectId(skillRequest.getTreeId()), skillRequest.getName(),
        skillRequest.getBackgroundUrl(), skillRequest.getTimeSpentHours(),
        skillRequest.getParentSkillId() != null ? new ObjectId(skillRequest.getParentSkillId()) : null);
  }

  /**
   * Create a SkillResponse DTO from a Skill.
   *
   * @param skill The Skill the DTO is made from
   * @return The created SkillResponse DTO
   */
  public static SkillResponse fromSkill(Skill skill) {
    if (skill == null) {
      return null;
    }

    return new SkillResponse(skill.getId().toString(), skill.getTreeId().toString(), skill.getName(),
        skill.getBackgroundUrl(), skill.getTimeSpentHours(), skill.getParentSkillId() != null ? skill.getParentSkillId().toString() : null);
  }

  /**
   * Create a SkillSummary DTO from a Skill.
   *
   * @param skill The Skill the DTO is made from
   * @return The created SkillSummary DTO
   */
  public static SkillSummary getSkillSummary(Skill skill) {
    if (skill == null) {
      return null;
    }

    return new SkillSummary(skill.getName(), skill.getBackgroundUrl(), skill.getTimeSpentHours());
  }
}
