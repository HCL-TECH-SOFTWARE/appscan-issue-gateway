/**
 * © Copyright HCL Technologies Ltd. 2019, 2026.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.ase;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class ASEExternalIdHandler implements ASEConstants {

	public boolean isExternalIdPresent(AppScanIssue issue, PushJobData jobData, List<String> errors)throws Exception {
		ResponseEntity<ASEIssueDetail> details= getIssueDetail((String)issue.get("id"), jobData, errors);
		ASEIssueDetail.AttributeCollection.Attributes [] attributesArray=details.getBody().getAttributeCollection().getAttributeArray();
		// this is to directly check the externalId which is present as the 40th element in the attribute array
		if (attributesArray[39].getLookup().equals("externalid")) {
			if (attributesArray[39].getValue().length>0) 
				return true;
			return false;
		}
		//fallback if the 40th element is not externalId
		for (ASEIssueDetail.AttributeCollection.Attributes attribute :attributesArray) {
			if (attribute.getLookup().equals("externalid")) {
				if (attribute.getValue().length>0) 
					return true;
				break;
			}
		}
		return false;
		
	}
	
	public void updateExternalId (PushJobData jobData, List<String> errors, Map<String, String> results) throws Exception {
		for (String issueId:results.keySet()) {
			updateExternalId(issueId, jobData, errors, results);
		}
		
	}
	public void updateExternalId(String issueId,PushJobData jobData, List<String> errors, Map<String, String> results) throws Exception{
		if (!results.get(issueId).startsWith("http")) {
			return;
		}
		ResponseEntity<ASEIssueDetail> response=getIssueDetail(issueId, jobData, errors);
		ASEIssueDetail issueDetail=response.getBody();
		ASEIssueDetail.AttributeCollection.Attributes externalIdAttribute=null;
		ASEIssueDetail.AttributeCollection.Attributes [] attributesArray=issueDetail.getAttributeCollection().getAttributeArray();
		
		if (attributesArray[39].getLookup().equals("externalid")) {
			externalIdAttribute=attributesArray[39];
		}
		else {
			for (ASEIssueDetail.AttributeCollection.Attributes attribute:issueDetail.getAttributeCollection().getAttributeArray()) {
				if (attribute.getLookup().equals("externalid")) {
					externalIdAttribute=attribute;
					break;
				}
			}
		}
		//create a request Entity
		IssueUpdateRequest requestEntity=new IssueUpdateRequest();
		requestEntity.appReleaseId=issueDetail.getAppReleaseId();
		requestEntity.issueId=issueDetail.getIssueId();
		IssueUpdateRequest.IssueAttributeCollection attributeCollection=new IssueUpdateRequest.IssueAttributeCollection();
		requestEntity.attributeCollection=attributeCollection;
		IssueUpdateRequest.IssueAttributeCollection.IssueAttribute requestExternalIdAttribute=new IssueUpdateRequest.IssueAttributeCollection.IssueAttribute();
		requestExternalIdAttribute.name=externalIdAttribute.name;
		requestExternalIdAttribute.attributeType=externalIdAttribute.attributeType;
		requestExternalIdAttribute.issueAttributeDefinitionId=externalIdAttribute.issueAttributeDefinitionId;
		String[] values= {results.get(issueId)};
		requestExternalIdAttribute.value=values;
		IssueUpdateRequest.IssueAttributeCollection.IssueAttribute [] attributeArray= {requestExternalIdAttribute};
		attributeCollection.attributeArray=attributeArray;
		requestEntity.attributeCollection=attributeCollection;
		
		String url=jobData.getAppscanData().getUrl()+ASE_API_ISSUE_UPDATE.replaceAll("ISSUEID", issueId);
		RestTemplate restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
		restTemplate.setErrorHandler(new ResponseErrorHandler());
		HttpHeaders headers = new HttpHeaders();
		headers.add(HEADER_ASC_XSRF_TOKEN, ASEAuthHandler.getInstance().getBearerToken(jobData));
		final List<HttpCookie> cookies=ASEAuthHandler.getInstance().getCookies();
	 	if (cookies != null) {
	       StringBuilder sb = new StringBuilder();
	       for (HttpCookie cookie : cookies) {
	           sb.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
	       }
	       headers.add("Cookie", sb.toString());
	    }
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		headers.add("If-Match", response.getHeaders().getETag());
		HttpEntity<IssueUpdateRequest> entity =new HttpEntity<>(requestEntity,headers);
		ResponseEntity<ASEIssueDetail> responseEntity=restTemplate.exchange(url,  HttpMethod.PUT, entity, ASEIssueDetail.class);
		
		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			errors.add("An error occured updating the external id in AppScan issue. A status code of " + responseEntity.getStatusCodeValue() + " was received from " + url);
		}
		
	}
	
	private ResponseEntity<ASEIssueDetail> getIssueDetail(String issueId, PushJobData jobData, List<String> errors) throws Exception{
		String url=jobData.getAppscanData().getUrl()+ASE_API_ISSUE_DETAILS.replaceAll("ISSUEID", issueId).replaceAll("APPID",jobData.getAppscanData().getAppid());
		RestTemplate restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
		restTemplate.setErrorHandler(new ResponseErrorHandler());
		HttpHeaders headers = new HttpHeaders();
		headers.add(HEADER_ASC_XSRF_TOKEN, ASEAuthHandler.getInstance().getBearerToken(jobData));
		final List<HttpCookie> cookies=ASEAuthHandler.getInstance().getCookies();
	 	if (cookies != null) {
	        StringBuilder sb = new StringBuilder();
	        for (HttpCookie cookie : cookies) {
	           sb.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
	        }
	        headers.add("Cookie", sb.toString());
	    }
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<String> entity =new HttpEntity<>(headers);
		ResponseEntity<ASEIssueDetail> responseEntity=restTemplate.exchange(url,  HttpMethod.GET, entity, ASEIssueDetail.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			return responseEntity;
		}
	    errors.add("An error occured retrieving issue details. A status code of " + responseEntity.getStatusCodeValue() + " was received from " + url);
	    return null;
		
	}

	@SuppressWarnings("unused")
	private static class IssueUpdateRequest{
		public String appReleaseId;
		public String issueId;
		public IssueAttributeCollection attributeCollection;
		
		
		public String getAppReleaseId() {
			return appReleaseId;
		}


		public void setAppReleaseId(String appReleaseId) {
			this.appReleaseId = appReleaseId;
		}


		public String getIssueId() {
			return issueId;
		}


		public void setIssueId(String issueId) {
			this.issueId = issueId;
		}


		public IssueAttributeCollection getAttributeCollection() {
			return attributeCollection;
		}


		public void setAttributeCollection(IssueAttributeCollection attributeCollection) {
			this.attributeCollection = attributeCollection;
		}


		public static class IssueAttributeCollection {
			public IssueAttribute[] attributeArray;
			
			public IssueAttribute[] getAttributeArray() {
				return attributeArray;
			}

			public void setAttributeArray(IssueAttribute[] attributeArray) {
				this.attributeArray = attributeArray;
			}

			public static class IssueAttribute {
				String name;
				String attributeType;
				String issueAttributeDefinitionId ;
				String [] value;
				public String getName() {
					return name;
				}
				public void setName(String name) {
					this.name = name;
				}
				public String getAttributeType() {
					return attributeType;
				}
				public void setAttributeType(String attributeType) {
					this.attributeType = attributeType;
				}
				public String getIssueAttributeDefinitionId() {
					return issueAttributeDefinitionId;
				}
				public void setIssueAttributeDefinitionId(String issueAttributeDefinitionId) {
					this.issueAttributeDefinitionId = issueAttributeDefinitionId;
				}
				public String[] getValue() {
					return value;
				}
				public void setValue(String[] value) {
					this.value = value;
				}
				
			}
		}
	}
}
