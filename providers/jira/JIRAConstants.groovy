/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package jira
import common.IAppScanIssue

public class JIRAConstants {
	
	//Provider name
	static def PROVIDER_NAME = "jira"

	//Required fields
	static def SERVER_URL  = "url"
	static def USERNAME    = "username"
	static def PASSWORD    = "password"
	static def PROJECTKEY  = "projectkey"
	
	//Optional fields
	static def ISSUETYPE     = "issuetype"
	static def SEVERITYFIELD = "severityfield"
	static def SEVERITYMAP   = "severitymap"
	static def SUMMARY       = "summary"
	static def OTHERFIELDS   = "otherfields"
	
	//Description
	static def PROVIDER_DESCRIPTION =
	[
		'JIRA provider. Configuration fields are below',
		'(Required)' + SERVER_URL    + ': the JIRA URL to connect to',
		'(Required)' + USERNAME      + ': User name',
		'(Required)' + PASSWORD      + ': Password',
		'(Required)' + PROJECTKEY    + ': Project Key',
	    '(Optional)' + SUMMARY       + ': Override default issue summary. Issue attributes can be included with %% subsitution varibles.  For example the default is \'AppScan: %IssueType% found at %Location%\'',
		'(Optional)' + SEVERITYFIELD + ': Field Id that corresponds to \'priority\' or \'severity\'. This field will be populated with the AppScan Issue Severity.  Default value = \'priority\'',
		'(Optional)' + SEVERITYMAP   + ': Map of AppScan Severities to JIRA Priorities.  If set, a mapping must be provided for High, Medium, Low, Informational',
		'(Optional)' + OTHERFIELDS   + ': Additional JSON that should be sent when creating JIRA issues.  For example:  { labels: [\'appscan\',\'security\'] }',
		'Complete JSON Example: (replace single quotes with double quotes and ignore leading and trailing double quotes on each line)                                                       ',
		'     {                                                                                                    ',                                                                 
	    '       \'appscanData\': {                                                                                   ',                                     
		'           \'url\': \'https://cloud.appscan.com\',                                                         ',
		'           \'apikeyid\': \'00000000-0000-000000000-000000000000\',                                            ',
		'           \'apikeysecret\': \'111111111-11-1111111-111111111111-1111-11111\',                                ',
		'           \'issuestates\': \'New,Open\',                                                                     ',
		'           \'appid\': \'22222222222-2222222-22222222222222-2\',                                               ',
		'           \'maxissues\': 10,                                                                               ',
        '           \'policyids\': \'333333-33-3-333-3-3333333333333333333-333333-333333\'                             ',
		'           \'issueFilters\': {}                                                                             ',
	    '        },                                                                                                ',
	    '        \'imData\': {                                                                                       ',
		'          \'provider\': \'jira\',                                                                             ',
		'          \'config\': {                                                                                     ',
     	'            \'url\': \'http://localhost:8080\',                                                               ',
        '            \'username\': \'testuser\',                                                                       ',
        '            \'password\': \'passwopd\',                                                                       ',
        '            \'projectkey\': \'ABC\',                                                                          ',
        '            \'issuetype\': \'Story\',                                                                         ',
        '            \'summary\': \'Security issue: %IssueType% found by %Scanner%.  We must fix it!\',                ',
        '            \'severityfield\': \'severity\'                                                                   ',
        '            \'severitymap\':  {                                                                             ',
        '              \'High\': \'Highest\',                                                                          ',
        '              \'Medium\': \'Highest\',                                                                        ',
        '              \'Low\': \'Highest\',                                                                           ',
        '              \'Informational\': \'Highest\'                                                                  ',
        '            },                                                                                            ',
        '          \'otherfields\': {                                                                                ',  
        '             \'labels\' : [                                                                                 ',
        '               \'appscan\',                                                                                 ', 
        '               \'security\'                                                                                 ',
        '          ]                                                                                               ',
        '        }                                                                                                 ',
        '     }                                                                                                    '  
	]	
}