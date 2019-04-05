/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018,2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package common;

import java.util.List;
import java.util.Map;

public interface IProvider {
	public String       getId();
	public List<String> getDescription();
	public void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors, Map<String, String> results);
	//this is to process the issues one by one.Only for ASE.
	public void submitIssue(IAppScanIssue appscanIssue, Map<String, Object> config, List<String> errors, Map<String, String> results);
}
