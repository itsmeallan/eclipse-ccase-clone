package net.sourceforge.eclipseccase.ui;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamPlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamUIPlugin;
import org.eclipse.ui.internal.misc.OverlayComposite;
import org.eclipse.ui.internal.misc.OverlayIcon;

// Borrowed heavily from the ExampleDecorator from Team examples
public class ClearcaseDecorator
	extends LabelProvider
	implements ILabelDecorator, IResourceChangeListener
{

	// Used to exit the isDirty resource visitor
	private static final CoreException CORE_EXCEPTION =
		new CoreException(new Status(IStatus.OK, "id", 1, "", null));

	public ClearcaseDecorator()
	{
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
			this,
			IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * @see ITeamDecorator#getText(String, IResource)
	 */
	public String decorateText(String text, Object element)
	{
		IResource resource = getResource(element);
		if (resource == null || resource.getType() == IResource.ROOT)
			return text;
		ClearcaseProvider p = getClearcaseProvider(resource);
		if (p == null)
			return text;
		
		if (resource.getType() == IResource.PROJECT)
		{
			StringBuffer buffer = new StringBuffer(text);
			buffer.append(" [clearcase]");
			return buffer.toString();
		}
		
		return text;
	}

	/*
	 * @see ITeamDecorator#getImage(IResource)
	 */
	public Image decorateImage(Image image, Object element)
	{
		IResource resource = getResource(element);
		if (resource == null || resource.getType() == IResource.ROOT)
			return image;
		ClearcaseProvider p = getClearcaseProvider(resource);
		if (p == null)
			return image;
		if (!p.hasRemote(resource))
			return image;

		OverlayComposite result = new OverlayComposite(image.getImageData());

		if (p.isCheckedOut(resource))
		{
			result.addForegroundImage(
				TeamUIPlugin
					.getPlugin()
					.getImageDescriptor(ISharedImages.IMG_CHECKEDOUT_OVR)
					.getImageData());
		}
		else
		{
			result.addForegroundImage(
				TeamUIPlugin
					.getPlugin()
					.getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR)
					.getImageData());
		}
/*
 * too slow till we add cached state
		if (isDirty(resource))
		{
			result.addForegroundImage(
				TeamUIPlugin
					.getPlugin()
					.getImageDescriptor(ISharedImages.IMG_DIRTY_OVR)
					.getImageData());
		}
*/
		return result.createImage();
	}

	/**
	 * Consider a resource dirty if any of its members are dirty
	 */
	private boolean isDirty(IResource resource)
	{
		try
		{
			resource.accept(new IResourceVisitor()
			{
				public boolean visit(IResource resource) throws CoreException
				{
					ClearcaseProvider p =
						(ClearcaseProvider) RepositoryProvider.getProvider(resource.getProject());
					if (p == null)
						return false;

					if (!p.hasRemote(resource))
						return false;

					if (p.isDirty(resource))
						throw CORE_EXCEPTION;

					return true;
				}
			}, IResource.DEPTH_INFINITE, true);
		}
		catch (CoreException e)
		{
			//if our exception was caught, we know there's a dirty child
			return e == CORE_EXCEPTION;
		}
		return false;
	}

	/*
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event)
	{
		final LinkedList events = new LinkedList();
		try
		{
			event.getDelta().accept(new IResourceDeltaVisitor()
			{
				public boolean visit(IResourceDelta delta) throws CoreException
				{
					IResource resource = delta.getResource();
					int type = resource.getType();

					// skip workspace root
					if (type == IResource.ROOT)
					{
						return true;
					}

					// if this is a change event for a resource not associated with a simple
					// project, then stop processing the deltas and return an empty event list.
					ClearcaseProvider p = getClearcaseProvider(resource);
					if (p == null)
						return false;

					// don't care about deletions
					if (delta.getKind() == IResourceDelta.REMOVED)
					{
						return false;
					}

					// ignore subtrees that don't have remotes
					if (!p.hasRemote(resource))
					{
						return false;
					}
					// chances are the team outgoing bit needs to be updated
					// if any child has changed.
					events.addFirst(
						new LabelProviderChangedEvent(ClearcaseDecorator.this, resource));
					return true;
				}
			});
		}
		catch (CoreException e)
		{
			TeamUIPlugin.log(e.getStatus());
		}
		// post label events for resource delta, this will force a refresh of the decorations
		// for the elements in the delta.
		postLabelEvents(
			(LabelProviderChangedEvent[]) events.toArray(
				new LabelProviderChangedEvent[events.size()]));
	}

	private ClearcaseProvider getClearcaseProvider(IResource resource)
	{
		try
		{
			return (ClearcaseProvider) RepositoryProvider.getProvider(
				resource.getProject());
		}
		catch (ClassCastException e)
		{
			return null;
		}
	}

	private IResource getResource(Object object)
	{
		if (object instanceof IResource)
		{
			return (IResource) object;
		}
		if (object instanceof IAdaptable)
		{
			return (IResource) ((IAdaptable) object).getAdapter(IResource.class);
		}
		return null;
	}

	private void postLabelEvents(final LabelProviderChangedEvent[] events)
	{
		if (events.length > 0)
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					for (int i = 0; i < events.length; i++)
					{
						fireLabelProviderChanged(events[i]);
					}
				}
			});
		}
	}

}