/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class PushJobResult {
	private String id;
	private String status;
	private List<String> errors;
	private Map<String, String> results;
	public PushJobResult(String id, String status, List<String> errors, Map<String, String> results) {
		this.id=id;
		this.status = status;
		this.errors = errors;
		this.results = results;
	}
	public String getId() {
		return id;
	}
	public String getStatus() {
		return status;
	}
	public List<String> getErrors() {
		return errors;
	}
	public Map<String, String> getResults() {
		return results;
	}
}
