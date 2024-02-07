/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018,2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import com.hcl.appscan.issuegateway.issues.PushJobResult;
import com.hcl.appscan.issuegateway.issues.PushJobService;
import com.hcl.appscan.issuegateway.issues.V1PushJobData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Tag(name = "issues")
public class PushJobController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final PushJobService pushJobService;

	public PushJobController(PushJobService pushJobService) {
		this.pushJobService = pushJobService;
	}

	@Deprecated
	@PostMapping(value = "/issues/pushjobs", produces = "application/json")
	@Operation(summary = "Create a job to push AppScan issues to an issue management system",
			description = "This API creates a Job that will process AppScan issues and push them into other issue management systems. "
			+ "The job is completely controlled by the JSON that is passed in. The details of the JSON will vary depending on your target issue management system."
			+ "To view configuration details, invoke the GET /providers API below and the details will be in the response")
	public PushJobResult postIssuesPushJobs(
			@Valid @RequestBody @Parameter(name = "body", required = true) V1PushJobData submitJobData) {
		try {
			new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(submitJobData);
		} catch (Exception e) {
			logger.error("Error parsing JSON", e);
		}
		return pushJobService.createPushJob(submitJobData);
	}

	@PostMapping(value = "/v4/issues/pushjobs", produces = "application/json")
	@Operation(summary = "Create a job to push AppScan issues to an issue management system",
			description = "This API creates a Job that will process AppScan issues and push them into other issue management systems. "
			+ "The job is completely controlled by the JSON that is passed in. The details of the JSON will vary depending on your target issue management system."
			+ "To view configuration details, invoke the GET /providers API below and the details will be in the response")
	public PushJobResult postIssuesPushJobsV4(
			@Valid @RequestBody @Parameter(name = "body", required = true) PushJobData submitJobData) {
		try {
			new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(submitJobData);
		} catch (Exception e) {
			logger.error("Error parsing JSON", e);
		}
		return pushJobService.createPushJob(submitJobData);
	}

	@Deprecated
	@GetMapping(value = "/issues/pushjobs", produces = "application/json")
	@Operation(summary = "Get an issue push job", description = "A PushJob will have a Status of \"Running - Current Operation\" until the job has either finished successfully or failed. "
			+ "The errors field holds problems that were encountered during the operation. "
			+ "The results field will (if successful) hold a Map of AppScan Issue Ids and their associated Issues in the other issue management system.")
	public PushJobResult getIssuesPushJobs(String id) {
		return pushJobService.getStatus(id);
	}

	@GetMapping(value = "/v4/issues/pushjobs", produces = "application/json")
	@Operation(summary = "Get an issue push job", description = "A PushJob will have a Status of \"Running - Current Operation\" until the job has either finished successfully or failed. "
			+ "The errors field holds problems that were encountered during the operation. "
			+ "The results field will (if successful) hold a Map of AppScan Issue Ids and their associated Issues in the other issue management system.")
	public PushJobResult getIssuesPushJobsV4(String id) {
		return pushJobService.getStatus(id);
	}

}