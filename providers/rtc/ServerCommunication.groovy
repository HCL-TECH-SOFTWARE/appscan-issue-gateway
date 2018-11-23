package rtc
/**
 * © Copyright IBM Corporation 2018.
 * © Copyright PrimeUP Solucoes em TI LTDA 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

import java.util.AbstractMap.SimpleEntry

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import common.RESTUtils
import rtc.RTCConstants as Constants

/**
 * This class deals with communication between RTCProvider and RTC server. 
 */
class ServerCommunication {
	
	static final String PREFIX_BOUNDARY_STRING = "AppScanBoundary_"
	
	static final String METHOD_POST = "post"
	static final String METHOD_PUT = "put"
	
	static final String STATUS_LOGGED_IN = "loggetIn"
	static final String STATUS_BASIC_LOGIN_REQUIRED = "basicLoginRequired"
	static final String STATUS_FORM_LOGIN_REQUIRED = "formLoginRequeired"
	
	CookieManager cookieManager = new CookieManager()
	
	/**
	 * Constructor ServerCommunication
	 */
	ServerCommunication () {
		
	}
	
	/**
	 * Setting connection Server Comunicaton
	 */
	void setUpConnection() {
		CookieHandler.setDefault(cookieManager)
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
		
		def nullTrustManager = [
			checkClientTrusted: { chain, authType ->  },
			checkServerTrusted: { chain, authType ->  },
			getAcceptedIssuers: { null }
		]

		def nullHostnameVerifier = [
			verify: { hostname, session -> true }
		]

		SSLContext sslContext = SSLContext.getInstance("SSL")
		sslContext.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null)
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory())
		HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)
	}

	/**
	 * Method responsible for send request get for secure document
	 * @param serverURL URL Server
	 * @param URL to prepare connection
	 * @param login username RTC
	 * @param password password RTC
	 * @param authString Authnetication String
	 * @return HttpURLConnection requeriment URL to connection
	 * @throws IOException input output Exception
	 * @throws Exception Generic Exception
	 */
	HttpURLConnection sendGetForSecureDocument(String serverURL, String url, String login, String password, String authString=null)	throws IOException, Exception {		
		
		HttpURLConnection urlConnection = prepareGetConnection(url,authString)
		urlConnection.connect()
		switch (getLoginStatus(urlConnection)) {
			case STATUS_LOGGED_IN:
				return urlConnection
			case STATUS_BASIC_LOGIN_REQUIRED:
				return sendGetForSecureDocument(serverURL, url, login, password,getAuthString(login, password))

			default:
				doFormLogin(serverURL,login, password)
				return sendGetForSecureDocument(serverURL, url, login, password,authString)
		}
	}
	
	/**
	 * Method responsible for send request secure document
	 * @param serverURL URL Server
	 * @param url URL to prepare connection
	 * @param login username RTC
	 * @param password password RTC
	 * @param data data of request
	 * @param type type request
	 * @param authString Authentication string
	 * @return HttpURLConnection requeriment URL to connection
	 * @throws IOException input output Exception
	 * @throws Exception Generic Exception
	 */
	HttpURLConnection sendSecureDocument(String serverURL, String url, String login, String password, String data, String type, String authString=null) throws IOException, Exception {
		if (!METHOD_POST.equals(type) && !METHOD_PUT.equals(type)) {
			type = METHOD_POST;
		}
		HttpURLConnection urlConnection = prepareConnection(url, data, authString, type)
		urlConnection.connect()
		
		switch (getLoginStatus(urlConnection)) {
			case STATUS_LOGGED_IN:
				return urlConnection
			case STATUS_BASIC_LOGIN_REQUIRED:
				return sendSecureDocument(serverURL, url, login, password, data, getAuthString(login, password), type)
			default:
				doFormLogin(serverURL,login, password)
				return sendSecureDocument(serverURL, url, login, password, data, authString, type)
		}
	}
	
	/**
	 * Method responsible for send stream file upload document
	 * @param serverURL URL Server
	 * @param url URL to prepare connection
	 * @param login username RTC
	 * @param password password RTC
	 * @param file file 
	 * @param fileName file name
	 * @param authString authentication string
	 * @return HttpURLConnection requeriment URL to connection
	 * @throws IOException input output Exception
	 * @throws Exception Generic Exception
	 */
	HttpURLConnection sendOctetStreamFileUploadDocument(String serverURL, String url, String login, String password, File file, String fileName, String authString=null)
	throws IOException, Exception {
		HttpURLConnection urlConnection = prepareOctetStreamFileUploadConnection(url, file, fileName, authString)
		urlConnection.connect()
		switch (getLoginStatus(urlConnection)) {
			case STATUS_LOGGED_IN:
				return urlConnection
			case STATUS_BASIC_LOGIN_REQUIRED:
				return prepareOctetStreamFileUploadConnection(url, file, fileName, getAuthString(login, password))
			default:
				doFormLogin(serverURL,login, password)
				return prepareOctetStreamFileUploadConnection(url, file, fileName, authString)
		}
	}

	/**
	 * Method resposible to get login status
	 * @param urlConnection URL to connection
	 * @return String status login
	 */
	private String getLoginStatus(HttpURLConnection urlConnection) {
		String authrequired = urlConnection.getHeaderField(Constants.AUTHREQUIRED_HEADER)
		String basicHeader = urlConnection.getHeaderField(Constants.BASIC_HEADER)

		boolean mustFormLogin = urlConnection.responseCode == 200 && (authrequired!=null) && (Constants.AUTHREQUIRED_VALUE.compareTo(authrequired) == 0)
		boolean mustBasicLogin = urlConnection.responseCode == 401 && (basicHeader!=null)
		if(!mustFormLogin && !mustBasicLogin){
			return STATUS_LOGGED_IN
		}
		else {
			urlConnection.disconnect()
			if(mustFormLogin) {
				return STATUS_FORM_LOGIN_REQUIRED
			}
			else {
				return STATUS_BASIC_LOGIN_REQUIRED
			}
		}
	}
	
	/**
	 * Method responsible to create a form login
	 * @param serverURL URL Server
	 * @param login usename RTC
	 * @param password pasword RTC
	 * @throws Exception Generic Exception
	 */
	private void doFormLogin(String serverURL, String login, String password) throws Exception {
		URL authurl = new URL(serverURL + "" + Constants.JSECURITYCHECK)
		HttpURLConnection urlConnection = authurl.openConnection()

		List<SimpleEntry> nvps = new ArrayList<SimpleEntry>()
		nvps.add(new SimpleEntry("j_username", login))
		nvps.add(new SimpleEntry("j_password", password))
		urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
		urlConnection.setRequestMethod("POST")
		urlConnection.doOutput = true
		urlConnection.doInput = true
		urlConnection.outputStream.withWriter{
			it.write(getQuery(nvps))
			it.flush()
		}
		urlConnection.connect()
		String authrequired = urlConnection.getHeaderField(Constants.AUTHREQUIRED_HEADER)
		urlConnection.disconnect()
		if ((authrequired != null) && (Constants.AUTHFAILED_VALUE.compareTo(authrequired) == 0)) {
			// The login failed
			throw new Exception("Authentication failed")
		}
	}
	
	/**
	 * Method responsible for prepare GetConnection
	 * @param url URL to open connetion
	 * @param authString authentication string
	 * @return HttpURLConnection requeriment URL to connection
	 */
	private HttpURLConnection prepareGetConnection(String url, String authString) {
		HttpURLConnection urlConnection = new URL(url).openConnection()
		urlConnection.addRequestProperty(Constants.ACCEPT_HEADER, Constants.APPLICATION_RDF_XML_TYPE)
		urlConnection.addRequestProperty(Constants.OSLC_CORE_VERSION_HEADER, Constants.OSLC_VERSION)
		setCookies(urlConnection)

		if(authString != null) {
			urlConnection.addRequestProperty(Constants.AUTHORIZATION_HEADER, authString)
		}
		urlConnection.setRequestMethod("GET")
		urlConnection.doOutput = true

		return urlConnection
	}
	
	/**
	 * Method responsible for prepare Connection
	 * @param url URL to prepare connection
	 * @param data data of request 
	 * @param authString authentication string
	 * @param type type request 
	 * @return HttpURLConnection requeriment URL to connection
	 */
	private HttpURLConnection prepareConnection(String url, String data, String authString, String type) {
		HttpURLConnection urlConnection = new URL(url).openConnection()
		urlConnection.addRequestProperty(Constants.ACCEPT_HEADER, Constants.APPLICATION_RDF_XML_TYPE)
		urlConnection.addRequestProperty(Constants.CONTENT_TYPE_HEADER, Constants.APPLICATION_RDF_XML_TYPE)
		urlConnection.addRequestProperty(Constants.OSLC_CORE_VERSION_HEADER, Constants.OSLC_VERSION)
		setCookies(urlConnection)

		if(authString != null) {
			urlConnection.addRequestProperty(Constants.AUTHORIZATION_HEADER, authString)
		}
		
		if(METHOD_POST.equals(type)) {
			urlConnection.setRequestMethod("POST")		
		}
		if(METHOD_PUT.equals(type)) {
			urlConnection.setRequestMethod("PUT")
		}
		urlConnection.doOutput = true
		urlConnection.doInput = true
		
		urlConnection.outputStream.write(data.getBytes("UTF-8"))
		return urlConnection
	}

	/**
	 * Method responsible for prepare stream file upload connection
	 * @param url URL to open connection
	 * @param file file 
	 * @param fileName file name 
	 * @param authString authetication string 
	 * @return HttpURLConnection requeriment URL to connection
	 */
	private HttpURLConnection prepareOctetStreamFileUploadConnection(String url, File file, String fileName, String authString) {
		HttpURLConnection urlConnection = new URL(url).openConnection()
		
		String boundaryString = generateBoundaryString();
		
		String thePart = RESTUtils.generatePart(file, fileName, boundaryString)
		setCookies(urlConnection)
		if(authString != null) {
			urlConnection.addRequestProperty(Constants.AUTHORIZATION_HEADER, authString)
		}

		urlConnection.addRequestProperty("Accept", "application/text")
		urlConnection.addRequestProperty("Content-Type", 'multipart/form-data; boundary=' + boundaryString)

		urlConnection.setRequestMethod("POST")
		urlConnection.doOutput = true
		urlConnection.doInput = true

		urlConnection.outputStream.withWriter{
			it.write(thePart)
			it.flush()
		}
		return urlConnection
	}
	
	/**
	 * Method generate the boundary string for multipart/form-data request
	 * @return String generated boundary
	 */
	private String generateBoundaryString() {
		return PREFIX_BOUNDARY_STRING + UUID.randomUUID().toString();
	}
	
	private void setCookies(HttpURLConnection connection) {
		if (cookieManager.getCookieStore().getCookies().size() > 0) {
			List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies()

			if (cookies != null) {
				for (HttpCookie cookie : cookies) {
					connection.addRequestProperty("Cookie", cookie.getName() + "=" + cookie.getValue())
				}
			}
		}
	}
	
	/**
	 * Method responsible for get Authentication String
	 * @param username username RTC
	 * @param password password RTC
	 * @return String Authentication
	 */
	private String getAuthString(String username, String password){
		String authString = username + ":" + password
		byte[] authEncBytes = Base64.encoder.encode(authString.getBytes())
		String authStringEnc = new String(authEncBytes)
		return "Basic " + authStringEnc
	}
	
	/**
	 * Method responsible for get query
	 * @param params maps with params query
	 * @return String query
	 * @throws UnsupportedEncodingException unsupported encoding exception
	 */
	private String getQuery(List<SimpleEntry> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder()
		boolean first = true

		for (SimpleEntry pair : params)	{
			if (first)
				first = false
			else
				result.append("&")

			result.append(URLEncoder.encode(pair.getKey(), "UTF-8"))
			result.append("=")
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"))
		}

		return result.toString()
	}

}

