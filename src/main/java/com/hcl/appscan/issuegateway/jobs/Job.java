/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.jobs;

import java.util.UUID;
import java.util.concurrent.Callable;

import com.hcl.appscan.issuegateway.issues.PushJobResult;

public abstract class Job implements Callable<Boolean>  {
	
	private String id = UUID.randomUUID().toString();
	
	@Override
	public abstract Boolean call() throws Exception;

	public String getId() {
		return this.id;
	}
	
    protected void updateResult(PushJobResult result) {
    	JobManager.getInstance().updateJobResult(result);
    }
}
