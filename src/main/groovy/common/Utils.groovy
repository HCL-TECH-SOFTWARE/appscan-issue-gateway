package common

class Utils {

	//For now just remove any double quotes.  Causes problems
	//Replace all backslashes with double backslashes to handle windows paths in Location
	public static String escape(String theString) {
		if (theString != null) {
			theString = theString.replaceAll("\"", "'")
			theString = theString.replaceAll("\\\\", "\\\\\\\\")
		} else {
			theString = ""
		}
		theString
	}

}