package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;

import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

import lombok.Getter;

/**
 * An exception class which can contain multiple errors.
 * Used for validation, in service classes.
 * 
 * @author Sanjay Patel
 */
@Getter
public class MultiErrorException extends RuntimeException {

	private static final long serialVersionUID = 6020532846519363456L;
	
	// list of errors
	private List<LemonFieldError> errors = new ArrayList<>(10);
	
	// HTTP Status code to be returned
	private HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
	
	// Set this if you need to customize exceptionId
	private String exceptionId = null;
	
	public MultiErrorException httpStatus(HttpStatus status) {
		this.status = status;
		return this;
	}

	public MultiErrorException exceptionId(String exceptionId) {
		this.exceptionId = exceptionId;
		return this;
	}

	/**
	 * Adds a field-error if the given condition isn't true
	 */
	public MultiErrorException validate(String fieldName, boolean valid,
			String messageKey, Object... args) {
		
		if (!valid)
			errors.add(new LemonFieldError(fieldName, messageKey,
				LexUtils.getMessage(messageKey, args)));
			
		return this;
	}

	/**
	 * Throws the exception, if there are accumulated errors
	 */
	public void go() {
		if (!errors.isEmpty())
			throw this;
	}
	
	/**
	 * Adds a global-error if the given condition isn't true
	 */
	public MultiErrorException validate(boolean valid,
			String messageKey, Object... args) {
		
		// delegate
		return validate(null, valid, messageKey, args);
	}

	/**
	 * Adds constraint violations
	 * 
	 * @param constraintViolations
	 * @param objectName
	 * @return
	 */
	public MultiErrorException addErrors(Set<? extends ConstraintViolation<?>> constraintViolations, String objectName) {
		
		errors.addAll(constraintViolations.stream()
				.map(constraintViolation ->
					new LemonFieldError(
							objectName + "." + constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessageTemplate(),
							constraintViolation.getMessage()))
			    .collect(Collectors.toList()));
		
		return this;
	}

	/**
	 * Overrides the standard getMessage
	 */
	@Override
	public String getMessage() {

		if (errors.isEmpty())
			return null;
		
		// return the first message
		return errors.get(0).getMessage();
	}
}
