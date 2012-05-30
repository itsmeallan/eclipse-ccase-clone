package net.sourceforge.eclipseccase.ui.test;

import static org.junit.Assert.*;
import junit.framework.Assert;

import net.sourceforge.eclipseccase.ui.preferences.DiffMergePreferencePage;

import org.junit.Test;

public class DiffMergePreferencesTest {
	
	private static final String KDIFF3 = "kdiff3";
	private static final String KDIFF3_PATH = "/my/path/to/kdiff3";
	
	private static final String CLEARCASE = "clearcase";
	private static final String CLEARCASE_PATH = "/usr/atria/bin/cleartool";

	private static final String [][] ref = new String [][]{{KDIFF3,KDIFF3_PATH},{CLEARCASE,CLEARCASE_PATH}};
	
	@Test
	public void testFromStringToArray() {
		String preferenceValue = "kdiff3:/my/path/to/kdiff3;clearcase:/usr/atria/bin/cleartool";
		DiffMergePreferencePage diffPref = new DiffMergePreferencePage();
		String [][] array = diffPref.fromStringToArray(preferenceValue);
		for (int i=0; i < array.length; i++){
			   
			for (int j=0; j < array[i].length ; j++){
			      Assert.assertEquals(ref[i][j], array[i][j]);
			   }
		}
		
		
		
	}

}
