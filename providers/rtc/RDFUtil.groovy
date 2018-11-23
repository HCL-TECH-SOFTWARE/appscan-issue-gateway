package rtc
/**
 * © Copyright IBM Corporation 2018.
 * © Copyright PrimeUP Solucoes em TI LTDA 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import rtc.RTCConstants as Constants 
/**
 * This assistant class responsible for creating URLs from Xpath 
 */
class RDFUtil {
	
	final String SERVICE_PROVIDERS_XPATH = "/rdf:Description/oslc_cm:cmServiceProviders/@rdf:resource"
	final String SERVICE_PROVIDER_XPATH = "//oslc:ServiceProvider/dcterms:title/text()[ . = '%s']/ancestor::oslc:serviceProvider/oslc:ServiceProvider/@rdf:about"
	final String WORK_ITEM_CREATION_XPATH = "//oslc:creationFactory/oslc:CreationFactory/oslc:resourceType['%s' = substring(@rdf:resource, string-length(@rdf:resource) - string-length('%s') +1)]/ancestor::oslc:CreationFactory/oslc:creation/@rdf:resource"
	final String WORK_ITEM_RESOURCE_SHAPE_XPATH = "//oslc:creationFactory/oslc:CreationFactory/oslc:resourceType['%s' = substring(@rdf:resource, string-length(@rdf:resource) - string-length('%s') +1)]/ancestor::oslc:CreationFactory/oslc:resourceShape/@rdf:resource"
	final String PROJECT_AREA_ID_XPATH = "//oslc:ServiceProvider/dcterms:title/text()[ . = '%s']/ancestor::oslc:ServiceProvider/oslc:details/@rdf:resource"
	
	final String ALLOWED_VALUE_XPATH = "//oslc:allowedValue/@rdf:resource"


	final String ATTRIBUTE_ALLOWED_VALUES_XPATH = "//rdf:Description/oslc:name[text()='%s']/parent::rdf:Description/oslc:allowedValues/@rdf:resource"
	final String ATTRIBUTE_ALLOWED_VALUE_NAME_XPATH = "//%s[text()='%s']"
		
	String serverUrl
	String username
	String password
	ServerCommunication connection
	
	String rootServicesURL
	String serviceProviderXPath
	String workItemCreationXPath
	String workItemResourceShapeXPath
	
	String projectAreaId
	String serviceProvidersURL
	String serviceProviderURL
	String workItemCreationURL
	String workItemResourceShapeURL
	
	/**
	 * Construtor Method to prepare RTC connection 
	 * @param serverUrl Server URL
	 * @param username RTC user
	 * @param password RTC password
	 * @param connection connection RTC
	 * @param projectArea Project Area to job
	 * @param workItemType WorkItem Type used
	 */
	RDFUtil (String serverUrl, String username, String password, ServerCommunication connection, String projectArea, String workItemType) {
		this.serverUrl = serverUrl
		this.username = username
		this.password = password
		this.connection = connection
		
		rootServicesURL = serverUrl +  Constants.ROOT_SERVICES
		serviceProviderXPath = String.format(SERVICE_PROVIDER_XPATH, projectArea)
		workItemCreationXPath = String.format(WORK_ITEM_CREATION_XPATH, workItemType, workItemType)
		workItemResourceShapeXPath = String.format(WORK_ITEM_RESOURCE_SHAPE_XPATH, workItemType, workItemType)		
		
		serviceProvidersURL = extractFromDocument( rootServicesURL, SERVICE_PROVIDERS_XPATH)
		serviceProviderURL = extractFromDocument( serviceProvidersURL, serviceProviderXPath)
		workItemCreationURL = extractFromDocument( serviceProviderURL, workItemCreationXPath)
		
		String temp = extractFromDocument( serviceProvidersURL, String.format(PROJECT_AREA_ID_XPATH, projectArea))
		projectAreaId =  temp.substring(temp.lastIndexOf("/") + 1, temp.length())
			
		workItemResourceShapeURL = extractFromDocument( serviceProviderURL, workItemResourceShapeXPath)
	}
	
