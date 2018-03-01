/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.errors;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

class ApiError {

   private HttpStatus status;
   private Integer statusCode;
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
   private LocalDateTime timestamp;
   private String message;
   private String debugMessage;

   ApiError(HttpStatus status, String message, Throwable ex) {
	   timestamp = LocalDateTime.now();
       this.status=status;
       this.statusCode=status.value();
       this.message=message;
       this.debugMessage=ex.getLocalizedMessage();
   }

   public HttpStatus getStatus() {
	   return status;
   }

   public Integer getStatusCode() {
	   return statusCode;
   }

   public String getMessage() {
	   return message;
   }

   public String getDebugMessage() {
	   return debugMessage;
   }
}