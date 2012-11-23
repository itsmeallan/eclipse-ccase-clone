package net.sourceforge.eclipseccase;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class Views {
	
	private static Map<String, String> viewLookupTable = new Hashtable<String, String>(
			200);
	
	
	public static String getViewName(final String path) {
		String res = viewLookupTable.get(path);
		if (res == null) {
			res = ClearCasePlugin.getEngine().getViewName(path);
			viewLookupTable.put(path, res);
		}
		return res;
	}

	public static String[] getUsedViewNames() {
		Set<String> views = new HashSet<String>();
		for (String v : viewLookupTable.values()) {
			views.add(v);
		}
		return views.toArray(new String[views.size()]);
	}

	public static Map<String, String> getViewLookupTable() {
		return viewLookupTable;
	}

	public static void setViewLookupTable(Map<String, String> viewLookupTable) {
		Views.viewLookupTable = viewLookupTable;
	}

}
