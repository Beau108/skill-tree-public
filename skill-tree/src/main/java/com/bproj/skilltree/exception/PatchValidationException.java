package com.bproj.skilltree.exception;

/**
 * Thrown when a patch request contains invalid parameters.
 */
public class PatchValidationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public PatchValidationException(String message) {
    super(message);
  }
}
