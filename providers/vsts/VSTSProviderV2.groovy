/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package vsts

import common.IAppScanIssue
import common.IProvider

class VSTSProviderV2 implements IProvider {

	//Provider name
	static def PROVIDER_NAME = "vsts"

	//Required fields
	static def SERVER_URL  = "url"
	static def API_KEY    = "apiKey"

	//Optional fields
	static def SEVERITYMAP = "severitymap"
	static def ISSUETYPE = "issuetype"

	//Description
	static def PROVIDER_DESCRIPTION =
	[
		'VSTS provider. Configuration fields are below',
		'(Required)' + SERVER_URL  + ': the VSTS URL to connect to',
		'(Required)' + API_KEY    + ': API Key',
		'(Optional)' + ISSUETYPE   + ': Issue Type.Default value is bug',
		'(Optional)' + SEVERITYMAP + ': Map of AppScan Severities to VSTS Priorities.If set, a mapping must be provided for High, Medium, Low, Informational.In case of ASE it should be provided for Critical, High, Medium, Low, Information',
		'Complete JSON Example: (replace single quotes with double quotes)                                                       ',
		'     {                                                                                                    ',
		'       \'appscanData\': {                                                                                   ',
		'           \'appscanProvider\': \'ASE or ASOC\',                                                         ',
		'           \'url\': \'https://cloud.appscan.com\' or \'https://hostname:port_number/ase\',                                                         ',
		'           \'apikeyid\': \'00000000-0000-000000000-000000000000\',                                            ',
		'           \'apikeysecret\': \'111111111-11-1111111-111111111111-1111-11111\',                                ',
		'           \'issuestates\': \'New,Open\',                                                                     ',
		'           \'appid\': \'22222222222-2222222-22222222222222-2\',                                               ',
		'           \'maxissues\': 10,                                                                               ',
		'           \'policyids\': \'333333-33-3-333-3-3333333333333333333-333333-333333\'                             ',
		'           \'includeIssueFilters\': {}                                                                             ',
		'           \'excludeIssueFilters\': {}                                                                             ',
		'        },                                                                                                ',
		'        \'imData\': {                                                                                       ',
		'          \'provider\': \'vsts\',                                                                             ',
		'          \'config\': {                                                                                     ',
		'            \'url\': \'https://dev.azure.com/<VSTS_userid>/<VSTS_projectName>\',                              ',
		'            \'apiKey\': \'apikey\',                                                                            ',
		'            \'issuetype\': \'Bug\',                                                                         ',
		'            \'severitymap\':  {                                                                             ',
		'              \'High\': \'1 - Critical\',                                                                          ',
		'              \'Medium\': \'2 - High\',                                                                        ',
		'              \'Low\': \'3 - Medium\',                                                                           ',
		'              \'Informational\': \'4 - Low\'                                                                  ',
		'            }                                                                                            ',
		'     }                                                                                                    '
	]

	@Override
	public String getId() {
		return PROVIDER_NAME;
	}

	@Override
	List<String> getDescription() {
		return PROVIDER_DESCRIPTION;
	}

	@Override
	public void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors, Map<String, String> results) {

	}

	@Override
	public void submitIssue(IAppScanIssue appscanIssue, Map<String, Object> config, List<String> errors, Map<String, String> results) {
	}
}
