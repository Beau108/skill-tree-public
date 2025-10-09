package com.bproj.skilltree.web;

import java.net.URI;
import java.time.Instant;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bproj.skilltree.exception.ForbiddenException;
import com.bproj.skilltree.exception.PatchValidationException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	
	private ProblemDetail base(HttpStatus status, String title, String detail, HttpServletRequest req) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setTitle(title);
		pd.setInstance(URI.create(req.getRequestURI()));
		pd.setProperty("timestamp", Instant.now());
		return pd;
	}
	
	@ExceptionHandler(PatchValidationException.class)
	public ResponseEntity<ProblemDetail> handlePatchValidation(
			PatchValidationException e, HttpServletRequest req) {
		ProblemDetail pd = base(HttpStatus.UNPROCESSABLE_ENTITY,
				"Validation failed", "One or more fields are invalid.", req);
		pd.setProperty("errors", e.getMessage());
		return ResponseEntity.unprocessableEntity().body(pd);
	}
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ProblemDetail> handleNotFound(
			NotFoundException e, HttpServletRequest req) {
		ProblemDetail pd = base(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), req);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
	}
	@ExceptionHandler(ForbiddenException.class)
	  public ResponseEntity<ProblemDetail> handleForbidden(
			  ForbiddenException e, HttpServletRequest req) {
	    ProblemDetail pd = base(HttpStatus.FORBIDDEN, "Forbidden", e.getMessage(), req);
	    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
	  }

	  @ExceptionHandler(HttpMessageNotReadableException.class) // malformed JSON / type coercion
	  public ResponseEntity<ProblemDetail> handleNotReadable(
	      HttpMessageNotReadableException e, HttpServletRequest req) {
	    ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Invalid request body",
	        "Malformed JSON or type mismatch.", req);
	    return ResponseEntity.badRequest().body(pd);
	  }

	  @ExceptionHandler(IllegalArgumentException.class)
	  public ResponseEntity<ProblemDetail> handleIllegalArg(
			  IllegalArgumentException e, HttpServletRequest req) {
	    ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Invalid input", e.getMessage(), req);
	    return ResponseEntity.badRequest().body(pd);
	  }

	  @ExceptionHandler(Exception.class) // fallback
	  public ResponseEntity<ProblemDetail> handleUnknown(
			  Exception ex, HttpServletRequest req) {
	    ProblemDetail pd = base(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "Unexpected error.", req);
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
	  }		
}
