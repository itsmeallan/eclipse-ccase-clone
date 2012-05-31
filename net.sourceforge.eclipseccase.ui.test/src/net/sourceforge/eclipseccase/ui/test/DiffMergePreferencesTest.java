package net.sourceforge.eclipseccase.ui.test;

import static org.junit.Assert.*;

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
	public void testFromStringToArray() {
		String preferenceValue = "kdiff3:/my/path/to/kdiff3;clearcase:/usr/atria/bin/cleartool";
		DiffMergePreferencePage diffPref = new DiffMergePreferencePage();
		//Map<String,String> map = diffPref.fromStringToMap(preferenceValue);
		//Assert.assertFalse("Map shouldn't have been empty",
	    //                       map.isEmpty());
		
		
		
		
	}

}
