/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.errors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

   @Override
   protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
       String error = "Malformed JSON request";
       return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
   }

   private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
       return new ResponseEntity<>(apiError, apiError.getStatus());
   }

   /**
    * Handle DataIntegrityViolationException, inspects the cause for different data related causes.
    *
    * @param ex the DataIntegrityViolationException
    * @return the ApiError object
    */
   @ExceptionHandler(ApiException.class)
   protected ResponseEntity<Object> handleDataIntegrityViolation(ApiException ex, WebRequest request) {
       return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Server Configuration Error", ex));
   }
   
   @Override
   protected ResponseEntity<Object> handleMethodArgumentNotValid(
           MethodArgumentNotValidException ex,
           HttpHeaders headers,
           HttpStatus status,
           WebRequest request) {
       ApiError apiError = new ApiError(BAD_REQUEST,"Validation Error",ex);
       apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
       apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
       return buildResponseEntity(apiError);
   }
   
   @ExceptionHandler(EntityNotFoundException.class)
   protected ResponseEntity<Object> handleEntityNotFound(
           EntityNotFoundException ex) {
       ApiError apiError = new ApiError(HttpStatus.NOT_FOUND,ex.getMessage(),ex);
       return buildResponseEntity(apiError);
   }

}