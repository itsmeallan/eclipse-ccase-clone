package net.sourceforge.eclipseccase.compare;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;


abstract public class VersionExtendedContainer
	extends VersionExtendedResource
	implements IContainer
{
	
	private IContainer container;
	private Collection members;
	
	public VersionExtendedContainer(IContainer folder, String version)
	{
		super(folder, version);
		this.container = folder;
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
		return container.getFile(path);
	}

	public IFolder getFolder(IPath path)
	{
		return container.getFolder(path);
	}


	public boolean exists(IPath path)
	{
		return false;
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
					child = getFolder(new Path(file.getName())); 
				}
				else
				{
//					child = new VersionExtendedFile(getFile(file.getName()), version);
					child = getFile(new Path(file.getName()));
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
