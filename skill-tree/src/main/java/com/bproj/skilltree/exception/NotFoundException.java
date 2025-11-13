package com.bproj.skilltree.exception;

import java.util.Map;

/**
 * Thrown when a repository call expects a value and does not find one with the given query.
 */
public class NotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final Map<String, String> query;
  private final String collectionName;

  /**
   * Create a new NotFoundException.
   *
   * @param collectionName The name of the collection that was queried
   * @param query The query parameters
   */
  public NotFoundException(String collectionName, Map<String, String> query) {
    super("Could not find element");
    this.query = query;
    this.collectionName = collectionName;
  }

  public Map<String, String> getQuery() {
    return query;
  }

  public String getCollectionName() {
    return collectionName;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append("Could not find element in collection '").append(collectionName)
        .append("' with query: ").append(query);
    return sb.toString();
  }

  @Override
  public String toString() {
    return "NotFoundException{" + "collectionName='" + collectionName + '\'' + ", query=" + query
        + '}';
  }

}
