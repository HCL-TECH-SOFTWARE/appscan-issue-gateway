package rtc
/**
 * © Copyright IBM Corporation 2018.
 * © Copyright PrimeUP Solucoes em TI LTDA 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

import javax.xml.XMLConstants
import javax.xml.namespace.NamespaceContext

/**
 * This class encapsulates information about rdf namespace
 */
class NamespaceContextMap implements NamespaceContext {
	
	HashMap<String, String> prefixMap

	/**
	 * Create a Namespace Context Map
	 */
	NamespaceContextMap() {
		super()
		this.prefixMap = new HashMap<String, String>()
	}
	
	/**
	 * Create a Namespace Context Map from string array
	 * @param args String list
	 */
	NamespaceContextMap(String[] args) {
		this()
		if (args != null) setMap(args)
	}
	
	/**
	 * Set Prefix Map 
	 * @param args String list keys, values
	 */
	void setMap(String[] args) {
		int len = args.length
		int i = 0
		while (i+1 < len) {
			prefixMap.put(args[i++], args[i++])
		}
	}
	
	/**
	 * Get namespace URI
	 * @param prefix key to get URI
	 */
	String getNamespaceURI(String prefix) {
		if (prefix == null)
			throw new NullPointerException("Null prefix")
		String uri = prefixMap.get(prefix)
		return (uri != null)?uri:XMLConstants.NULL_NS_URI
	}

	/**
	 * Method unused
	 */
	String getPrefix(String namespaceURI) {
		// Unused
		return null
	}

	/**
	 * Method unused
	 */
	Iterator getPrefixes(String namespaceURI) {
		// Unused
		return null
	}
}
