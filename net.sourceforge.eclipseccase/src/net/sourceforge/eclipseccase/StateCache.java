package net.sourceforge.eclipseccase;

import net.sourceforge.eclipseccase.jni.Clearcase;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * @author conwaym
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class StateCache
{
	public static final QualifiedName ID =
		new QualifiedName("net.sourceforge.eclipseccase", "StateCache");

	private boolean isCheckedOut = false;
	private boolean hasRemote = false;
	private boolean isDirty = false;
	private boolean isSnapShot = false;

	private StateCache()
	{
	}

	static public StateCache getState(IResource resource)
	{
		StateCache cache = null;
		try
		{
			cache = (StateCache) resource.getSessionProperty(ID);
			if (cache == null)
			{
				cache = new StateCache();
				cache.update(resource);
			}
			resource.setSessionProperty(ID, cache);
		}
		catch (CoreException e)
		{
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
			if (hasRemote)
			{
				isCheckedOut = Clearcase.isCheckedOut(path);
			}
			isDirty = (!hasRemote) || isCheckedOut;
			isSnapShot = provider.isSnapShot();
		}
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
	 * Sets the isCheckedOut.
	 * @param isCheckedOut The isCheckedOut to set
	 */
	public void setIsCheckedOut(boolean isCheckedOut)
	{
		this.isCheckedOut = isCheckedOut;
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
	 * Sets the isDirty.
	 * @param isDirty The isDirty to set
	 */
	public void setIsDirty(boolean isDirty)
	{
		this.isDirty = isDirty;
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
	 * Sets the hasRemote.
	 * @param hasRemote The hasRemote to set
	 */
	public void setHasRemote(boolean hasRemote)
	{
		this.hasRemote = hasRemote;
	}

	/**
	 * Gets the isSnapShot.
	 * @return Returns a boolean
	 */
	public boolean isSnapShot()
	{
		return isSnapShot;
	}

	/**
	 * Sets the isSnapShot.
	 * @param isSnapShot The isSnapShot to set
	 */
	public void setIsSnapShot(boolean isSnapShot)
	{
		this.isSnapShot = isSnapShot;
	}

}