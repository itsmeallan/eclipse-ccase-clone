package net.sourceforge.eclipseccase.compare;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;


public class VersionExtendedProject
	extends VersionExtendedContainer
	implements IProject
{
	
	private IProject project;

	public VersionExtendedProject(IProject project, String version)
	{
		super(project, version);
		this.project = project;
	}

	public void build(
		int kind,
		String builderName,
		Map args,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void build(int kind, IProgressMonitor monitor) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void close(IProgressMonitor monitor) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void create(
		IProjectDescription description,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void create(IProgressMonitor monitor) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void delete(
		boolean deleteContent,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IProjectDescription getDescription() throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IFile getFile(String name)
	{
		return project.getFile(name);
	}

	public IFolder getFolder(String name)
	{
		return project.getFolder(name);
	}

	public IProjectNature getNature(String natureId) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IPath getPluginWorkingLocation(IPluginDescriptor plugin)
	{
		throw new UnsupportedOperationException();
	}

	public IProject[] getReferencedProjects() throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public IProject[] getReferencingProjects()
	{
		throw new UnsupportedOperationException();
	}

	public boolean hasNature(String natureId) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isNatureEnabled(String natureId) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isOpen()
	{
		throw new UnsupportedOperationException();
	}

	public void move(
		IProjectDescription description,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void open(IProgressMonitor monitor) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setDescription(
		IProjectDescription description,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setDescription(
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public int getType()
	{
		return PROJECT;
	}

}
