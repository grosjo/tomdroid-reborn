package org.tomdroid.reborn;

public class XmlUtils {
	
	public static String removeIllegal(String input) {
		
		String invalidXMLChars = "[^[\\u0009\\u000A\\u000D][\\u0020-\\uD7FF][\\uE000-\\uFFFD][\\u10000-\\u10FFFF]]";
		
		return input
				.replaceAll(invalidXMLChars, "�");
	}
	
	/**
	 * Useful to replace the characters forbidden in xml by their escaped counterparts
	 * Ex: &amp; -> &amp;amp;
	 * 
	 * @param input the string to escape
	 * @return the escaped string
	 */
	public static String escape(String input) {
		
		return input
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("\'", "&apos;");
	}
	
	/**
	 * Useful to replace the escaped characters their unescaped counterparts
	 * Ex: &amp;amp; -> &amp;
	 * 
	 * @param input the string to unescape
	 * @return the unescaped string
	 */
	public static String unescape(String input) {
		return input
			.replace("&amp;", "&")
			.replace("&lt;", "<")
			.replace("&gt;", ">")
			.replace("&quot;", "\"")
			.replace("&apos;", "\'");
	}
}
