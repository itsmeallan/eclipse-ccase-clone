package net.sourceforge.eclipseccase.compare;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;


public class VersionExtendedFolder
	extends VersionExtendedResource
	implements IFolder
{

	private IFolder folder;
	private Collection members;
	
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

	public boolean exists(IPath path)
	{
		return false;
	}

	public IResource findMember(String name)
	{
		throw new UnsupportedOperationException();
	}

	public IResource findMember(String name, boolean includePhantoms)
	{
		throw new UnsupportedOperationException();
	}

	public IResource findMember(IPath path)
	{
		throw new UnsupportedOperationException();
	}

	public IResource findMember(IPath path, boolean includePhantoms)
	{
		throw new UnsupportedOperationException();
	}

	public IFile getFile(IPath path)
	{
		return folder.getFile(path);
	}

	public IFolder getFolder(IPath path)
	{
		return folder.getFolder(path);
	}

	public IResource[] members() throws CoreException
	{
		return members(0);
	}

	public IResource[] members(boolean includePhantoms) throws CoreException
	{
		return members(0);
	}

	public synchronized IResource[] members(int memberFlags) throws CoreException
	{
		if (members == null)
		{
			members = new LinkedList();
			File dir = new File(getVersionExtendedPath());
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				File file = files[i];
				boolean isDirectory =
					ClearcasePlugin
						.getEngine()
						.cleartool("describe \"" + file.getPath() + "\"")
						.message.indexOf("type: directory") != -1;
				IResource child = null;
				if (isDirectory)
				{
//					child = new VersionExtendedFolder(getFolder(file.getName()), version);
					child = getFolder(file.getName()); 
				}
				else
				{
//					child = new VersionExtendedFile(getFile(file.getName()), version);
					child = getFile(file.getName());
				}
				members.add(child);
			}
		}
		return (IResource[]) members.toArray(new IResource[members.size()]);
	}

	public IFile[] findDeletedMembersWithHistory(
		int depth,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

}
