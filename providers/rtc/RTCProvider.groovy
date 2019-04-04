package rtc
/**
 * © Copyright IBM Corporation 2018.
 * © Copyright PrimeUP Solucoes em TI LTDA 2018.
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

/**
 *	This class Rational Team Concert Provider has the responsibility to validate and submit issues AppScan 
 */
class RTCProvider implements IProvider {
	
	private ServerCommunication connection;
	
	@Override
	public String getId() {
		return Constants.PROVIDER_NAME
	}

	@Override
	List<String> getDescription() {
		return Constants.PROVIDER_DESCRIPTION
	}

	@Override
	public void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors, Map<String, String> results) {

		connection = new ServerCommunication();
		connection.setUpConnection()		

		try {
			if (validate(config, errors)) {
				for (IAppScanIssue issue : issues) {
					submitIssue(issue, config, errors, results)
				}
			}
		} catch (Exception e) {
			errors.add("Internal Server Error while submitting RTC issues: " + e.getMessage())
		}
	}

	/**
	 * Method responsible for validating the configuration file. The attributes: 
	 * Server URL, Username, Password, Issue Type and Project Area must be set.
	 * @param config json settings
	 * @param errors response map errors
	 * @return Boolean resposnse validation attributes
	 */
	private Boolean validate(Map<String, String> config, List<String> errors) {

		//Check for required fields
		boolean valid = true
		if (!config.containsKey(Constants.SERVER_URL)) {
			errors.add("RTC Configuration: URL must be set")
			valid = false
		}
		if (!config.containsKey(Constants.USERNAME)) {
			errors.add("RTC Configuration: username must be set")
			valid = false
		}

		if (!config.containsKey(Constants.PASSWORD)) {
			errors.add("RTC Configuration: password must be set")
			valid = false
		}

		if (!config.containsKey(Constants.ISSUETYPE)) {
			errors.add("RTC Configuration: issuetype must be set")
			valid = false
		}
		if (!config.containsKey(Constants.PROJECTAREA)) {
			errors.add("RTC Configuration: projectarea must be set");
			valid = false
		}
		if (!config.containsKey(Constants.OTHERFIELDS)||!config.get(Constants.OTHERFIELDS).containsKey(Constants.FILEDAGAINST)) {
			errors.add("RTC Configuration: otherfields must be set");
			valid = false
		}
		
		//If there is a trailing / on the passed in rtc URL remove it
		String serverURL = config.get(Constants.SERVER_URL)
		if (serverURL.endsWith("/")) {
			config.put(Constants.SERVER_URL, serverURL.substring(0, serverURL.length() -1))
		}
		return valid
	}

	/**
	 * Method responsible for submitting issue to RTC server.
	 * @param appscanIssue issue AppScan
	 * @param config json settings
	 * @param errors response map errors
	 * @param results response map results
	 */
	private void submitIssue(IAppScanIssue appscanIssue, Map <String,String> config, List<String> errors, Map<String, String> results){
		try {
			//populates attributes from json
			Attributes attr = new Attributes(config, appscanIssue, connection)				

			//populates change request object
			ChangeRequest changeRequest = new ChangeRequest(attr)			

			StringWriter changeRequestData = new StringWriter()
			changeRequest.writeXML(changeRequestData)

			//prepares connection and submit issue
			HttpURLConnection createWorkItemConnection = connection.sendSecureDocument(attr.serverUrl, attr.workItemCreation, attr.username, attr.password, changeRequestData.toString(), ServerCommunication.METHOD_POST)

			if(createWorkItemConnection.responseCode == 201) {
				String workItemURL = createWorkItemConnection.getHeaderField("location")
				results.put(appscanIssue.get("Id"), workItemURL)

				changeRequest = getChangeRequest(workItemURL, createWorkItemConnection.getInputStream(), errors)				

				File issueDetails = appscanIssue.issueDetails
				String attachmentURL = uploadAttachment(attr.serverUrl, attr.username, attr.password, attr.projectAreaId, issueDetails, String.format(Constants.FILE_NAME, appscanIssue.get("Id")), null, errors)

				changeRequest.dcDescription = attr.description				
				changeRequest.attachmentURL = attachmentURL
	
				changeRequestData = new StringWriter()
				changeRequest.writeXML(changeRequestData)

				HttpURLConnection updateWorkItemConnection = connection.sendSecureDocument(attr.serverUrl, workItemURL, attr.username, attr.password, changeRequestData.toString(), ServerCommunication.METHOD_PUT, null)

				if(updateWorkItemConnection.responseCode != 200) {
					errors.add("An error occured while communicating with tracking system on Put: " + updateWorkItemConnection.errorStream.text)
				}
				updateWorkItemConnection.disconnect()
			}
			else {
				errors.add("An error occured while communicating with tracking system on Post: " + createWorkItemConnection.errorStream.text)
			}
			createWorkItemConnection.disconnect()

		} catch (Exception e) {
			errors.add("An exception occured while communicating with tracking system: " + e.getMessage())
		}
	}


	/**
	 * Method responsible for uploading attachment
	 * @param serverURL URL Server
	 * @param username username RTC
	 * @param password password RTC
	 * @param projectAreaUUID project area UUID
	 * @param file file
	 * @param fileName file name
	 * @param authString authentication string
	 * @param errors errors list exception
	 * @return String URL Json Object
	 * @throws IOException input output Exception
	 * @throws URISyntaxException URI syntax exception
	 */
	private String uploadAttachment(String serverURL, String username, String password, String projectAreaUUID, File file, String fileName, String authString, List<String> errors) throws IOException, URISyntaxException {
		HttpURLConnection urlConnection = null
		try {
			String attachmentUploadUrl = String.format(Constants.ATTACHMENT_UPLOAD_URL, serverURL, projectAreaUUID)
			urlConnection = connection.sendOctetStreamFileUploadDocument(serverURL,attachmentUploadUrl, username,  password, file, fileName, authString)

			String response = urlConnection.content.text
			if(urlConnection.responseCode != 200) {
				throw new Exception(response)
			}
			Object jsonObj = new JsonSlurper().parseText(response.substring(response.indexOf("[") + 1, response.lastIndexOf("]")))
			return jsonObj["url"]

		} catch (Exception e) {
			try {
				errors.add("An error occured while communicating with tracking system on Upload: " + ((HttpURLConnection)urlConnection).errorStream.text)
			} catch (Exception f) {
				errors.add("An error occured while communicating with tracking system on Upload: " + e.getMessage() + " & " + f.getMessage())
			}
		}
	}


	/**
	 * Method responsible for transforming response to Change Request
	 * @param crURL URL request
	 * @param is input stream
	 * @param errors errors list exception
	 * @return ChangeRequest Request response
	 */
	private ChangeRequest getChangeRequest(String crURL, InputStream is, List<String> errors) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
			
			// use the factory to create a documentbuilder
			DocumentBuilder builder = factory.newDocumentBuilder()

			// create a new document from input stream
			Document doc = builder.parse(is)

			// get the first element
			Element element = doc.getDocumentElement()
			return new ChangeRequest(crURL, element)
		} catch (Exception e) {
			errors.add("Internal Server Error while transforming response to Change Request: " + e.getMessage())

		}
	}

}