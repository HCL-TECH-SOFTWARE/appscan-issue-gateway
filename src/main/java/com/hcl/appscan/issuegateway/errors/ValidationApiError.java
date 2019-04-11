/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018,2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.errors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, property = "error", visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
class ValidationApiError {

	private HttpStatus status;
	private Integer statusCode;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;
	private String message;
	private List<ValidationError> errorList;

	private ValidationApiError() {
		timestamp = LocalDateTime.now();
	}

	ValidationApiError(HttpStatus status) {
		this();
		this.status = status;
	}

	ValidationApiError(HttpStatus status, Throwable ex) {
		this();
		this.status = status;
		this.message = "Unexpected error";
	}

	ValidationApiError(HttpStatus status, String message, Throwable ex) {
		this();
		this.status = status;
		this.statusCode = status.value();
		this.message = message;
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

	public List<ValidationError> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<ValidationError> errorList) {
		this.errorList = errorList;
	}

	private void addToErrorList(ValidationError error) {
		if (errorList == null) {
			errorList = new ArrayList<>();
		}
		errorList.add(error);
	}

	private void addValidationError(String object, String field, Object rejectedValue, String message) {
		addToErrorList(new ValidationError(object, field, rejectedValue, message));
	}

	private void addValidationError(String object, String message) {
		addToErrorList(new ValidationError(object, message));
	}

	private void addValidationError(FieldError fieldError) {
		this.addValidationError(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(),
				fieldError.getDefaultMessage());
	}

	void addValidationErrors(List<FieldError> fieldErrors) {
		fieldErrors.forEach(this::addValidationError);
	}

	private void addValidationError(ObjectError objectError) {
		this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
	}

	void addValidationError(List<ObjectError> globalErrors) {
		globalErrors.forEach(this::addValidationError);
	}

	class ValidationError {
		private String object;
		private String field;
		private Object rejectedValue;
		private String message;

		public ValidationError(String object, String field, Object rejectedValue, String message) {
			super();
			this.object = object;
			this.field = field;
			this.rejectedValue = rejectedValue;
			this.message = message;
		}

		ValidationError(String object, String message) {
			this.object = object;
			this.message = message;
		}

		public String getObject() {
			return object;
		}

		public void setObject(String object) {
			this.object = object;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public Object getRejectedValue() {
			return rejectedValue;
		}

		public void setRejectedValue(Object rejectedValue) {
			this.rejectedValue = rejectedValue;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}
}

class LowerCaseClassNameResolver extends TypeIdResolverBase {

	@Override
	public String idFromValue(Object value) {
		return value.getClass().getSimpleName().toLowerCase();
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		return idFromValue(value);
	}

	@Override
	public JsonTypeInfo.Id getMechanism() {
		return JsonTypeInfo.Id.CUSTOM;
	}
}