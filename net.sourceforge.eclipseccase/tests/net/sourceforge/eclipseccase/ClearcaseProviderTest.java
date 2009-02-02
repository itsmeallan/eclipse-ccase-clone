/**
 * 
 */
package net.sourceforge.eclipseccase;

import net.sourceforge.eclipseccase.ClearcaseProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.ReopenEditorMenu;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for the ClearcaseProvider. ClearcaseProvider contains
 * funtions for check-in, check-out ... 
 * @author eraonel
 * 
 */
@RunWith(JMock.class)
public class ClearcaseProviderTest {

	Mockery context = new JUnit4Mockery();

	@Test
	public void testCheckinSuccess() {

		// Setup
		final IResource resourceMock = context.mock(IResource.class,
				"resource");
		ClearcaseProvider provider = new ClearcaseProvider();
		NullProgressMonitor progressMonitor = new NullProgressMonitor();

		// Expectations
		context.checking(new Expectations() {
			{
				one(resourceMock).getFullPath();
				
				
			}
		});

		provider.CHECK_IN.visit(resourceMock, progressMonitor);

		// Assert

	}

}
