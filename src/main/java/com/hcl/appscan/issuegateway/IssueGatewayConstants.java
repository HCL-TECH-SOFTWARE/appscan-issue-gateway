/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway;

public interface IssueGatewayConstants {
	
	String COMMENT_TOKEN 		     ="AppScan Issue Gateway";
	
	//Header Parameters Constants 
	String  HEADER_ASC_XSRF_TOKEN 	 = "asc_xsrf_token";
	String  HEADER_COOKIE 			 = "cookie";
	String  HEADER_CONTENT_TYPE		 = "Content-Type";
	String  HEADER_ACCEPT			 = "Accept";
	String  HEADER_ACCEPT_LANGUAGE	 = "Accept-Language";
	String  HEADER_AUTHORIZATION	 = "Authorization";
	
	//REST API for ASE
	String  ASE_API_APPLICATIONS     = "/api/applications";
	String  ASE_API_APIKEYLOGIN      = "/api/keylogin/apikeylogin";
	String  ASE_API_APPS_COUNT       = "/api/summaries/apps/count";
	String  ASE_API_ISSUE_DETAILS    = "/api/issues/ISSUEID/application/APPID";
	String  ASE_API_ISSUE_UPDATE     = "/api/issues/ISSUEID";
	
	//REST API for ASOC
	String ASOC_API__APIKEYLOGIN  	 = "/api/v2/Account/ApiKeyLogin";
	String ASOC_API_APPSCOUNT        = "/api/v2/Apps/Count";
	String ASOC_API_APPLICATIONS     = "/api/V2/Apps";
	String ASOC_API_COMMENT 	     = "/api/v2/Issues/ISSUEID/Comments";
	
	
}