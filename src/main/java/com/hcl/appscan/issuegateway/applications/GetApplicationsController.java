/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.applications;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags="applications")
@RequestMapping("/applications")
public class GetApplicationsController {
	private final Logger logger =LoggerFactory.getLogger(this.getClass());
	List<String> errors = new ArrayList<String>();
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	@ApiOperation(value="Get the list of Applications",notes="This API gives the list of applications associated with the account.The user must provide the Account details in the form of id and secret,"
    		+ "which are generated using the ASE account - API POST /keylogin/apikeylogin or from ASoC cloud account. "
    		+ "For simplicity the response contains only name and id of the application.")
	ResponseEntity<String> getResult(GetApplicationData applicationData) throws Exception {
		try {
    		logger.debug("New Request.  Payload:\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(applicationData));
    	} 
		catch (Exception e) 
		{
			logger.error("Error parsing JSON", e);
		}
		ApplicationsRetrievalHandler obj=new ApplicationsRetrievalHandler();
		return obj.retrieveApplicationList(applicationData, errors);
	}
}

