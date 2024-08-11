package com.ead.payments.errors;

import static java.util.stream.Collectors.toList;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Log4j2
@ControllerAdvice
@AllArgsConstructor
class GeneralExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException exception) {
		var invalidParams = exception.getConstraintViolations()
				.stream()
				.map(violation -> {
					var fields = StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
													  .reduce((first, second) -> second).orElse(null)
													  .toString();
					return Map.of("name", fields,
							      "reason", violation.getMessage());

				})
				.collect(toList());

		return ResponseEntity.unprocessableEntity()
		 					.body(Map.of(
							"title", "Constraint violated exception",
							"details", exception.getMessage(),
							"invalid_params", invalidParams));
	}

	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

		var invalidParams = exception.getBindingResult()
													   .getFieldErrors()
													   .stream()
													   .collect(Collectors.toMap(FieldError::getField,
															   					 FieldError::getDefaultMessage));

		var invalidClassName = Optional.ofNullable(exception.getBindingResult())
				.map(BindingResult::getTarget)
				.map(Object::getClass)
				.map(Class::getSimpleName)
				.orElse("request");

		var details = "The '" + invalidClassName + "' is invalid";;
		return ResponseEntity.unprocessableEntity()
							 .body(Map.of(
									"title", "Constraint Violation Exception",
									"details", details,
									"invalid_params", invalidParams));
	}
}
