/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.applications;

import io.swagger.annotations.ApiModelProperty;

public class GetApplicationData {
	
	@ApiModelProperty(position = 1,name="appscanProvider", required = true, value = "The provider of Appscan product.For Appscan Enterprise it is ASE and for Appscan on Cloud its is ASOC")
	private String appscanProvider;
	@ApiModelProperty(position = 2, required = true, value = "The root URL of the ASE instance if the product is ASE and production site of Appscan on Cloud if it is ASoC.Example (for ASE) = \"https://hostname:port_number/ase\"")
	private String url ;
	@ApiModelProperty(position = 3, required = true, value = "Your API Key Id generated from ASE or ASoC account")
	private String apikeyid;
	@ApiModelProperty(position = 4, required = true, value = "Your API Key Secret generated from ASE or ASoC account")
	private String apikeysecret;
	@ApiModelProperty(position = 5, required = false, value = "This is an optional field applicable only in case of ASE.If provided,this API will fetch the applications which contains this tag")
	public String tag="";
	
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
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
}
