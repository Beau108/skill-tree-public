package com.bproj.skilltree.util;

import com.bproj.skilltree.exception.PatchValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;

/**
 * Utility methods for applying JSON Merge Patch operations (RFC 7396) to existing domain objects.
 *
 * <p>
 * JSON Merge Patch allows clients to send partial JSON documents with only the fields they want to
 * change. Any field not included in the patch remains unchanged.
 * </p>
 *
 * <p>
 * Example patch request (application/merge-patch+json): { "displayName": "New Name" }
 * </p>
 *
 */
public class JsonMergePatchUtils {

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Applies a JSON Merge Patch to a target object.
   *
   * @param patch the JsonMergePatch object representing changes
   * @param target the existing domain object to update
   * @param type the class type of the domain object
   * @param <T> generic type parameter
   * @return a new instance of T with the patch applied
   * @throws Exception if patch application or mapping fails
   */
  public static <T> T applyMergePatch(JsonMergePatch patch, T target, Class<T> type) {
    try {
      JsonNode targetNode = mapper.convertValue(target, JsonNode.class);
      JsonNode patchedNode = patch.apply(targetNode);
      return mapper.treeToValue(patchedNode, type);
    } catch (Exception e) {
      throw new PatchValidationException("Failed to apply JSON merge patch");
    }
  }
}
