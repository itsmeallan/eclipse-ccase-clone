package net.sourceforge.eclipseccase;

import java.util.StringTokenizer;

import net.sourceforge.eclipseccase.jni.Clearcase;

import org.eclipse.core.internal.runtime.Log;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.xml.sax.InputSource;

public class StateCache
{
	public static final QualifiedName ID =
		new QualifiedName("net.sourceforge.eclipseccase", "StateCache");

	private boolean hasRemote = false;
	private boolean isCheckedOut = false;
	private boolean isDirty = false;

	private StateCache()
	{
	}

	private StateCache(IResource resource, String serialized)
	{
		if (serialized != null && serialized.length() == 3)
		{
			hasRemote = serialized.charAt(0) == '1';
			isCheckedOut = serialized.charAt(1) == '1';
			isDirty = serialized.charAt(2) == '1';
		}
		else
		{
			update(resource);
		}
	}
	
	private String serialize()
	{
		StringBuffer sb = new StringBuffer(3);
		sb.append(hasRemote ? "1" : "0");	
		sb.append(isCheckedOut ? "1" : "0");	
		sb.append(isDirty ? "1" : "0");
		return sb.toString();
	}
	
	static public StateCache getState(IResource resource)
	{
		StateCache cache = null;
		try
		{
			cache = (StateCache) resource.getSessionProperty(ID);
			if (cache == null)
			{
				String persistentCache = null;
				if (ClearcasePlugin.isPersistState())
					persistentCache = resource.getPersistentProperty(ID);
				cache = new StateCache(resource, persistentCache);
				resource.setSessionProperty(ID, cache);
				if (ClearcasePlugin.isPersistState())
					resource.setPersistentProperty(ID, cache.serialize());
			}
		}
		catch (CoreException ex)
		{
			ClearcasePlugin.log(IStatus.WARNING, "Unexpected failure retrieving clearcase state cache", ex);
		}
		return cache;
	}

	public void update(IResource resource)
	{
		ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
		if (provider != null)
		{
			String path = resource.getLocation().toOSString();
			hasRemote = Clearcase.isElement(path);
			isCheckedOut = hasRemote && Clearcase.isCheckedOut(path);
			isDirty = (!hasRemote) || isCheckedOut;
			if (ClearcasePlugin.isPersistState())
			{
				try
				{
					resource.setPersistentProperty(ID, this.serialize());
				}
				catch(CoreException ex)
				{
					ClearcasePlugin.log(IStatus.WARNING, "Could not persist clearcase state", ex);
				}
			}
		}
	}

	public void clear(IResource resource)
	{
		try
		{
			resource.setSessionProperty(ID, null);
			resource.setPersistentProperty(ID, null);		
		} catch (CoreException ex)
		{
			ClearcasePlugin.log(IStatus.WARNING, "Could not clear clearcase state", ex);
		}
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

}