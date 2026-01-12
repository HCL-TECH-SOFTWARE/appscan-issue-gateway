/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import common.IAppScanIssue;

public class AppScanIssue implements IAppScanIssue {

	private File issueDetails;
	private Map<String, Object> issueFields = new HashMap<>();

	@Override
	@JsonAnyGetter
	public Object get(String name) {
		return issueFields.get(name);
	}

	@JsonAnySetter
	public void set(String name, Object value) {
		issueFields.put(name, value);
	}

	@Override
	public File getIssueDetails() {
		return issueDetails;
	}

	public void setIssueDetails(File issueDetails) {
		this.issueDetails = issueDetails;
	}

	@Override
	public String toString() {
		return "AppScanIssue{" +
				"issueDetails=" + issueDetails +
				", issueFields=" + issueFields +
				'}';
	}
}
