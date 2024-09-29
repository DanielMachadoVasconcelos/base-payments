package com.ead.payments.orders;

import com.ead.payments.ConcurrencyException;
import java.net.URI;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OrderControlAdvice {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Invalid Resource Version");
        problemDetail.setDetail(e.getMessage());
        problemDetail.setType(URI.create("v1/orders"));
        return new ResponseEntity<>(problemDetail, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Invalid Resource Version");
        problemDetail.setDetail("The resource was updated by another transaction, please reload and try again.");
        problemDetail.setType(URI.create("v1/orders"));
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problemDetail);
    }

    @ExceptionHandler(ConcurrencyException.class)
    public ResponseEntity<ProblemDetail> handleConcurrencyException(ConcurrencyException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Invalid Resource Version");
        problemDetail.setDetail("The resource was updated by another transaction, please reload and try again.");
        problemDetail.setType(URI.create("v1/orders"));
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problemDetail);
    }
}
