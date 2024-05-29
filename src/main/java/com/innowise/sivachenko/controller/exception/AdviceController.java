package com.innowise.sivachenko.controller.exception;

import com.innowise.sivachenko.model.error.ExceptionErrorDto;
import com.innowise.sivachenko.model.error.ExceptionListDto;
import com.innowise.sivachenko.model.exception.CannotCreatePaymentException;
import com.innowise.sivachenko.model.exception.CannotDeletePaymentException;
import com.innowise.sivachenko.model.exception.RefundPaymentException;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class AdviceController {

    /**
     * 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionListDto methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        return new ExceptionListDto(e.getBindingResult().getFieldErrors().stream()
                .map(s -> new ExceptionErrorDto(s.getDefaultMessage()))
                .collect(Collectors.toList()));
    }

    /**
     * 400 (Bad Request)
     */
    @ExceptionHandler(CannotCreatePaymentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionErrorDto cannotCreatePaymentExceptionHandler(CannotCreatePaymentException e) {
        return new ExceptionErrorDto(e.getMessage());
    }

    /**
     * 400 (Bad Request)
     */
    @ExceptionHandler(CannotDeletePaymentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionErrorDto cannotDeletePaymentExceptionHandler(CannotDeletePaymentException e) {
        return new ExceptionErrorDto(e.getMessage());
    }

    /**
     * 400 (Bad Request)
     */
    @ExceptionHandler(RefundPaymentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionErrorDto refundPaymentExceptionHandler(RefundPaymentException e) {
        return new ExceptionErrorDto(e.getMessage());
    }

    /**
     * 403 (Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionErrorDto accessDeniedExceptionHandler(AccessDeniedException e) {
        return new ExceptionErrorDto("You haven't authorities for doing this");
    }

    /**
     * 404 (Not Found)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionErrorDto entityNotFoundExceptionHandler(EntityNotFoundException e) {
        return new ExceptionErrorDto(e.getMessage());
    }

    /**
     * 500 (Internal Error)
     */
    @ExceptionHandler()
    public ExceptionErrorDto internalErrorHandler(Throwable e) {
        return new ExceptionErrorDto(e.getMessage());
    }

    /**
     * 503 (Service Unavailable)
     */
    @ExceptionHandler(ServiceNotFoundException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionErrorDto serviceNotFoundExceptionHandler(ServiceNotFoundException e) {
        return new ExceptionErrorDto(e.getMessage());
    }
}
