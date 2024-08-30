/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018,2023. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class PushJobData {

	@Valid
	private AppScanData appscanData;

	@Valid
	private IMData imData;

	public AppScanData getAppscanData() {
		return appscanData;
	}

	public void setAppscanData(AppScanData appscanData) {
		this.appscanData = appscanData;
	}

	public IMData getImData() {
		return imData;
	}

	public void setImData(IMData imData) {
		this.imData = imData;
	}

	public static class AppScanData {

		@NotBlank
		@Schema(required = true, defaultValue = "The Provider of appscan product . example : for Appscan Enterprise it is ASE , for AppScan 360° it is A360 , for AppScan on Cloud it is ASOC ")
		private String appscanProvider;

		@Schema(required = true, defaultValue = "The root URL of the ASE instance if the product is ASE , the root URL of the AppScan 360° instance if the product is AppScan 360 and production site of Appscan on Cloud if it is ASoC", example = "https://hostname:port_number/ase ,https://<AppScan 360° Server URL>/  , \"https://cloud.appscan.com\"")
		@NotBlank
		private String url;

		@Schema(required = true, defaultValue = "Your API Key Id generated from ASE , AppScan 360° or ASoC account")
		@NotBlank
		private String apikeyid;

		@Schema(required = true, defaultValue = "Your API Key Secret generated from ASE , AppScan 360° or ASoC account")
		@NotBlank
		private String apikeysecret;

		@Schema(required = true, defaultValue = "The Id of the ASE , AppScan 360° or ASoC application to be processed")
		@NotBlank
		private String appid;

		@Schema(required = false, example = "25", defaultValue = "Maximum number of issues to process. The default is 25.")
		private Integer maxissues = 25;

		@Schema(required = false, defaultValue = "Issue States to process.The default will process issues that are in 'Open' state. For multiple states, use comma separated values, as for e.g. 'Open, Reopened'")
		private String issuestates = "Open";

		@Schema(required = false, defaultValue = "Applicable only for ASoC.Comma separated list of ASoC policy ids used to filter the results. If not specified, the application's registered policies will be used")
		private String policyids;

		@Schema(required = false, defaultValue = "List of regex experessions to run on Issue fields, to further filter the results.If Id filter is used , only one id can be provided and all other filters will be ignored . If Id is not provided then multiple values of other parameters can be provided like \"Severity\":\"High,Medium\"")
		private Map<String, String> includeIssuefilters;

		@Schema(required = false, defaultValue = "List of regex experessions to run on Issue fields, to further filter the results.These values will be used to exclude the issues from the result.")
		private Map<String, String> excludeIssuefilters;
		@Schema(required = false, defaultValue = "To specify trusted or untrusted connection. For trusted connection , specify 'true'. For Untrusted connection , specify 'false'. This field is applicable only for AppScan 360° . For trusted connections , please ensure that the AppScan 360° server root certificate is imported to the JAVA Keystore.")
		private String trusted = "true";
		@Schema(required = false, defaultValue = "Other internal debug or demo settings")
		private Map<String, String> other;

		public String getAppscanProvider() {
			return appscanProvider;
		}

		public void setAppscanProvider(String appscanProvider) {
			this.appscanProvider = appscanProvider;
		}

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

		public Map<String, String> getIncludeIssuefilters() {
			return includeIssuefilters;
		}

		public void setIncludeIssuefilters(Map<String, String> includeIssuefilters) {
			this.includeIssuefilters = includeIssuefilters;
		}

		public Map<String, String> getExcludeIssuefilters() {
			return excludeIssuefilters;
		}

		public void setExcludeIssuefilters(Map<String, String> excludeIssuefilters) {
			this.excludeIssuefilters = excludeIssuefilters;
		}

		public Map<String, String> getOther() {
			return other;
		}

		public void setOther(Map<String, String> other) {
			this.other = other;
		}

		public String getTrusted() {
			return trusted;
		}

		public void setTrusted(String trusted) {
			this.trusted = trusted;
		}
	}

	static public class IMData {

		@Schema(required = true, defaultValue = "The Issue Management provider to use (e.g. 'jira')")
		@NotBlank
		private String provider;

		@Schema(required = true, defaultValue = "List of configuration settings to be used while processing the issues. See the help of the appropriate Issue Management provider for specifics")
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
	}
}
