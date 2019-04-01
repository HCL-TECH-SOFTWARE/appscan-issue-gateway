/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.appscan.issuegateway.errors.EntityNotFoundException;
import com.hcl.appscan.issuegateway.jobs.JobManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "issues")

public class PushJobController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping(value="/issues/pushjobs",method = RequestMethod.POST, produces = "application/json")
    @ApiOperation(notes="This API creates a Job that will process AppScan issues and push them into other issue management systems. "
    		+ "The job is completely controlled by the JSON that is passed in. The details of the JSON will vary depending on your target issue management system."
    		+ "To view configuration details, invoke the GET /providers API below and the details will be in the response",value="Create a job to push AppScan issues to an issue management system")
	PushJobResult issuePush(@Valid @RequestBody @ApiParam(name="body", required=true) ASOCPushJobData submitJobData) {
    	try {
    		logger.debug("New Request.  Payload:\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(submitJobData));
    	} catch (Exception e) {logger.error("Error parsing JSON", e);}
    	
    	PushJobData jobData= new PushJobData();
    	PushJobData.AppScanData appscanData= new PushJobData.AppScanData();
    	PushJobData.IMData imData=new PushJobData.IMData();
    	jobData.setAppscanData(appscanData);
    	jobData.setImData(imData);
    	jobData.getAppscanData().setAppscanProvider("ASOC");
    	jobData.getAppscanData().setUrl(submitJobData.appscanData.url);
    	jobData.getAppscanData().setApikeyid(submitJobData.appscanData.apikeyid);
    	jobData.getAppscanData().setApikeysecret(submitJobData.appscanData.apikeysecret);
    	jobData.getAppscanData().setAppid(submitJobData.appscanData.appid);
    	jobData.getAppscanData().setMaxissues(submitJobData.appscanData.maxissues);
    	jobData.getAppscanData().setIssuestates(submitJobData.appscanData.issuestates);
    	jobData.getAppscanData().setPolicyids(submitJobData.appscanData.policyids);
    	jobData.getAppscanData().setExcludeIssuefilters(submitJobData.appscanData.issuefilters);
    	jobData.getImData().setProvider(submitJobData.imData.provider);
    	jobData.getImData().setConfig(submitJobData.imData.config);
    	
    	PushJob submitJob = new PushJob(jobData);
    	PushJobResult jobResult = JobManager.getInstance().submitJob(submitJob);
   		return jobResult;
    }
    
    @RequestMapping(value="/issues/pushjobs",method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value="Get an issue push job",notes="A PushJob will have a Status of \"Running - Current Operation\" until the job has either finished successfully or failed. "
    		+ "The errors field holds problems that were encountred during the operation. "
    		+ "The results field will (if successful) hold a Map of AppScan Issue Ids and their associated Issues in the other issue management system.")
	PushJobResult getStatus(String id) throws EntityNotFoundException{
		return JobManager.getInstance().getJobResult(id);
	}
	
    @RequestMapping(value="/v2/issues/pushjobs",method = RequestMethod.POST, produces = "application/json")
    @ApiOperation(notes="This API creates a Job that will process AppScan issues and push them into other issue management systems. "
    		+ "The job is completely controlled by the JSON that is passed in. The details of the JSON will vary depending on your target issue management system."
    		+ "To view configuration details, invoke the GET /providers API below and the details will be in the response",value="Create a job to push AppScan issues to an issue management system")
	PushJobResult issuePushV2(@Valid @RequestBody @ApiParam(name="body", required=true) PushJobData submitJobData) {
    	try {
    		logger.debug("New Request.  Payload:\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(submitJobData));
    	} catch (Exception e) {logger.error("Error parsing JSON", e);}
    	PushJob submitJob = new PushJob(submitJobData);
    	PushJobResult jobResult = JobManager.getInstance().submitJob(submitJob);
   		return jobResult;
    }
    
    @RequestMapping(value="/v2/issues/pushjobs",method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value="Get an issue push job",notes="A PushJob will have a Status of \"Running - Current Operation\" until the job has either finished successfully or failed. "
    		+ "The errors field holds problems that were encountred during the operation. "
    		+ "The results field will (if successful) hold a Map of AppScan Issue Ids and their associated Issues in the other issue management system.")
	PushJobResult getStatusV2(String id) throws EntityNotFoundException{
		return JobManager.getInstance().getJobResult(id);
	}
     
}