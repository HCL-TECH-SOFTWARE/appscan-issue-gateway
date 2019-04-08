/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018,2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.appscan.issuegateway.errors.EntityNotFoundException;
import com.hcl.appscan.issuegateway.issues.ASOCPushJobData;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import com.hcl.appscan.issuegateway.issues.PushJobResult;
import com.hcl.appscan.issuegateway.issues.PushJobService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "issues")
public class PushJobController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PushJobService pushJobService;

	@RequestMapping(value = "/issues/pushjobs", method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(notes = "This API creates a Job that will process AppScan issues and push them into other issue management systems. "
			+ "The job is completely controlled by the JSON that is passed in. The details of the JSON will vary depending on your target issue management system."
			+ "To view configuration details, invoke the GET /providers API below and the details will be in the response", value = "Create a job to push AppScan issues to an issue management system")
	PushJobResult postIssuesPushJobs(
			@Valid @RequestBody @ApiParam(name = "body", required = true) ASOCPushJobData submitJobData) {
		try {
			logger.debug("New Request.  Payload:\n"
					+ new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(submitJobData));
		} catch (Exception e) {
			logger.error("Error parsing JSON", e);
		}
		return pushJobService.createPushJob(submitJobData);
	}

	@RequestMapping(value = "/v2/issues/pushjobs", method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(notes = "This API creates a Job that will process AppScan issues and push them into other issue management systems. "
			+ "The job is completely controlled by the JSON that is passed in. The details of the JSON will vary depending on your target issue management system."
			+ "To view configuration details, invoke the GET /providers API below and the details will be in the response", value = "Create a job to push AppScan issues to an issue management system")
	PushJobResult postIssuesPushJobsV2(
			@Valid @RequestBody @ApiParam(name = "body", required = true) PushJobData submitJobData) {
		try {
			logger.debug("New Request. Payload:\n"
					+ new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(submitJobData));
		} catch (Exception e) {
			logger.error("Error parsing JSON", e);
		}
		return pushJobService.createPushJob(submitJobData);
	}

	@RequestMapping(value = "/issues/pushjobs", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get an issue push job", notes = "A PushJob will have a Status of \"Running - Current Operation\" until the job has either finished successfully or failed. "
			+ "The errors field holds problems that were encountered during the operation. "
			+ "The results field will (if successful) hold a Map of AppScan Issue Ids and their associated Issues in the other issue management system.")
	PushJobResult getIssuesPushJobs(String id) throws EntityNotFoundException {
		return pushJobService.getStatus(id);
	}

	@RequestMapping(value = "/v2/issues/pushjobs", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get an issue push job", notes = "A PushJob will have a Status of \"Running - Current Operation\" until the job has either finished successfully or failed. "
			+ "The errors field holds problems that were encountered during the operation. "
			+ "The results field will (if successful) hold a Map of AppScan Issue Ids and their associated Issues in the other issue management system.")
	PushJobResult getIssuesPushJobsV2(String id) throws EntityNotFoundException {
		return pushJobService.getStatus(id);
	}

}