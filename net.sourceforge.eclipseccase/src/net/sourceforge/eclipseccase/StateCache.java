package net.sourceforge.eclipseccase;

import java.io.Serializable;

public class StateCache implements Serializable
{
	private String osPath;
	private boolean hasRemote = false;
	private boolean isCheckedOut = false;
	private boolean isDirty = false;
	private String version = "";

	public StateCache(String osPath)
	{
		this.osPath = osPath;
		update();
	}
	
	public void update()
	{
		hasRemote = ClearcasePlugin.getEngine().isElement(osPath);
		isCheckedOut = hasRemote && ClearcasePlugin.getEngine().isCheckedOut(osPath);
		isDirty = (!hasRemote) || isCheckedOut;
		version = ClearcasePlugin.getEngine().cleartool("describe -fmt \"%Vn\" " + osPath).message.trim().replace('\\', '/');
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

}