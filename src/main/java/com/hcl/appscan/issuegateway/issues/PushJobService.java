package com.hcl.appscan.issuegateway.issues;

import org.springframework.stereotype.Service;

import com.hcl.appscan.issuegateway.AppscanProvider;
import com.hcl.appscan.issuegateway.appscanprovider.ASEProvider;
import com.hcl.appscan.issuegateway.appscanprovider.ASOCProvider;
import com.hcl.appscan.issuegateway.appscanprovider.IAppScanProvider;
import com.hcl.appscan.issuegateway.errors.EntityNotFoundException;
import com.hcl.appscan.issuegateway.jobs.JobManager;
import com.hcl.appscan.issuegateway.providers.ProvidersRepository;

import common.IProvider;

@Service
public class PushJobService {

	public PushJobResult getStatus(String id) throws EntityNotFoundException {
		return JobManager.getInstance().getJobResult(id);
	}

	public PushJobResult createPushJob(ASOCPushJobData submitJobData) {
		PushJobData jobData = new PushJobData();
		PushJobData.AppScanData appscanData = new PushJobData.AppScanData();
		PushJobData.IMData imData = new PushJobData.IMData();
		jobData.setAppscanData(appscanData);
		jobData.setImData(imData);
		jobData.getAppscanData().setAppscanProvider("ASOC");
		jobData.getAppscanData().setUrl(submitJobData.getAppscanData().getUrl());
		jobData.getAppscanData().setApikeyid(submitJobData.getAppscanData().getApikeyid());
		jobData.getAppscanData().setApikeysecret(submitJobData.getAppscanData().getApikeysecret());
		jobData.getAppscanData().setAppid(submitJobData.getAppscanData().getAppid());
		jobData.getAppscanData().setMaxissues(submitJobData.getAppscanData().getMaxissues());
		jobData.getAppscanData().setIssuestates(submitJobData.getAppscanData().getIssuestates());
		jobData.getAppscanData().setPolicyids(submitJobData.getAppscanData().getPolicyids());
		jobData.getAppscanData().setExcludeIssuefilters(submitJobData.getAppscanData().getIssuefilters());
		jobData.getImData().setProvider(submitJobData.getImData().getProvider());
		jobData.getImData().setConfig(submitJobData.getImData().getConfig());
		return createPushJob(jobData);
	}

	public PushJobResult createPushJob(PushJobData submitJobData) {
		IAppScanProvider appscanProvider;
		if (submitJobData.getAppscanData().getAppscanProvider().equalsIgnoreCase(AppscanProvider.ASE.name())) {
			appscanProvider = new ASEProvider(submitJobData);
		} else if (submitJobData.getAppscanData().getAppscanProvider().equalsIgnoreCase(AppscanProvider.ASOC.name())) {
			appscanProvider = new ASOCProvider(submitJobData);
		} else {
			throw new IllegalArgumentException(); // TODO error handling
		}

		IProvider provider = ProvidersRepository.getProviders().get(submitJobData.getImData().getProvider());
		if (provider == null) {
			throw new IllegalArgumentException(); // TODO error handling
		}
		PushJob submitJob = new PushJob(submitJobData, appscanProvider, provider);
		PushJobResult jobResult = JobManager.getInstance().submitJob(submitJob);
		return jobResult;
	}
}
