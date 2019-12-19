/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package common;

import java.io.File;

public interface IAppScanIssue {

	File getIssueDetails();

	String get(String name);

}

