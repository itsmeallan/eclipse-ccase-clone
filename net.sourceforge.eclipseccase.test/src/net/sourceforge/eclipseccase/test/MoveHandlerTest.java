/**
 * 
 */
package net.sourceforge.eclipseccase.test;

import static org.junit.Assert.*;

import net.sourceforge.clearcase.ClearCaseInterface;
import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.MoveHandler;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author mikael petterson
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { IResourceTree.class, IFile.class, IProgressMonitor.class })
public class MoveHandlerTest {
	
	// mock instance
	private IResourceTree resourceTreeMock;
	private IFile sourceMock;
	private IFile destinationMock;
	private IProgressMonitor monitorMock;
	private ClearCaseProvider providerMock;
	private IStatus statusMock;
	//class under test
	private MoveHandler moveHandler;
	
	
	@Before
	public void setUp() {
		resourceTreeMock = PowerMock.createMock(IResourceTree.class);
		sourceMock = PowerMock.createMock(IFile.class,"source");
		destinationMock = PowerMock.createMock(IFile.class,"destination");
		monitorMock = PowerMock.createMock(IProgressMonitor.class);
		providerMock = PowerMock.createMock(ClearCaseProvider.class);
		statusMock = PowerMock.createMock(IStatus.class);
		
		moveHandler = new MoveHandler(providerMock);

	}

	/**
	 * Test method for {@link net.sourceforge.eclipseccase.MoveHandler#deleteFile(org.eclipse.core.resources.team.IResourceTree, org.eclipse.core.resources.IFile, int, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testDeleteFile() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link net.sourceforge.eclipseccase.MoveHandler#deleteFolder(org.eclipse.core.resources.team.IResourceTree, org.eclipse.core.resources.IFolder, int, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testDeleteFolder() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link net.sourceforge.eclipseccase.MoveHandler#deleteProject(org.eclipse.core.resources.team.IResourceTree, org.eclipse.core.resources.IProject, int, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testDeleteProject() {
		fail("Not yet implemented");
	}

	/**
	 * Testing how we can handle an excpetion form the clearcase-java layer.
	 * Test method for {@link net.sourceforge.eclipseccase.MoveHandler#moveFile(org.eclipse.core.resources.team.IResourceTree, org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile, int, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testMoveFile() {
		int updateFlags = 1;
		//No standard move
		EasyMock.expect(providerMock.isIgnored(sourceMock)).andReturn(false);
		EasyMock.expect(sourceMock.isLinked()).andReturn(false);
		EasyMock.expect(providerMock.isClearCaseElement(sourceMock)).andReturn(true);
		
		EasyMock.expect(sourceMock.getName()).andReturn("myFile");
		
		//IStatus status = validateDest(destination,
		//		new SubProgressMonitor(monitor, 40));
		EasyMock.expect(resourceTreeMock.isSynchronized(sourceMock,IResource.DEPTH_INFINITE)).andReturn(true);
		EasyMock.expect(statusMock.getCode()).andReturn(IStatus.OK);
		
		
		
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link net.sourceforge.eclipseccase.MoveHandler#moveFolder(org.eclipse.core.resources.team.IResourceTree, org.eclipse.core.resources.IFolder, org.eclipse.core.resources.IFolder, int, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testMoveFolder() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link net.sourceforge.eclipseccase.MoveHandler#moveProject(org.eclipse.core.resources.team.IResourceTree, org.eclipse.core.resources.IProject, org.eclipse.core.resources.IProjectDescription, int, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testMoveProject() {
		fail("Not yet implemented");
	}

}
