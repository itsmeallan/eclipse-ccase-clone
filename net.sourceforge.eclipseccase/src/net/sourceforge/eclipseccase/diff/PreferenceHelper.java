package net.sourceforge.eclipseccase.diff;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Helps to convert preference data that don't use the regular format.
 * 
 * @author mikael petterson 
 *
 */
public class PreferenceHelper {
	
	public static final String TOOL_DELIMITER = ";";

	public static final String PATH_DELIMITER = ":";
	
	public static final String EMPTY_STR = "";
	
	public static Map<String, String> strToMap(String value) {

		Map<String, String> map = new HashMap<String, String>();

		if (value.isEmpty())
			return map;
		// decode str to map. tool1:path1;tool2:path2; and tool1:;tool2:path2
		// split to too1:path1
		StringTokenizer tokenizer = new StringTokenizer(value, TOOL_DELIMITER);
		String[] nameValuePair = new String[tokenizer.countTokens()];
		for (int i = 0; i < nameValuePair.length; i++) {
			nameValuePair[i] = tokenizer.nextToken();
		}

		// now split name value into map for each element in string.
		for (int i = 0; i < nameValuePair.length; i++) {
			String[] nameValue = nameValuePair[i].split(PATH_DELIMITER);
			// handle if we have no value for tool to avoid nullpointer.
			if (nameValue.length == 2) {
				map.put(nameValue[0], nameValue[1]);
			} else if (nameValue.length == 1) {
				map.put(nameValue[0], EMPTY_STR);
			}
		}

		// map with toolname and matching execPath.

		return map;
	}
	
	
	public static String mapToStr(Map<String, String> map) {

		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			// System.out.println("Key = " + entry.getKey() + ", Value = " +
			// entry.getValue());
			sb.append(entry.getKey());
			sb.append(PATH_DELIMITER);
			sb.append(entry.getValue());
			sb.append(TOOL_DELIMITER);
		}
		// sb containing tool1:path1;tool2:path2;

		return sb.toString();
	}
	
	public static String getExecPath(String selectedTool,Map<String,String> toolsPathMap) {
		String result = EMPTY_STR;

		if (toolsPathMap.containsKey(selectedTool)) {
			result = toolsPathMap.get(selectedTool);
		}

		return result;
	}

}
