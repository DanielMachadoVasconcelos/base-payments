package com.ead.payments.orders.place;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class PlaceOrderAdvice {

    // capture the UnknownAuthorizationStatus and return a 500 status code
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleUnknownAuthorizationStatus(UnknownAuthorizationStatus e,
                                                                          HttpServletRequest request) {
        log.error("Unknown authorization status", e);
        ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetails.setTitle("Unknown authorization status");
        problemDetails.setDetail(e.getMessage());
        problemDetails.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity
                .status(500)
                .body(problemDetails);

    }

    // capture the IssuerDeclinedException and return a 400 status code
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleIssuerDeclinedException(IssuerDeclinedException e,
                                                                       HttpServletRequest request) {
        log.error("Issuer declined the authorization", e);
        ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetails.setTitle("Issuer declined the authorization");
        problemDetails.setDetail(e.getMessage());
        problemDetails.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity
                .status(401)
                .body(problemDetails);
    }
}
