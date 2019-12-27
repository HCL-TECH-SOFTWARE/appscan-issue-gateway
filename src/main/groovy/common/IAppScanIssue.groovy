///**
// * � Copyright IBM Corporation 2018.
// * � Copyright HCL Technologies Ltd. 2018.
// * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
// */
//package common
//
///**
// * Objects implementing this interface represent one issue in AppScan. They are created by this service and passed to
// * implementors of the {@link IProvider} interface. Objects of type IAppScanIssue hold all the fields of an issue
// * and use the same fields names that exist in the AppScan REST API:
// *   https://cloud.appscan.com/swagger/ui/index#!/Apps/Apps_GetIssuesByAppId \
// * For example, to get the Severity of an issue is just: appscanIssue.get("Severity");
// * The "IssueDetails" {@link java.io.File} is a single issue report, also created by the service, and can be attached to
// * issues opened in other issue management systems.
// */
//interface IAppScanIssue {
//
//	File getIssueDetails()
//
//	String get(String name)
//
//}
//
