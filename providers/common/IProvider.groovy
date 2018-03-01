/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package common

import java.util.List
import java.util.Map

/**
 * Interface that Providers must implement.  Very simple for now, but as more features are added this will likely grow
 * 
 * The getId() method is expecting a readable Id.  It's what the user will include in their JSON to direct their 
 * work to your provider.  So probably best to pick a short readable one (ie. "jira")
 * 
 * The getDescription() method is only used by the service to populate the response to the GET /providers call
 * which is only likely to be called when new users are trying things out.   Some explanation for why the Description is a 
 * String list: Swagger UI's rendering of response text is very limiting.  Most or all formatting characters are not respected, 
 * so there is no way (that we've yet found) to have a nicely formatted multi-line description string show well in the response UI.
 * To work around that the String list is used because Swagger does show list items on their own lines
 *  * 
 * The submitIssues API passes the provider:
 *     IAppScanIssue[] issues      : An array of issues to work on. No additional filtering is required. These have already been filtered based on the caller JSON
 *     Map<String, Object> config  : This is the entire config the user sent in as JSON.  Probably best to look at the JIRA example for how it's used
 *     List<String> errors         : If the provider encounters any errors they can populate this list and they will be returned as JSON to the caller
 *     Map<String, String> results : This map must be populated by the Provider. The keys are AppScan issues IDs and the values are whatever reference
 *                                   to the created issue in the other system makes sense. In the case of JIRA,simple URIs to the generated JIRA work item suffice.
 *                                   This Map is then used to call back about to AppScan and update the issues with this metadata.  
 */

interface IProvider {
	String       getId()
	List<String> getDescription()
	void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors, Map<String, String> results)	
}
