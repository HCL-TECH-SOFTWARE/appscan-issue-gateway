/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ASEIssueDetail {
	private String appReleaseId;
	private String issueId;
	private String issueTypeId;
	private String isThirdParty;
	private String dateCreated;
	private String lastUpdated;
	private String fixRecommendation;
	private String advisory;
	private AttributeCollection attributeCollection;
	
	@JsonAnyGetter
	public String getFixRecommendation() {
		return fixRecommendation;
	}
	@JsonAnySetter
	public void setFixRecommendation(String fixRecommendation) {
		this.fixRecommendation = fixRecommendation;
	}
	@JsonAnyGetter
	public String getAdvisory() {
		return advisory;
	}
	@JsonAnySetter
	public void setAdvisory(String advisory) {
		this.advisory = advisory;
	}
	@JsonAnyGetter
	public String getAppReleaseId() {
		return appReleaseId;
	}
	@JsonAnySetter
	public void setAppReleaseId(String appReleaseId) {
		this.appReleaseId = appReleaseId;
	}
	@JsonAnyGetter
	public String getIssueId() {
		return issueId;
	}
	@JsonAnySetter
	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}
	@JsonAnyGetter
	public String getIssueTypeId() {
		return issueTypeId;
	}
	@JsonAnySetter
	public void setIssueTypeId(String issueTypeId) {
		this.issueTypeId = issueTypeId;
	}
	@JsonAnyGetter
	public String getIsThirdParty() {
		return isThirdParty;
	}
	@JsonAnySetter
	public void setIsThirdParty(String isThirdParty) {
		this.isThirdParty = isThirdParty;
	}
	@JsonAnyGetter
	public String getDateCreated() {
		return dateCreated;
	}
	@JsonAnySetter
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	@JsonAnyGetter
	public String getLastUpdated() {
		return lastUpdated;
	}
	@JsonAnySetter
	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	@JsonAnyGetter
	public AttributeCollection getAttributeCollection() {
		return attributeCollection;
	}
	@JsonAnySetter
	public void setAttributeCollection(AttributeCollection attributeCollection) {
		this.attributeCollection = attributeCollection;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AttributeCollection{
		public Attributes[] attributeArray;
		@JsonAnyGetter
		public Attributes[] getAttributeArray() {
			return attributeArray;
		}
		@JsonAnySetter
		public void setAttributeArray(Attributes[] attributeArray) {
			this.attributeArray = attributeArray;
		}
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Attributes{
			public String name;
			public String [] value;
			public String lookup;
			public String issueAttributeDefinitionId;
			public String attributeType;
			public String attributeCategory;
			public String contributesToUnique;
			public String updateable;
			public String getName() {
				return name;
			}
			@JsonAnySetter
			public void setName(String name) {
				this.name = name;
			}
			@JsonAnyGetter
			public String[] getValue() {
				return value;
			}
			@JsonAnySetter
			public void setValue(String[] value) {
				this.value = value;
			}
			@JsonAnyGetter
			public String getLookup() {
				return lookup;
			}
			@JsonAnySetter
			public void setLookup(String lookup) {
				this.lookup = lookup;
			}
			@JsonAnyGetter
			public String getIssueAttributeDefinitionId() {
				return issueAttributeDefinitionId;
			}
			@JsonAnySetter
			public void setIssueAttributeDefinitionId(String issueAttributeDefinitionId) {
				this.issueAttributeDefinitionId = issueAttributeDefinitionId;
			}
			@JsonAnyGetter
			public String getAttributeType() {
				return attributeType;
			}
			@JsonAnySetter
			public void setAttributeType(String attributeType) {
				this.attributeType = attributeType;
			}
			@JsonAnyGetter
			public String getAttributeCategory() {
				return attributeCategory;
			}
			@JsonAnySetter
			public void setAttributeCategory(String attributeCategory) {
				this.attributeCategory = attributeCategory;
			}
			@JsonAnyGetter
			public String getContributesToUnique() {
				return contributesToUnique;
			}
			@JsonAnySetter
			public void setContributesToUnique(String contributesToUnique) {
				this.contributesToUnique = contributesToUnique;
			}
			@JsonAnyGetter
			public String getUpdateable() {
				return updateable;
			}
			@JsonAnySetter
			public void setUpdateable(String updateable) {
				this.updateable = updateable;
			}
		}

	}
	
	}
