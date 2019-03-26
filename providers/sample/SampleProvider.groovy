package sample

import common.IAppScanIssue
import common.IProvider

/**
 *  Note: For now the provider loading logic is very basic (and I guess fragile).
 *  Originally the plan was to just use the folder names as proper package names, but I spent more time than I'd care to admit
 *  trying to figure out why the GroovyClassLoader on the Java side seemed to ignore packages and get confused easily when loading classes. 
 *  So this does need a better solution, but for now the rules of Provider file naming are:
 *  1. Name your provider XXXProvider.groovy where XXX is going to be a prefix unlikely to clash with other providers
 *  2. Name any other helper classes you create with the same prefix. 
 *  3. One provider per folder
 */
class SampleProvider implements IProvider {

	String getId() {
		"sample"
	}
	
	List<String> getDescription() {
		Arrays.asList("A trivial provider to show the basics of adding a provider")
	}
	
	public void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors, Map<String, String> results) {
		//Just extract the Id and Severity out of each issue and return them back as a result Map (which the caller of the Service REST API could do something with);
		for (IAppScanIssue issue : issues) {
			results.put(issue.get("Id"), issue.get("Severity"));
		}
	}
	
	public void submitIssue(IAppScanIssue issues, Map<String, Object> config, List<String> errors, Map<String, String> results) {
		//Just extract the Id and Severity out of each issue and return them back as a result Map (which the caller of the Service REST API could do something with);
		//for (IAppScanIssue issue : issues) {
			results.put(issue.get("Id"), issue.get("Severity"));
		//}
	}
}
