package rtc
/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Document
import org.w3c.dom.Element

import common.IAppScanIssue
import common.IProvider
import groovy.json.JsonSlurper
import rtc.RTCConstants as Constants
import rtc.ServerCommunication 

class RTCProviderV2 implements IProvider {
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
		'(Optional)' + SUMMARY      + ': Issue attributes can be included with %% substitution variables. For example \'AppScan: %IssueType% found at %Location%\'',
		'(Optional)' + DESCRIPTION  + ': Issue attributes can be included with %% substitution variables. For example \'<b>Security issue:</b> %IssueType%<br></br> <b>Scanner:</b> %Scanner%.\'',
		'Complete JSON Example: (replace single quotes with double quotes)                                         ',
		'     {                                                                                                    ',
		'       \'appscanData\': {                                                                                 ',
		'           \'appscanProvider\': \'ASE or A360 or ASOC\',                                                         ',
		'           \'url\': \'https://hostname:port_number/ase\' or \'https://<AppScan 360 Server URL>\' or \'https://cloud.appscan.com\',                                                     ',
		'           \'apikeyid\': \'00000000-0000-000000000-000000000000\',                                        ',
		'           \'apikeysecret\': \'111111111-11-1111111-111111111111-1111-11111\',                            ',
		'           \'appid\': \'22222222222-2222222-22222222222222-2\',                                           ',
		'           \'issuestates\': \'Open,Reopened\',                                                                 ',
		'           \'maxissues\': 10,                                                                             ',
		'           \'other\': { \'checkduplicates\': false }                                                      ',
		'           \'includeIssueFilters\': {}                                                                             ',
		'           \'excludeIssueFilters\': {}                                                                             ',
		'           \'trusted\': \'true\'                                                                                   ',
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
	
	@Override
	public String getId() {
		return PROVIDER_NAME
	}

	@Override
	List<String> getDescription() {
		return PROVIDER_DESCRIPTION
	}

	@Override
	public void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors, Map<String, String> results) {
    }
	
	@Override
	public void submitIssue(IAppScanIssue appscanIssue, Map <String, Object> config, List<String> errors, Map<String, String> results){
	}
}