package net.sourceforge.eclipseccase.ui.test;



import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import net.sourceforge.eclipseccase.ui.preferences.DiffMergePreferencePage;

import org.junit.Test;

public class DiffMergePreferencesTest {
	
	private static final String KDIFF3 = "kdiff3";
	private static final String KDIFF3_PATH = "/my/path/to/kdiff3";
	
	private static final String CLEARCASE = "clearcase";
	private static final String CLEARCASE_PATH = "/usr/atria/bin/cleartool";

	
	
	@Test
	public void testStrToMap() {
		String preferenceValue = KDIFF3+":"+KDIFF3_PATH+";"+CLEARCASE+":"+CLEARCASE_PATH+";";
		Map<String,String> map = DiffMergePreferencePage.strToMap(preferenceValue);
		Assert.assertFalse("Map shouldn't have been empty",
	                       map.isEmpty());
		Assert.assertEquals(KDIFF3_PATH, map.get(KDIFF3));
	}
	
	@Test
	public void testMapToStr(){
		String expected = KDIFF3+":"+KDIFF3_PATH+";"+CLEARCASE+":"+CLEARCASE_PATH+";";
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put(KDIFF3,KDIFF3_PATH);
		map.put(CLEARCASE,CLEARCASE_PATH);
		String actual = DiffMergePreferencePage.mapToStr(map);
		Assert.assertEquals(expected, actual);
		
	}

}
