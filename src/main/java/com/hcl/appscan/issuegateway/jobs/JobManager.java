/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018,2019. 
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

import com.fasterxml.jackson.core.JsonProcessingException;
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

	private JobManager() {
		executor = Executors.newCachedThreadPool();
		// Expiration time of 3 hours
		jobResults = Collections.synchronizedMap(new PassiveExpiringMap<>(1000 * 60 * 60 * 3));
	}

	public PushJobResult submitJob(Job job) {
		executor.submit(new FutureTask<>(job));
		jobResults.put(job.getId(), new PushJobResult(job.getId(), "Created", null, null));
		return jobResults.get(job.getId());
	}

	public PushJobResult getJobResult(String id) {
		if (!jobResults.containsKey(id)) {
			throw new IllegalArgumentException("Job id not found: " + id +".Please check the logs before proceeding further.");
		}
		return jobResults.get(id);
	}

	public void updateJobResult(PushJobResult jobResult) {
		try {
			logger.debug(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jobResult));
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		}
		jobResults.put(jobResult.getId(), jobResult);
	}
}