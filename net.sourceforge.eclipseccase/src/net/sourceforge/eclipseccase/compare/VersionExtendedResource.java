package net.sourceforge.eclipseccase.compare;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;


public abstract class VersionExtendedResource implements IResource
{
	private static final String VERSION_EXTENDED_DELIM = "@@";
	private IResource resource;
	private String version;
	private String versionExtendedPath;
	
	public VersionExtendedResource(IResource resource, String version)
	{
		super();
		this.resource = resource;
		this.version = version;
		this.versionExtendedPath = resource.getLocation().toOSString() + VERSION_EXTENDED_DELIM + version;
	}

	protected String getVersionExtendedPath()
	{
		return versionExtendedPath;
	}


	public void accept(IResourceProxyVisitor visitor, int memberFlags)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void accept(IResourceVisitor visitor) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void accept(
		IResourceVisitor visitor,
		int depth,
		boolean includePhantoms)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void clearHistory(IProgressMonitor monitor) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void copy(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void copy(
		IPath destination,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void copy(
		IProjectDescription description,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void copy(
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IMarker createMarker(String type) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void delete(boolean force, IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void delete(int updateFlags, IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void deleteMarkers(String type, boolean includeSubtypes, int depth)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public boolean exists()
	{
		return false;
	}

	public IMarker findMarker(long id) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IMarker[] findMarkers(
		String type,
		boolean includeSubtypes,
		int depth)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public String getFileExtension()
	{
		return resource.getFileExtension();
	}

	public IPath getFullPath()
	{
		return resource.getFullPath();
	}

	public IPath getLocation()
	{
		return null;
	}

	public IMarker getMarker(long id)
	{
		throw new UnsupportedOperationException();
	}

	public long getModificationStamp()
	{
		return NULL_STAMP;
	}

	public String getName()
	{
		return resource.getName() + VERSION_EXTENDED_DELIM + version;
	}

	public IContainer getParent()
	{
		throw new UnsupportedOperationException();
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IProject getProject()
	{
		return resource.getProject();
	}

	public IPath getProjectRelativePath()
	{
		throw new UnsupportedOperationException();
	}

	public IPath getRawLocation()
	{
		throw new UnsupportedOperationException();
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IWorkspace getWorkspace()
	{
		return resource.getWorkspace();
	}

	public boolean isAccessible()
	{
		return false;
	}

	public boolean isDerived()
	{
		return false;
	}

	public boolean isLocal(int depth)
	{
		return false;
	}

	public boolean isLinked()
	{
		return false;
	}

	public boolean isPhantom()
	{
		return false;
	}

	public boolean isReadOnly()
	{
		return true;
	}

	public boolean isSynchronized(int depth)
	{
		return false;
	}

	public boolean isTeamPrivateMember()
	{
		return false;
	}

	public void move(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void move(
		IPath destination,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void move(
		IProjectDescription description,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void move(
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void refreshLocal(int depth, IProgressMonitor monitor)
		throws CoreException
	{
	}

	public void setDerived(boolean isDerived) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setPersistentProperty(QualifiedName key, String value)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setReadOnly(boolean readOnly)
	{
		throw new UnsupportedOperationException();
	}

	public void setSessionProperty(QualifiedName key, Object value)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setTeamPrivateMember(boolean isTeamPrivate)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void touch(IProgressMonitor monitor) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public Object getAdapter(Class adapter)
	{
		return null;
	}

}
