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
import org.springframework.http.ProblemDetail;
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
	public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
		var invalidParams = exception.getConstraintViolations()
				.stream()
				.map(violation -> {
					var fields = StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
													  .reduce((first, second) -> second).orElse(null)
													  .toString();
					return Map.of("name", fields,
							      "reason", violation.getMessage());

				})
				.toList();

		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
		problemDetail.setTitle("Constraint violated exception");
		problemDetail.setDetail(exception.getMessage());
		problemDetail.setProperty("invalid_params", invalidParams);

		return ResponseEntity.unprocessableEntity().body(problemDetail);
	}

	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail>  handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

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

		var details = STR."The '\{invalidClassName}' is invalid";

		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
		problemDetail.setTitle("Constraint Violation Exception");
		problemDetail.setDetail(details);
		problemDetail.setProperty("invalid_params", invalidParams);

		return ResponseEntity.unprocessableEntity().body(problemDetail);
	}
}
