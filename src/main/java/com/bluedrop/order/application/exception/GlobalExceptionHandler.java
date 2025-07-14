package com.bluedrop.order.application.exception;

import com.bluedrop.order.application.response.ApiErrorResponse;
import com.bluedrop.order.domain.exceptions.InvalidOrderStateTransitionException;
import com.bluedrop.order.domain.exceptions.OrderBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderBusinessException(OrderBusinessException e) {
        ApiErrorResponse error = new ApiErrorResponse("ORDER_BUSINESS_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidOrderStateTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStateTransition(InvalidOrderStateTransitionException e) {
        ApiErrorResponse error = new ApiErrorResponse("INVALID_STATE_TRANSITION", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ApiErrorResponse error = new ApiErrorResponse("VALIDATION_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadableException(IllegalArgumentException e) {
        ApiErrorResponse error = new ApiErrorResponse("BODY_VALIDATION_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentConversionNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleArgumentConversionNotSupportedException(IllegalArgumentException e) {
        ApiErrorResponse error = new ApiErrorResponse("ARGUMENT_CONVERSION_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleArgumentTypeMismatchException(IllegalArgumentException e) {
        ApiErrorResponse error = new ApiErrorResponse("ARGUMENT_TYPE_MISMATCH_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiErrorResponse error = new ApiErrorResponse("VALIDATION_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception e) {
        ApiErrorResponse error = new ApiErrorResponse("INTERNAL_ERROR", "An unexpected error occurred: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}