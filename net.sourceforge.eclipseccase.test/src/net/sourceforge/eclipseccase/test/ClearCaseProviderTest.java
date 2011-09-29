/**
 * 
 */
package net.sourceforge.eclipseccase.test;

import static org.junit.Assert.*;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author mike
 * 
 */
public class ClearCaseProviderTest {

	private static ClearCaseProvider provider;
	private static final String PVOB_TAG = "pvob1";

	@BeforeClass
	public static void runBeforeClass() {
		provider = new ClearCaseProvider();
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.eclipseccase.ClearCaseProvider#getPvobTag(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetPvobTagWin() {
		String activitySelector = "activity:act-mpg_llv_infrastructure_toside_install_toside_emagnmo@\\"
				+ PVOB_TAG;
		String pvobTag = provider.getPvobTag(activitySelector);
		assertEquals(PVOB_TAG, pvobTag);
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.eclipseccase.ClearCaseProvider#getPvobTag(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetPvobTagUnixAlt1() {
		String activitySelector = "activity:act-mpg_llv_infrastructure_toside_install_toside_emagnmo@/vobs/"
				+ PVOB_TAG;
		String pvobTag = provider.getPvobTag(activitySelector);
		assertEquals(PVOB_TAG, pvobTag);
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.eclipseccase.ClearCaseProvider#getPvobTag(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetPvobTagUnixAlt2() {
		String activitySelector = "activity:act-mpg_llv_infrastructure_toside_install_toside_emagnmo@/vob/"
				+ PVOB_TAG;
		String pvobTag = provider.getPvobTag(activitySelector);
		assertEquals(PVOB_TAG, pvobTag);
	}

}
