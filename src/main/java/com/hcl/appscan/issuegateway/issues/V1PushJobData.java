/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public class V1PushJobData {

	private AppScanDataV1 appscanData;
	private IMDataV1 imData;

	public AppScanDataV1 getAppscanData() {
		return appscanData;
	}

	public IMDataV1 getImData() {
		return imData;
	}

	public class AppScanDataV1 {
		@Schema(required = false, description = "The root URL if connecting to any version of ASoC other than public cloud", example = "https://cloud.appscan.com")
		private String url = "https://cloud.appscan.com";
		@Schema(required = true, description = "Your API Key Id generated from Application Security on Cloud")
		private String apikeyid;
		@Schema(required = true, description = "Your API Key Secret generated from Application Security on Cloud")
		private String apikeysecret;
		@Schema(required = true, description = "The Id of the ASoC application to be processed")
		private String appid;
		@Schema(required = false, example = "25", description = "Maximum number of issues to process. The default is 25. Pass in a value of -1 to have no limit")
		private Integer maxissues = 25;
		@Schema(required = false, description = "Issue States to process.  The default is to only process issues that are 'Open'. To also process new issues use 'New, Open'")
		private String issuestates = "Open";
		@Schema(required = false, description = "Comma separated list of ASoC policy ids used to filter the results. If not specified, the application's registered policies will be used")
		private String policyids;
		@Schema(required = false, description = "List of regex experessions to run on Issue fields, to further filter the results")
		private Map<String, String> issuefilters;
		@Schema(required = false, description = "Other internal debug or demo settings")
		private Map<String, String> other;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getApikeyid() {
			return apikeyid;
		}

		public void setApikeyid(String apikeyid) {
			this.apikeyid = apikeyid;
		}

		public String getApikeysecret() {
			return apikeysecret;
		}

		public void setApikeysecret(String apikeysecret) {
			this.apikeysecret = apikeysecret;
		}

		public String getAppid() {
			return appid;
		}

		public void setAppid(String appid) {
			this.appid = appid;
		}

		public Integer getMaxissues() {
			return maxissues;
		}

		public void setMaxissues(Integer maxissues) {
			this.maxissues = maxissues;
		}

		public String getIssuestates() {
			return issuestates;
		}

		public void setIssuestates(String issuestates) {
			this.issuestates = issuestates;
		}

		public String getPolicyids() {
			return policyids;
		}

		public void setPolicyids(String policyids) {
			this.policyids = policyids;
		}

		public Map<String, String> getIssuefilters() {
			return issuefilters;
		}

		public void setIssuefilters(Map<String, String> issuefilters) {
			this.issuefilters = issuefilters;
		}

		public Map<String, String> getOther() {
			return other;
		}

		public void setOther(Map<String, String> other) {
			this.other = other;
		}

		@Override
		public String toString() {
			return "AppScanDataV1 [url=" + url + ", apikeyid=" + apikeyid + ", apikeysecret=" + apikeysecret + ", appid="
					+ appid + ", maxissues=" + maxissues + ", issuestates=" + issuestates + ", policyids=" + policyids
					+ ", issuefilters=" + issuefilters + ", other=" + other + "]";
		}
	}

	public class IMDataV1 {
		@Schema(required = true, description = "The Issue Management provider to use (e.g. 'jira')")
		private String provider;
		@Schema(required = true, description = "List of configuration settings to be used while processing the issues. See the help of the appropriate Issue Management provider for specifics")
		private Map<String, Object> config;

		public String getProvider() {
			return provider;
		}

		public void setProvider(String provider) {
			this.provider = provider;
		}

		public Map<String, Object> getConfig() {
			return config;
		}

		public void setConfig(Map<String, Object> config) {
			this.config = config;
		}

		@Override
		public String toString() {
			return "IMDataV1 [provider=" + provider + ", config=" + config + "]";
		}
	}

	@Override
	public String toString() {
		return "ASOCPushJobData [appscanData=" + appscanData + ", imData=" + imData + "]";
	}

}
