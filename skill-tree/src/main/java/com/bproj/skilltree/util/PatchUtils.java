package com.bproj.skilltree.util;

import com.bproj.skilltree.dto.OrientationMovePatch;
import com.bproj.skilltree.dto.OrientationMoveType;
import com.bproj.skilltree.exception.BadRequestException;
import com.bproj.skilltree.exception.PatchValidationException;
import com.bproj.skilltree.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PatchUtils {

    // Mutable fields for each entity type
    public static final Set<String> USER_MUTABLE_FIELDS = Set.of("displayName", "profilePictureUrl");
    public static final Set<String> TREE_MUTABLE_FIELDS = Set.of("name", "backgroundUrl", "description", "visibility");
    public static final Set<String> SKILL_MUTABLE_FIELDS = Set.of("name", "backgroundUrl", "parentSkillId");
    public static final Set<String> ACHIEVEMENT_MUTABLE_FIELDS = Set.of("title", "backgroundUrl", "description", "prerequisites", "complete");
    public static final Set<String> ACTIVITY_MUTABLE_FIELDS = Set.of("name", "duration", "skillWeights");
    public static final Set<String> ORIENTATION_MUTABLE_FIELDS = Set.of("skillLocations", "achievementLocations");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PatchUtils() {}

    private static void validatePatchKeys(Map<String, Object> patch, Set<String> allowedFields) {
        for (String key : patch.keySet()) {
            if (!allowedFields.contains(key)) {
                throw new PatchValidationException("'" + key + "' is not a modifiable field.");
            }
        }
    }

    private static String safeCastToString(String key, Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof String str) {
            return str;
        }
        throw new PatchValidationException("'" + key + "' contains a non-String value.");
    }

    private static ObjectId safeCastToObjectId(String key, Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof String str) {
            return new ObjectId(str);
        }
        if (raw instanceof ObjectId id) {
            return id;
        }
        throw new PatchValidationException("'" + key + "' contains a non-ObjectId value.");
    }

    private static boolean safeCastToBoolean(String key, Object raw) {
        if (raw == null) {
            throw new PatchValidationException("'" + key + "' cannot be null.");
        }
        if (raw instanceof Boolean bool) {
            return bool;
        }
        throw new PatchValidationException("'" + key + "' contains a non-boolean value.");
    }

    private static double safeCastToDouble(String key, Object raw) {
        if (raw == null) {
            throw new PatchValidationException("'" + key + "' cannot be null.");
        }
        if (raw instanceof Number num) {
            return num.doubleValue();
        }
        throw new PatchValidationException("'" + key + "' contains a non-numeric value.");
    }

    public static User applyUserPatch(User user, Map<String, Object> patch) {
        validatePatchKeys(patch, USER_MUTABLE_FIELDS);
        User patched = new User(user);
        for (String key : patch.keySet()) {
            String value = safeCastToString(key, patch.get(key));
            switch(key) {
                case "displayName":
                    patched.setDisplayName(value);
                    break;
                case "profilePictureUrl":
                    patched.setProfilePictureUrl(value);
                    break;
                default:
                    throw new IllegalArgumentException("Field '" + key + "' is not handled when User patch is applied.");
            }
        }
        return patched;
    }

    public static Tree applyTreePatch(Tree tree, Map<String, Object> patch) {
        validatePatchKeys(patch, TREE_MUTABLE_FIELDS);
        Tree patched = new Tree(tree);
        for (String key : patch.keySet()) {
            String value = safeCastToString(key, patch.get(key));
            switch (key) {
                case "name":
                    patched.setName(value);
                    break;
                case "backgroundUrl":
                    patched.setBackgroundUrl(value);
                    break;
                case "description":
                    patched.setDescription(value);
                    break;
                case "visibility":
                    Visibility visibility = Visibility.fromString(value);
                    patched.setVisibility(visibility);
                    break;
                default:
                    throw new IllegalArgumentException("Field '" + key + "' is not handled when Tree patch is applied.");
            }
        }
        return patched;
    }

    public static Skill applySkillPatch(Skill skill, Map<String, Object> patch) {
        validatePatchKeys(patch, SKILL_MUTABLE_FIELDS);
        Skill patched = new Skill(skill);
        for (String key : patch.keySet()) {
            switch (key) {
                case "name":
                    String name = safeCastToString(key, patch.get(key));
                    patched.setName(name);
                    break;
                case "backgroundUrl":
                    String background = safeCastToString(key, patch.get(key));
                    patched.setBackgroundUrl(background);
                    break;
                case "parentSkillId":
                    ObjectId id = safeCastToObjectId(key, patch.get(key));
                    patched.setParentSkillId(id);
                    break;
                default:
                    throw new IllegalArgumentException("Field '" + key + "' is not handled when Skill patch is applied.");
            }
        }
        return patched;
    }

    public static Achievement applyAchievementPatch(Achievement achievement, Map<String, Object> patch) {
        validatePatchKeys(patch, ACHIEVEMENT_MUTABLE_FIELDS);
        Achievement patched = new Achievement(achievement);
        for (String key : patch.keySet()) {
            Object raw = patch.get(key);
            switch (key) {
                case "title":
                    String title = safeCastToString(key, raw);
                    patched.setTitle(title);
                    break;
                case "backgroundUrl":
                    String background = safeCastToString(key, raw);
                    patched.setBackgroundUrl(background);
                    break;
                case "description":
                    String description = safeCastToString(key, raw);
                    patched.setDescription(description);
                    break;
                case "prerequisites":
                    if (!(raw instanceof List<?> list)) {
                        throw new PatchValidationException("Invalid prerequisites. Must be a List<ObjectId>");
                    }
                    List<ObjectId> prerequisites = list.stream()
                            .map(o -> safeCastToObjectId(key, o))
                            .toList();
                    patched.setPrerequisites(prerequisites);
                    break;
                case "complete":
                    boolean complete = safeCastToBoolean(key, raw);
                    patched.setComplete(complete);

                    if (complete) {
                        patched.setCompletedAt(Instant.now());
                    } else {
                        patched.setCompletedAt(null);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Field '" + key + "' is not handled when Achievement patch is applied.");
            }
        }
        return patched;
    }

    public static Activity applyActivityPatch(Activity activity, Map<String, Object> patch) {
        validatePatchKeys(patch, ACTIVITY_MUTABLE_FIELDS);
        Activity patched = new Activity(activity);
        for (String key : patch.keySet()) {
            Object raw = patch.get(key);
            switch (key) {
                case "name":
                    String name = safeCastToString(key, raw);
                    patched.setName(name);
                    break;
                case "duration":
                    double duration = safeCastToDouble(key, raw);
                    patched.setDuration(duration);
                    break;
                case "skillWeights":
                    if (!(raw instanceof List<?> list)) {
                        throw new PatchValidationException("'skillWeights' must be a list.");
                    }
                    List<SkillWeight> skillWeights = list.stream()
                            .map(o -> MAPPER.convertValue(o, SkillWeight.class))
                            .toList();
                    patched.setSkillWeights(skillWeights);
                    break;
                default:
                    throw new IllegalArgumentException("Field '" + key + "' is not handled when Activity patch is applied.");
            }
        }
        return patched;
    }

    /**
     * Orientation patches differ from other PATCH endpoints because clients cannot add or remove
     * skills or achievements. The orientation structure is fixed once created.
     *
     * Instead of sending a partial object to merge, the client sends a list of position updates
     * (e.g., x/y coordinate changes) for existing items only. Each update explicitly specifies:
     *
     *   - the item type ("SKILL" or "ACHIEVEMENT")
     *   - the item's ObjectId
     *   - the new normalized x/y coordinates
     *
     * This prevents unauthorized structural changes while still allowing interactive repositioning.
     */
    public static Orientation applyOrientationPatch(Orientation orientation, List<OrientationMovePatch> patch) {
        Orientation patched = new Orientation(orientation);
        Map<String, SkillLocation> skillLocationMap = patched.getSkillLocations().stream().collect(Collectors.toMap(sl -> sl.getSkillId().toString(), sl -> sl));
        Map<String, AchievementLocation> achievementLocationMap = patched.getAchievementLocations().stream().collect(Collectors.toMap(al -> al.getAchievementId().toString(), al -> al));
        for (OrientationMovePatch omp : patch) {
            if (omp.getType() == OrientationMoveType.SKILL) {
                SkillLocation target = skillLocationMap.get(omp.getId());
                if (target == null) {
                    throw new PatchValidationException("OrientationMovePatch skill not contained in existing orientation.");
                }
                target.setX(omp.getX());
                target.setY(omp.getY());
            } else if (omp.getType() == OrientationMoveType.ACHIEVEMENT){
                AchievementLocation target = achievementLocationMap.get(omp.getId());
                if (target == null) {
                   throw new PatchValidationException("OrientationMovePatch achievement not contained in existing orientation.");
                }
                target.setX(omp.getX());
                target.setY(omp.getY());
            } else {
                throw new PatchValidationException("Type of OrientationMovePatch not recognized (must be SKILL | ACHIEVEMENT)");
            }
        }
        return patched;
    }
}
