/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import java.util.Map;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
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

	static public class AppScanData {
		@ApiModelProperty(position = 1, required = false, value = "The Provider of appscan product . example : for Appscan Enterprise it is ASE , for Appscan on Cloud it is ASOC.If not provided , the default value would be ASOC")
		private String appscanProvider="ASOC";
		@ApiModelProperty(position = 2, required = true, value = "The root URL of the ASE instance if the product is ASE and production site of Appscan on Cloud if it is ASoC", example = "https://hostname:port_number/ase , \"https://appscan.ibmcloud.com\"")
		@NotBlank
		private String url ;
		@ApiModelProperty(position = 3, required = true, value = "Your API Key Id generated from ASE or ASoC account")
		@NotBlank
		private String apikeyid;
		@ApiModelProperty(position = 4, required = true, value = "Your API Key Secret generated from ASE or ASoC account")
		@NotBlank
		private String apikeysecret;
		@ApiModelProperty(position = 5, required = true, value = "The Id of the ASoC or ASE application to be processed")
		@NotBlank
		private String appid;
		@ApiModelProperty(position = 6, required = false, example = "25", value = "Maximum number of issues to process. The default is 25. Pass in a value of -1 to have no limit")
		private Integer maxissues = 25;
		@ApiModelProperty(position = 7, required = false, value = "Issue States to process.The default will process issues that are in 'Open' state. For multiple states, use comma separated vlaues, as for e.g. 'New, Open'")
		private String issuestates = "Open";
		@ApiModelProperty(position = 8, required = false, value = "Applicable only for ASoC.Comma separated list of ASoC policy ids used to filter the results. If not specified, the application's registered policies will be used")
		private String policyids;
		@ApiModelProperty(position = 9, required = false, value = "List of regex experessions to run on Issue fields, to further filter the results.If Id filter is used , only one id can be provided and all other filters will be ignored . If Id is not provided then multiple values of other parameters can be provided like \"Severity\":\"High,Medium\"")
		private Map<String, String> includeIssuefilters;
		@ApiModelProperty(position = 10, required = false, value = "List of regex experessions to run on Issue fields, to further filter the results.These values will be used to exclude the issues from the result.")
		private Map<String, String> excludeIssuefilters;
		@ApiModelProperty(position = 11, required = false, value = "Other internal debug or demo settings")
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

	}
	
	static public class IMData {
		@ApiModelProperty(position = 1, required = true, value = "The Issue Management provider to use (e.g. 'jira')")
		@NotBlank
		private String provider;
		@ApiModelProperty(position = 2, required = true, value = "List of configuration settings to be used while processing the issues. See the help of the appropriate Issue Management provider for specifics")
		private Map<String,Object> config;
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