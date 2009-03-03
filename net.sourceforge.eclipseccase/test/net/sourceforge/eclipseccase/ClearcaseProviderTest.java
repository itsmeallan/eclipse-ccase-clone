package net.sourceforge.eclipseccase;

import static org.junit.Assert.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test class contains tests for the clearcase provider
 * class only.
 * @author mike
 *
 */
@RunWith(JMock.class)
public class ClearcaseProviderTest {

	Mockery context = new JUnit4Mockery();

	@Test
	public void testIsSnapShotWithCache() {
		// set up
		ClearcaseProvider provider = new ClearcaseProvider();
		View view = new View("myProject", "snapshot");
		provider.setView("myProject", view);
		final IResource mockedResource = context.mock(IResource.class);
		final IProject mockedProject = context.mock(IProject.class);
		// expectations
		context.checking(new Expectations() {
			{
				oneOf(mockedResource).getProject();
				will(returnValue(mockedProject));
				oneOf(mockedProject).getName();
				will(returnValue("myProject"));
			}
		});
		// execute
		boolean isSnapshot = provider.isSnapShot(mockedResource);

		// check result
		assertEquals("Expected resource to be in a snapshotview", true,
				isSnapshot);
	}

	@Test
	public void testIsSnapShotFalseWithCache() {
		// set up
		ClearcaseProvider provider = new ClearcaseProvider();
		View view = new View("myProject", "dynamic");
		provider.setView("myProject", view);
		final IResource mockedResource = context.mock(IResource.class);
		final IProject mockedProject = context.mock(IProject.class);
		// expectations
		context.checking(new Expectations() {
			{
				oneOf(mockedResource).getProject();
				will(returnValue(mockedProject));
				oneOf(mockedProject).getName();
				will(returnValue("myProject"));
			}
		});
		// execute
		boolean isSnapshot = provider.isSnapShot(mockedResource);

		// check result
		assertEquals("Expected resource to be in a dynamic view", false,
				isSnapshot);

	}

	// FIXME: To test this I need to mock calls to the public
	// static method ClearcasePlugin.getEngine(). This is not supported
	// by jmock.
	public void testIsSnapShotTrueNoCache() {
		ClearcaseProvider provider = new ClearcaseProvider();
		final IResource mockedResource = context.mock(IResource.class);
		final IProject mockedProject = context.mock(IProject.class);
		final ClearcasePlugin mockedClearcasePlugin = context
				.mock(ClearcasePlugin.class);
		// expectations
		context.checking(new Expectations() {
			{
				oneOf(mockedResource).getProject();
				will(returnValue(mockedProject));
				oneOf(mockedProject).getName();
				will(returnValue("myProject"));

			}
		});

		// execute
		boolean isSnapshot = provider.isSnapShot(mockedResource);

		// check result
		assertEquals("Expected resource to be in a snapshotview", true,
				isSnapshot);

	}

}
