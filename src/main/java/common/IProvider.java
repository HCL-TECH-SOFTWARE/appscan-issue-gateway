/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018,2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package common;

import java.util.List;
import java.util.Map;

public interface IProvider {

	String getId();

	List<String> getDescription();

	void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors,
	                  Map<String, String> results);

	void submitIssue(IAppScanIssue appscanIssue, Map<String, Object> config, List<String> errors,
	                 Map<String, String> results);

}
