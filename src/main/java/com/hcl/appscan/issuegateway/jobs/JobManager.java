/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.jobs;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.appscan.issuegateway.issues.PushJobResult;

public class JobManager {

	private static JobManager jobManager;
	private Map<String, PushJobResult> jobResults;
	private ExecutorService executor;
	private static final Logger logger = LoggerFactory.getLogger(JobManager.class);
		
	public static synchronized JobManager getInstance() {
		if (jobManager == null) {
			jobManager = new JobManager();
		}
		return jobManager;
	}
	
	private JobManager () {
		executor = Executors.newCachedThreadPool();
		jobResults = Collections.synchronizedMap(new PassiveExpiringMap<String,PushJobResult>(1000 * 60 * 60 * 3)); // Expiration time of 3 hours
	}	
	
	public PushJobResult submitJob(Job job){
		executor.submit(new FutureTask<Boolean>(job));
		jobResults.put(job.getId(), new PushJobResult(job.getId(), "Created", null, null));
		return jobResults.get(job.getId());
	}
	
	public PushJobResult getJobResult(String id) {
		return jobResults.get(id);
	}
	
	public void updateJobResult(PushJobResult jobResult) {
    	try {
    		logger.debug(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jobResult));
    	} catch (Exception e) {e.printStackTrace();}
		jobResults.put(jobResult.getId(), jobResult);
	}
}