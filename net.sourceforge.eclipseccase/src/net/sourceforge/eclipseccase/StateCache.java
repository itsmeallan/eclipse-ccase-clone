package net.sourceforge.eclipseccase;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class StateCache implements Serializable
{
	public static final String STATE_CHANGE_MARKER_TYPE =
		"net.sourceforge.eclipseccase.statechangedmarker";


	private String osPath;
	private transient IResource resource;
	private boolean uninitialized = true;
	private boolean hasRemote = false;
	private boolean isCheckedOut = false;
	private boolean isDirty = false;
	private String version = "";

	public StateCache(String osPath)
	{
		this.osPath = osPath;
		createResource();
	}

	private void createResource()
	{
		File file = new File(osPath);
		IPath path = new Path(osPath);
		if (file.isDirectory())
			resource = ClearcasePlugin.getWorkspace().getRoot().getContainerForLocation(path);
		else
			resource = ClearcasePlugin.getWorkspace().getRoot().getFileForLocation(path);
	}
	
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
	{
		s.defaultReadObject();
		createResource();		
	}
	
	public synchronized void updateAsync()
	{
		uninitialized = true;
		ClearcaseDecorator.labelResource(resource);
		UpdateQueue.getInstance().add(new Runnable()
		{
			public void run() { update(); }
		});
	}

	
	public synchronized void update()
	{
		hasRemote = ClearcasePlugin.getEngine().isElement(osPath);
		isCheckedOut = hasRemote && ClearcasePlugin.getEngine().isCheckedOut(osPath);
		isDirty = (!hasRemote) || isCheckedOut;
		version = ClearcasePlugin.getEngine().cleartool("describe -fmt \"%Vn\" \"" + osPath + "\"").message.trim().replace('\\', '/');
		uninitialized = false;
		ClearcaseDecorator.labelResource(resource);
	}

	/**
	 * Gets the hasRemote.
	 * @return Returns a boolean
	 */
	public boolean hasRemote()
	{
		return hasRemote;
	}

	/**
	 * Gets the isCheckedOut.
	 * @return Returns a boolean
	 */
	public boolean isCheckedOut()
	{
		return isCheckedOut;
	}

	/**
	 * Gets the isDirty.
	 * @return Returns a boolean
	 */
	public boolean isDirty()
	{
		return isDirty;
	}

	/**
	 * Returns the osPath.
	 * @return String
	 */
	public String getPath()
	{
		return osPath;
	}

	/**
	 * Returns the version.
	 * @return String
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Returns the uninitialized.
	 * @return boolean
	 */
	public boolean isUninitialized()
	{
		return uninitialized;
	}

}