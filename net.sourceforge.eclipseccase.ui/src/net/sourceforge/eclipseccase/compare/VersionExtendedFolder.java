package net.sourceforge.eclipseccase.compare;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;


public class VersionExtendedFolder
	extends VersionExtendedContainer
	implements IFolder
{

	private IFolder folder;

	public VersionExtendedFolder(IFolder folder, String version)
	{
		super(folder, version);
		this.folder = folder;
	}

	public int getType()
	{
		return FOLDER;
	}

	public void create(boolean force, boolean local, IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void create(
		int updateFlags,
		boolean local,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void createLink(
		IPath localLocation,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void delete(
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IFile getFile(String name)
	{
		return folder.getFile(name);
	}

	public IFolder getFolder(String name)
	{
		return folder.getFolder(name);
	}

	public void move(
		IPath destination,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}


}
