package rtc
/**
 * © Copyright IBM Corporation 2018.
 * © Copyright PrimeUP Solucoes em TI LTDA 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * This class collects all constants used by the RTC provider
 */
public class RTCConstants {
	
	static final String ROOT_SERVICES = "/rootservices"
	static final String JSECURITYCHECK = "/j_security_check"

	static final String AUTHREQUIRED_HEADER = "X-com-ibm-team-repository-web-auth-msg"
	static final String AUTHREQUIRED_VALUE = "authrequired"
	static final String AUTHFAILED_VALUE = "authfailed"
	static final String BASIC_HEADER = "WWW-Authenticate"
	static final String AUTHORIZATION_HEADER = "Authorization"
	static final String APPLICATION_RDF_XML_TYPE = "application/rdf+xml"
	static final String ACCEPT_HEADER = "Accept"
	static final String CONTENT_TYPE_HEADER = "Content-Type"
	static final String OSLC_CORE_VERSION_HEADER = "OSLC-Core-Version"
	static final String OSLC_VERSION = "2.0"
	static final String FILED_AGAINST_PROPERTY = "filedAgainst"
	static final String FILE_NAME = "IssueDetails-%s.html"
	static final String ATTACHMENT_UPLOAD_URL = "%s/service/com.ibm.team.workitem.service.internal.rest.IAttachmentRestService/?projectId=%s&multiple=true"

	//Provider name
	static final String PROVIDER_NAME = "rtc"

	//Required fields
	static final String SERVER_URL = "url"
	static final String USERNAME = "username"
	static final String PASSWORD = "password"
	static final String PROJECTAREA = "projectarea"
	static final String ISSUETYPE = "issuetype"
	
	//Optional fields	
	static final String OTHERFIELDS = "otherfields"
	static final String FILEDAGAINST = "filedAgainst"
	static final String SUMMARY = "summary"
	static final String DESCRIPTION = "description"
	
	//Description
	static final String PROVIDER_DESCRIPTION =
	[
		'RTC provider. Configuration fields are below',
		'(Required)' + SERVER_URL   + ': the RTC Server URL to connect to',
		'(Required)' + USERNAME     + ': username',
		'(Required)' + PASSWORD     + ': password',
		'(Required)' + PROJECTAREA  + ': project area',
		'(Required)' + ISSUETYPE    + ': issue type',
		'(Required)' + OTHERFIELDS  + ': other fields',
		'(Required)' + FILEDAGAINST + ': filed against',
		'(Optional)' + SUMMARY      + ': Override default issue summary. Issue attributes can be included with %% subsitution varibles.  For example the default is \'AppScan: %IssueType% found at %Location%\'',
		'(Optional)' + DESCRIPTION  + ': Override default issue description. Issue attributes can be included with %% subsitution varibles For example the default is \'<b>Security issue:</b> %IssueType%<br></br> <b>Scanner:</b> %Scanner%.\'',
		'Complete JSON Example: (replace single quotes with double quotes)                                         ',
		'     {                                                                                                    ',
		'       \'appscanData\': {                                                                                 ',
		'           \'url\': \'https://appscan.ibmcloud.com\',                                                     ',
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