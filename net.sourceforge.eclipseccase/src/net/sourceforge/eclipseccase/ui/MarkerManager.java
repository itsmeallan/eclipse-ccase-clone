package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.StateCache;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;

public class MarkerManager
{
	public static final String CHECKEDOUT_MARKER =
		"net.sourceforge.eclipseccase.checkedout";

	private static MarkerManager instance = new MarkerManager();

	private MarkerManager()
	{
	}

	public static MarkerManager getInstance()
	{
		return instance;
	}

	private boolean hasMarker(IResource resource)
	{
		try
		{
			IMarker[] markers =
				resource.findMarkers(
					CHECKEDOUT_MARKER,
					false,
					IResource.DEPTH_ZERO);
			for (int i = 0; i < markers.length; i++)
			{
				if (markers[i].exists())
					return true;
			}
		}
		catch (CoreException e)
		{
			ClearcasePlugin.log(
				IStatus.ERROR,
				"Error creating checkedout marker",
				e);
		}
		return false;
	}

	private void createMarker(IResource resource)
	{
		try
		{
			if (hasMarker(resource))
				return;
			IMarker marker = resource.createMarker(CHECKEDOUT_MARKER);
			if (marker.exists())
			{
				marker.setAttribute(
					IMarker.MESSAGE,
					"The resource is checked out");
			}
		}
		catch (CoreException e)
		{
			ClearcasePlugin.log(
				IStatus.ERROR,
				"Error creating checkedout marker",
				e);
		}
	}

	private void removeMarker(IResource resource)
	{
		IMarker[] markers = null;
		try
		{
			resource.deleteMarkers(
				CHECKEDOUT_MARKER,
				false,
				IResource.DEPTH_ZERO);
		}
		catch (CoreException e)
		{
			ClearcasePlugin.log(
				IStatus.ERROR,
				"Error removing checkedout marker",
				e);
		}
	}

	public void stateChanged(final StateCache stateCache)
	{
		if (ClearcasePlugin.isCheckedoutMarker())
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					if (stateCache.isCheckedOut())
					{
						createMarker(stateCache.getResource());
					}
					else
					{
						removeMarker(stateCache.getResource());
					}
				}
			});
		}
	}
}
