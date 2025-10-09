package com.bproj.skilltree.util;

import com.bproj.skilltree.exception.BadRequestException;
import org.bson.types.ObjectId;

/**
 * Util class for ObjectIds.
 */
public class ObjectIdUtils {
  private ObjectIdUtils() {}
  
  /**
   * Create a new ObjectId from 'id'. Throw BRE if invalid.
   *
   * @param id  The id 
   * @param field   The field name of the id (logging)
   * @return    The new ObjectId
   */
  public static ObjectId validateObjectId(String id, String field) {
    if (!ObjectId.isValid(id)) {
      throw new BadRequestException("Invalid " + field + ": " + id);
    }
    return new ObjectId(id);
  }
}