	/**
	 * Get URLs from the attribute
	 * @param attributeName Attribute name
	 * @param attribute Attribute
	 * @return String URL to connection 
	 */
	public String getValueURL(String attributeName, String attributeId, String attributeQName) {
		if (workItemResourceShapeURL == null) {
			return null
		}		
		String attributeAllowedValuesXPath = String.format(ATTRIBUTE_ALLOWED_VALUES_XPATH, attributeId)
		String attributeAllowedValueNameXPath = String.format(ATTRIBUTE_ALLOWED_VALUE_NAME_XPATH, attributeQName, attributeName)
		String attributeAllowedValuesURL = extractFromDocument( workItemResourceShapeURL, attributeAllowedValuesXPath)
		String attributeValueURL = findEnumerationValueAtDocument( attributeAllowedValuesURL, ALLOWED_VALUE_XPATH, attributeAllowedValueNameXPath)

		return attributeValueURL;
	}
	
	
	/**
	 * Method responsible for creating node structure from xpath
	 * @param urlToGet Url to connection
	 * @param xpathExpression Xpath Expression
	 * @return String Node content from xpath
	 */
	private String extractFromDocument(String urlToGet, String xpathExpression) {
		if(urlToGet == null || urlToGet.length() == 0) {
			throw new IllegalArgumentException("urlToGet must not be null")
		}

		XPathFactory factory = XPathFactory.newInstance()
		XPath xpath = factory.newXPath()
		xpath.setNamespaceContext(new NamespaceContextMap([
			"rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
			"oslc_cm", "http://open-services.net/xmlns/cm/1.0/",
			"oslc", "http://open-services.net/ns/core#",
			"rtc_cm", "http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/",
			"dcterms", "http://purl.org/dc/terms/"] as String[]))
		HttpURLConnection urlConnection = connection.sendGetForSecureDocument(serverUrl, urlToGet, username, password)
		InputStream response = urlConnection.getInputStream()

		InputSource source = new InputSource(response)
		Node node = (Node) (xpath.evaluate(xpathExpression, source, XPathConstants.NODE))
		urlConnection.disconnect()
		
		if(node == null) {
			return null
		}
		
		return node.getTextContent()
	}

	/**
	 * Method responsible for finding enumeration value to create node structure from xpath
	 * @param urlToGet URL to connection
	 * @param xpathExpression Xpath expression
	 * @param enumValueXpathExpression Xpath Expression values
	 * @return String Node content item from xpath
	 */
	private String findEnumerationValueAtDocument(String urlToGet, String xpathExpression, String enumValueXpathExpression) {
		XPathFactory factory = XPathFactory.newInstance()
		XPath xpath = factory.newXPath()
		xpath.setNamespaceContext(new NamespaceContextMap([
			"rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", 
			"oslc_cm", "http://open-services.net/xmlns/cm/1.0/", 
			"oslc", "http://open-services.net/ns/core#", 
			"rtc_cm", "http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/", 
			"dcterms", "http://purl.org/dc/terms/",
			"rdfs", "http://www.w3.org/2000/01/rdf-schema#"] as String[]))
		
		HttpURLConnection urlConnection = connection.sendGetForSecureDocument(serverUrl, urlToGet, username, password)
		InputStream response = urlConnection.getInputStream()

		InputSource source = new InputSource(response);
		NodeList nodes = (NodeList) (xpath.evaluate( xpathExpression, source, XPathConstants.NODESET))

		urlConnection.disconnect()
		if(nodes == null || nodes.getLength() == 0) {
			throw new Exception("The xpath expression ${xpathExpression} did not return anything from  ${urlToGet}")
		}
		
		int length = nodes.getLength()
		for (int i = 0; i < length; i++) {
			String result = extractFromDocument(nodes.item(i).getTextContent(), enumValueXpathExpression)
			if(result != null) {
				return nodes.item(i).getTextContent()
			}
		}
		
		return null
	}	
}
