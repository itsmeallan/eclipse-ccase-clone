/**
 * 
 */
package net.sourceforge.eclipseccase.test;

//Since ClearCaseInterface is not an interface but abstract class.
import static org.junit.Assert.*;

import java.util.HashMap;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseInterface;
import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Junit 4 test cases for ClearCaseProvider.
 * 
 * @author mikael petterson
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { ClearCaseInterface.class, ClearCasePlugin.class })
public class ClearCaseProviderTest {

	private static ClearCaseProvider provider;
	private static final String PVOB_TAG_UNIX_ALT1 = "/vobs/pvob1";
	private static final String PVOB_TAG_UNIX_ALT2 = "/vob/pvob1";
	private static final String PVOB_TAG_WIN = "\\pbob1";

	// mock instance
	private ClearCaseInterface cciMock;

	@Before
	public void setUp() {
		cciMock = PowerMock.createMock(ClearCaseInterface.class);
		PowerMock.mockStatic(ClearCasePlugin.class);
		provider = new ClearCaseProvider();

	}

	/**
	 * Test method for
	 * {@link net.sourceforge.eclipseccase.ClearCaseProvider#getPvobTag(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetPvobTagWin() {
		String activitySelector = "activity:act-mpg_llv_infrastructure_toside_install_toside_emagnmo@"
				+ PVOB_TAG_WIN;
		String pvobTag = provider.getPvobTag(activitySelector);
		assertEquals(PVOB_TAG_WIN, pvobTag);
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.eclipseccase.ClearCaseProvider#getPvobTag(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetPvobTagUnixAlt1() {
		String activitySelector = "activity:act-mpg_llv_infrastructure_toside_install_toside_emagnmo@"
				+ PVOB_TAG_UNIX_ALT1;
		String pvobTag = provider.getPvobTag(activitySelector);
		assertEquals(PVOB_TAG_UNIX_ALT1, pvobTag);
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.eclipseccase.ClearCaseProvider#getPvobTag(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetPvobTagUnixAlt2() {
		String activitySelector = "activity:act-mpg_llv_infrastructure_toside_install_toside_emagnmo@"
				+ PVOB_TAG_UNIX_ALT2;
		String pvobTag = provider.getPvobTag(activitySelector);
		assertEquals(PVOB_TAG_UNIX_ALT2, pvobTag);
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.eclipseccase.ClearCaseProvider#getPvobTag(java.lang.String)}
	 * Tests that splitting is done for a line with activities.
	 */
	@Test
	public void testgetActivitySelectorsSuccesful() {
		final String VIEW_NAME = "myview";
		HashMap<Integer, String> args = new HashMap<Integer, String>();
		args.put(Integer.valueOf(ClearCase.FORMAT), "%[activities]CXp");
		args.put(Integer.valueOf(ClearCase.VIEW), VIEW_NAME);
		// Set expectations on mocks.

		EasyMock.expect(ClearCasePlugin.getEngine()).andReturn(cciMock);
		EasyMock
				.expect(
						cciMock.getStream(ClearCase.FORMAT | ClearCase.VIEW,
								args))
				.andStubReturn(
						new String[] { "activity:<activityId>@/vobs/$pvob, activity:<activityId>@/vobs/$pvob" });

		// Only add those not created by PowerMock.
		PowerMock.replayAll(cciMock);

		String[] activitySelectors = provider.getActivitySelectors(VIEW_NAME);
		assertArrayEquals(
				"Array does not contain separate activity in each element",
				new String[] { "activity:<activityId>@/vobs/$pvob",
						"activity:<activityId>@/vobs/$pvob" },
				activitySelectors);

		// Verify behavior for all mock objects.
		PowerMock.verifyAll();
	}
}
