/**
 * © Copyright IBM Corporation 2018.
 * © Copyright PrimeUP Solucoes em TI LTDA 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package rtc

import java.text.SimpleDateFormat

import org.w3c.dom.Element
import org.w3c.dom.NodeList
import rtc.Attributes

/**
 * This class encapsulates information to be send or received from the server.
 */
class ChangeRequest {
	
	static final String RDF_RESOURCE="rdf:resource"
	static final String CM_TITLE = "dcterms:title"
	static final String CM_IDENTIFIER = "dcterms:identifier"
	static final String CM_TYPE = "dcterms:type"
	static final String CM_DESCRIPTION = "dcterms:description"
	static final String RTC_EXT_CONTEXT_ID="rtc_ext:contextId"
	static final String RTC_CM_FILED_AGAINST = "rtc_cm:filedAgainst"
	static final String RTC_CM_ATTACHMENT="rtc_cm:com.ibm.team.workitem.linktype.attachment.attachment"
	static final String OSLC_CM_SEVERITY="oslc_cmx:severity"
	
	String uri
	String dcTitle
	String dcIdentifier
	String dcType
	String dcDescription
	String contextId

	String filedAgainst
	String attachmentURL
	String severity

	/**
	 * Creates a ChangeRequest based on the DOM structure
	 * @param uri uri to connection 
	 * @param element element RTC
	 */
	public ChangeRequest(String uri, Element element) {
		this.uri = uri
		this.dcTitle = getChildContent(element, CM_TITLE)
		this.dcIdentifier = getChildContent(element, CM_IDENTIFIER)
		this.dcType = getChildContent(element, CM_TYPE)
		this.dcDescription = getChildContent(element, CM_DESCRIPTION)
		this.contextId = getChildContent(element, RTC_EXT_CONTEXT_ID)
		this.filedAgainst = getChildContent(element, RTC_CM_FILED_AGAINST, RDF_RESOURCE)
		this.severity = getChildContent(element,OSLC_CM_SEVERITY,RDF_RESOURCE)
	}

	/**
	 * Creates a ChangeRequest based on the Attributes class
	 * @param attr Attribute RTC
	 */
	public ChangeRequest(Attributes attr) {
		dcTitle = attr.summary
		dcDescription = attr.description
		dcType = attr.workItemType
		filedAgainst = attr.filedAgaint
		severity = attr.severity
		
	}
	
	/**
	 * Generates the XML representation of the receiver
	 * @param out outuput XML
	 * @throws IOException if invalid throws Input Output Exception
	 */
	public void writeXML(Writer out) throws IOException {
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
		out.append("<rdf:RDF\n")
		out.append("\t\txmlns:rtc_ext=\"http://jazz.net/xmlns/prod/jazz/rtc/ext/1.0/\"\n")
		out.append("\t\txmlns:rtc_cm=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/\"\n")
		out.append("\t\txmlns:oslc_cm=\"http://open-services.net/ns/cm#\"\n")
		out.append("\t\txmlns:dcterms=\"http://purl.org/dc/terms/\"\n")
		out.append("\t\txmlns:oslc_cmx=\"http://open-services.net/ns/cm-x#\"\n")
		out.append("\t\txmlns:oslc=\"http://open-services.net/ns/core#\"\n")
		out.append("\t\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n")
		out.append("\t<oslc_cm:ChangeRequest\n")
		if (uri != null) {
			out.append("\n\t\trdf:about=\"" + uri + "\">\n")
		} else {
			out.append(">\n")
		}
		appendXMLAttribute(out, CM_TITLE, dcTitle)
		appendXMLAttribute(out, CM_IDENTIFIER, dcIdentifier)
		appendXMLAttribute(out, CM_TYPE, dcType)
		appendXMLAttributeParser(out, CM_DESCRIPTION, dcDescription)
		appendXMLAttribute(out, RTC_EXT_CONTEXT_ID, contextId)
		if(attachmentURL!=null) {
			appendXMLAttributeAsResource(out, RTC_CM_ATTACHMENT, attachmentURL)
		}
		appendXMLAttributeAsResource(out, RTC_CM_FILED_AGAINST, filedAgainst)
		appendXMLAttributeAsResource(out,OSLC_CM_SEVERITY, severity)

		out.append("\t</oslc_cm:ChangeRequest>\n")
		out.append("</rdf:RDF>\n")
		out.flush()
	}

	/**
	 * Append Attribute on the XML
	 * @param out output XML
	 * @param id id tag sign XML
	 * @param value tag value XML
	 * @throws IOException if invalid throws Input Output Exception
	 */
	private void appendXMLAttribute(Writer out, String id, String value) throws IOException {
		if(value!=null){
			out.append("\t<" + id + ">" +  value + "</" + id + ">\n")
		}
	}
	
	/**
	 * Append XML Atrribute on the XML
	 * @param out output XML
	 * @param id id tag sign XML
	 * @param value tag value XML
	 * @throws IOException if invalid throws Input Output Exception
	 */
	private void appendXMLAttributeAsResource(Writer out, String id, String value) throws IOException {
		if(value!=null){
			out.append("\t<" + id + " "+RDF_RESOURCE+"=\"" + value + "\"/>\n")
		}
	}
	
	/**
	 * Append XML Atrribute parse on the XML 
	 * @param out output XML
	 * @param id id tag sign XML
	 * @param value tag value XML
	 * @throws IOException if invalid throws Input Output Exception
	 */
	private void appendXMLAttributeParser(Writer out, String id, String value) throws IOException {
		if(value!=null){
			out.append("\t<" + id + " rdf:parseType=\"Literal\">" +  value + "</" + id + ">\n")
		}
	}

	/**
	 * Get Content of the child 
	 * @param element element
	 * @param tagName tag name element 
	 * @return String with text content of child
	 */
	private String getChildContent(Element element, String tagName) {
		NodeList list = element.getElementsByTagName(tagName)
		if (list.getLength() == 0)
			return null
		Element child = (Element) list.item(0)
		return child.getTextContent()
	}

	/**
	 * Get Content of the child
	 * @param element element
	 * @param tagName tag name element 
	 * @param attributeName Attribute name 
	 * @return String Attribute
	 */
	private String getChildContent(Element element, String tagName, String attributeName) {
		NodeList list = element.getElementsByTagName(tagName)
		if (list.getLength() == 0)
			return null
		Element child = (Element) list.item(0)
		return child.getAttribute(attributeName)
	}
}
