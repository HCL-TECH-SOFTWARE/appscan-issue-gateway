package rtc
/**
 * © Copyright IBM Corporation 2018.
 * © Copyright PrimeUP Solucoes em TI LTDA 2018.
 * © Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * This class collects all constants used by the RTC provider
 */
public class RTCConstants {
	
	static def ROOT_SERVICES = "/rootservices"
	static def JSECURITYCHECK = "/j_security_check"

	static def AUTHREQUIRED_HEADER = "X-com-ibm-team-repository-web-auth-msg"
	static def AUTHREQUIRED_VALUE = "authrequired"
	static def AUTHFAILED_VALUE = "authfailed"
	static def BASIC_HEADER = "WWW-Authenticate"
	static def AUTHORIZATION_HEADER = "Authorization"
	static def APPLICATION_RDF_XML_TYPE = "application/rdf+xml"
	static def ACCEPT_HEADER = "Accept"
	static def CONTENT_TYPE_HEADER = "Content-Type"
	static def OSLC_CORE_VERSION_HEADER = "OSLC-Core-Version"
	static def OSLC_VERSION = "2.0"
	static def FILED_AGAINST_PROPERTY = "filedAgainst"
	static def FILE_NAME = "IssueDetails-%s.html"
	static def ATTACHMENT_UPLOAD_URL = "%s/service/com.ibm.team.workitem.service.internal.rest.IAttachmentRestService/?projectId=%s&multiple=true"

	//Provider name
	static def PROVIDER_NAME = "rtc"

	//Required fields
	static def SERVER_URL = "url"
	static def USERNAME = "username"
	static def PASSWORD = "password"
	static def PROJECTAREA = "projectarea"
	static def ISSUETYPE = "issuetype"
	
	//Optional fields	
	static def OTHERFIELDS = "otherfields"
	static def FILEDAGAINST = "filedAgainst"
	static def SUMMARY = "summary"
	static def  DESCRIPTION = "description"
	
	//Description
	static def PROVIDER_DESCRIPTION =
	[
		'RTC provider. Configuration fields are below',
		'(Required)' + SERVER_URL   + ': the RTC Server URL to connect to',
		'(Required)' + USERNAME     + ': username',
		'(Required)' + PASSWORD     + ': password',
		'(Required)' + PROJECTAREA  + ': project area',
		'(Required)' + ISSUETYPE    + ': issue type',
		'(Required)' + OTHERFIELDS  + ': other fields',
		'(Required)' + FILEDAGAINST + ': filed against',
		'(Optional)' + SUMMARY      + ': Issue attributes can be included with %% substitution variables. For example : \'AppScan: %IssueType% found at %Location%\'',
		'(Optional)' + DESCRIPTION  + ': Issue attributes can be included with %% substitution variables. For example : \'<b>Security issue:</b> %IssueType%<br></br> <b>Scanner:</b> %Scanner%.\'',
		'Complete JSON Example: (replace single quotes with double quotes)                                         ',
		'     {                                                                                                    ',
		'       \'appscanData\': {                                                                                 ',
		'           \'url\': \'https://cloud.appscan.com\',                                                     ',
		'           \'apikeyid\': \'00000000-0000-000000000-000000000000\',                                        ',
		'           \'apikeysecret\': \'111111111-11-1111111-111111111111-1111-11111\',                            ',
		'           \'appid\': \'22222222222-2222222-22222222222222-2\',                                           ',
		'           \'issuestates\': \'New,Open\',                                                                 ',
		'           \'maxissues\': 10,                                                                             ',
		'           \'other\': { \'checkduplicates\': false }                                                      ',
		'           \'issueFilters\': {}                                                                           ',
		'        },                                                                                                ',
		'        \'imData\': {                                                                                     ',
		'          \'provider\': \'rtc\',                                                                          ',
		'          \'config\': {                                                                                   ',
		'            \'url\': \'https://localhost:9443/ccm\',                                                      ',
		'            \'username\': \'username\',                                                                   ',
		'            \'password\': \'password\',                                                                   ',
		'            \'projectarea\': \'ABC\',                                                                     ',
		'            \'issuetype\': \'defect\',                                                                    ',
		'          \'otherfields\': {                                                                              ',
		'             \'filedAgainst\' : \'My Root Category Name/My Child Category\', 							   ',
		'        }                                                                                                 ',
		'     }                                                                                                    '
	]
}