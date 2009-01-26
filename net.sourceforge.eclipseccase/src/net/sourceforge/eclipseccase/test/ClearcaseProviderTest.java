package net.sourceforge.eclipseccase.test;


import java.awt.List;
import java.util.ArrayList;

import net.sourceforge.eclipseccase.ClearcaseProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JMock.class)
public class ClearcaseProviderTest {
	
	private static final int DEPTH = 0;
	 Mockery context = new JUnit4Mockery();
	
	@Before
	public void setUp() throws Exception {
		
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCheckin(){
		
		final IResource resourceAMock = context.mock(IResource.class, "ResourceA");
		final IResource resourceBMock = context.mock(IResource.class, "ResourceB");
		
		
		final IProgressMonitor progressMonitorMock = context.mock(IProgressMonitor.class);
		
		ClearcaseProvider provider = new ClearcaseProvider();
		//provider.checkin(resourceMock, DEPTH, progressMonitorMock)
		

		
	}

}
