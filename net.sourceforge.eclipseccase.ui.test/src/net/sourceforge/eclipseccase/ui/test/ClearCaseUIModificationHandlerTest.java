package net.sourceforge.eclipseccase.ui.test;

import static org.junit.Assert.*;

import java.util.HashMap;


import net.sourceforge.clearcase.ClearCaseInterface;
import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.ClearCasePreferences;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ClearCasePreferences.class })
public class ClearCaseUIModificationHandlerTest {
	
	// mock instance
	private ClearCasePreferences ccpMock;
	
	//class under test
	

	@Test
	public final void testValidateEditIFileArrayFileModificationValidationContext() {
		fail("Not yet implemented");
	}

	@Test
	public void testValidateSaveIFile() {
		ccpMock = PowerMock.createMock(ClearCasePreferences.class);
		
		//uiModHandler = new ClearCaseUIModificationHandler();
		
		

		EasyMock.expect(ccpMock.isCheckoutAutoNever()).andReturn(false);
		
		// Verify behavior for all mock objects.
		PowerMock.verifyAll();
	}

}
