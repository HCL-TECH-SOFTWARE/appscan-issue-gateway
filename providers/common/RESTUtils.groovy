/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package common

import groovy.json.JsonSlurper

/**
 * Some REST Utilities which may be handy.  These will likely need to be generalized to be more broadly useful
 * For example, generalized content type handling, generalized response handling, etc
 */
class RESTUtils {
	
	//Returns the text of the response (or error messages).  It is up to callers of this method to parse and handle the JSON coming back
	public static postWithJSON(apiURL, authorization, jsonPayload, headers, List<String> errors) {
	
		def connection = apiURL.toURL().openConnection()
		connection.addRequestProperty("Authorization", authorization)
		connection.addRequestProperty("Content-Type", "application/json")
		if (headers != null) {
			for (String headerKey : headers.keySet()) {
				connection.addRequestProperty(headerKey, headers.get(headerKey))
			}
		}
		connection.setRequestMethod("POST")
		connection.doOutput = true
		connection.outputStream.withWriter{
			it.write(jsonPayload)
			it.flush()
		}
		connection.connect()

		try {
			def responseText = connection.content.text
			return responseText
		} catch (IOException e) {
			try {
				errors.add("An error occured while communicating with JIRA: " + ((HttpURLConnection)connection).errorStream.text)
			} catch (Exception f) {
				errors.add("An error occured while communicating with JIRA: " + e.getMessage()+ " & " + f.getMessage())
			}
		}	
	}
	
	public static postMultiPartFileUpload(apiURL, authorization, filePayload, fileName, headers, List<String> errors) {
		
		String boundaryString = "AppScanRandom123512351213";
		
		def connection = apiURL.toURL().openConnection()
		connection.addRequestProperty('Authorization', authorization)
		connection.addRequestProperty('X-Atlassian-Token', 'no-check')
		if (headers != null) {
			for (String headerKey : headers.keySet()) {
				connection.addRequestProperty(headerKey, headers.get(headerKey))
			}
		}
		connection.addRequestProperty("Content-Type", 'multipart/form-data; boundary=' + boundaryString)
		connection.setRequestMethod("POST")
		connection.doOutput = true
		connection.doInput = true
		
       	connection.outputStream.withWriter{
			def thePart = generatePart(filePayload, fileName, boundaryString)
			it.write(thePart)
			it.flush()
		}
		connection.connect()

		try {
			connection.content.text
		} catch (IOException e) {
			try {
				errors.add("An error occured while communicating with JIRA: " + ((HttpURLConnection)connection).errorStream.text)
			} catch (Exception f) {
				errors.add("An error occured while communicating with JIRA: " + e.getMessage() + " & " + f.getMessage())
			}
		}	
	}
		
	private static String generatePart(File theFile, String fileName, String boundaryString) {
		StringBuffer buf = new StringBuffer();
		buf.append("--" + boundaryString + "\r\n")
		buf.append("Content-Disposition: form-data; name=\"file\"; filename=\""+ fileName +"\"\r\n");
		buf.append("\nContent-Type: text/html\r\n");
		buf.append("\r\n");		 
		// Write the actual file contents
		BufferedReader br = new BufferedReader(new FileReader(theFile))
		String nextLine = null;
		while ((nextLine = br.readLine()) != null) {
			buf.append(nextLine + "\r\n");
		}
		 
		// Mark the end of the multipart http request
		buf.append("--" + boundaryString + "--\r\n");
		return buf.toString();
	}

}
