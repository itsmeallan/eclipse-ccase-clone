package net.sourceforge.eclipseccase.compare;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class VersionExtendedFile extends VersionExtendedResource implements IFile
{
	private IFile file;
	
	public VersionExtendedFile(IFile file, String version)
	{
		super(file, version);
		this.file = file;
	}
	

	public int getType()
	{
		return FILE;
	}

	public InputStream getContents() throws CoreException
	{
		InputStream contents = null;
		try
		{
			contents = new FileInputStream(getVersionExtendedPath());
		}
		catch (FileNotFoundException e)
		{
			ClearcasePlugin.log(IStatus.ERROR, "Could not open file: " + getVersionExtendedPath(), e);
		}
		return contents;
	}

	public InputStream getContents(boolean force) throws CoreException
	{
		return getContents();
	}
	
	public void appendContents(
		InputStream source,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void appendContents(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void create(
		InputStream source,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException("Cannot save a clearcase version extended file");
	}

	public void create(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException("Cannot save a clearcase version extended file");
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

	public int getEncoding() throws CoreException
	{
		return file.getEncoding();
	}

	public IFileState[] getHistory(IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
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

	public void setContents(
		IFileState source,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setContents(
		IFileState source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setContents(
		InputStream source,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	public void setContents(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException
	{
		throw new UnsupportedOperationException();
	}

}
