package net.sourceforge.eclipseccase.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
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
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.ui.IDecoratorManager;

// Borrowed heavily from the ExampleDecorator from Team examples
public class ClearcaseDecorator
	extends LabelProvider
	implements ILabelDecorator, IResourceChangeListener
{

	// Used to exit the isDirty resource visitor
	private static final CoreException CORE_DIRTY_EXCEPTION =
		new CoreException(new Status(IStatus.OK, "dirty", 1, "", null));
	private static final CoreException CORE_UNKNOWN_EXCEPTION =
		new CoreException(new Status(IStatus.OK, "unknown", 1, "", null));
	private final int CLEAN_STATE = 0;
	private final int DIRTY_STATE = 1;
	private final int UNKNOWN_STATE = 2;
		
		
	private static final String ID =
		"net.sourceforge.eclipseccase.ui.decorator";

	private Map iconCache = new HashMap();

	private static final int LABEL_QUEUE_INTERVAL = 5;
	private static final int LABEL_QUEUE_RETRY = 5;

	private List labelQueue = Collections.synchronizedList(new LinkedList());
	private List parentLabelQueue = Collections.synchronizedList(new LinkedList());
	private Thread labelQueueThread;

	public ClearcaseDecorator()
	{
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
			this,
			IResourceChangeEvent.POST_CHANGE);
		labelQueueThread = new Thread(new Runnable() {
			public void run()
			{
				IResource[] resources;
				int parentCounter = 0;
				while(true)
				{
					// Use polling instead of wait/notify as otherwise you end up
					// flooding the UI event queue, thereby making the UI unresponsive
					try{ Thread.sleep(500); } catch (InterruptedException e) {}
	
					int size = labelQueue.size();
					resources = new IResource[size];
					for (int i = 0; i < size; i++)
					{
						IResource resource = (IResource) labelQueue.remove(0);
						resources[i] = resource;
					}
					postLabelEvent(new LabelProviderChangedEvent(ClearcaseDecorator.this, resources));
					if (size == 0)
					{
						if (parentCounter++ > 4)
						{
							parentCounter = 0;
							int psize = parentLabelQueue.size();
							resources = new IResource[psize];
							for(int p = 0; p < psize; p++)
							{
								IResource resource = (IResource) parentLabelQueue.remove(0);
								resources[p] = resource;
							}
							postLabelEvent(new LabelProviderChangedEvent(ClearcaseDecorator.this, resources));
						}
					}
				}
			}
		}, "ClearcaseProvider Decorator thread");
		labelQueueThread.start();
	}

	public static void refresh()
	{
		IDecoratorManager manager =
			ClearcasePlugin.getDefault().getWorkbench().getDecoratorManager();
		if (manager.getEnabled(ID))
		{
			final ClearcaseDecorator activeDecorator =
				(ClearcaseDecorator) manager.getLabelDecorator(ID);
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					activeDecorator.fireLabelProviderChanged(
						new LabelProviderChangedEvent(activeDecorator));
				}
			});
		}
	}

	/*
	 * @see ITeamDecorator#getText(String, IResource)
	 */
	public String decorateText(String text, Object element)
	{
		IResource resource = getResource(element);
		if (resource == null || resource.getType() == IResource.ROOT)
			return text;
		ClearcaseProvider p = ClearcaseProvider.getProvider(resource);
		if (p == null)
			return text;

		if (!p.hasRemote(resource))
			return text;

		StringBuffer buffer = new StringBuffer(text);

		if (resource.getType() == IResource.PROJECT)
		{
			buffer.append(" [view: ");
			buffer.append(p.getViewName(resource));
			buffer.append("]");
		}

		if (ClearcasePlugin.isTextVersionDecoration())
		{
			int dirty = isDirty(resource);
			if (dirty == DIRTY_STATE)
			{
				buffer.insert(0, ">");
			}
			else if (dirty == UNKNOWN_STATE)
			{
				buffer.insert(0, "?>");
			}
			
			buffer.append(" : ");
			buffer.append(p.getVersion(resource));
		}

		return buffer.toString();
	}

	private synchronized Image getImage(HashableComposite icon)
	{
		Image image = (Image) iconCache.get(icon);
		if (image == null)
		{
			image = icon.createImage();
			iconCache.put(icon, image);
			//ClearcasePlugin.log(IStatus.INFO, "Image count at: " + iconCache.size(), null);
		}
		return image;
	}

	public Image decorateImage(Image image, Object element)
	{
		IResource resource = getResource(element);
		if (resource == null || resource.getType() == IResource.ROOT)
			return image;
		ClearcaseProvider p = ClearcaseProvider.getProvider(resource);
		if (p == null)
			return image;

		HashableComposite result = new HashableComposite(image);

		if (p.isUnknownState(resource))
		{
			result.addForegroundImage(
				ClearcaseImages.getImageDescriptor(
					ClearcaseImages.IMG_UNKNOWN_OVR));
		}
		else
		{
			if (!p.hasRemote(resource))
				return image;

			if (p.isCheckedOut(resource))
			{
				result.addForegroundImage(
					ClearcaseImages.getImageDescriptor(
						ISharedImages.IMG_CHECKEDOUT_OVR));
			}
			else if (p.isHijacked(resource))
			{
				result.addForegroundImage(
					ClearcaseImages.getImageDescriptor(
						ClearcaseImages.IMG_HIJACKED_OVR));
			}
			else
			{
				result.addForegroundImage(
					ClearcaseImages.getImageDescriptor(
						ISharedImages.IMG_CHECKEDIN_OVR));
			}

			int dirty = isDirty(resource);
			if (dirty == DIRTY_STATE)
			{
				result.addForegroundImage(
					ClearcaseImages.getImageDescriptor(
						ClearcaseImages.IMG_DIRTY_OVR));
			}
			else if(dirty == UNKNOWN_STATE)
			{
				result.addForegroundImage(
					ClearcaseImages.getImageDescriptor(
						ClearcaseImages.IMG_DIRTY_UNKNOWN_OVR));
			}
		}

		return getImage(result);
	}

	/**
	 * Consider a resource dirty if any of its members are dirty
	 */
	private int isDirty(IResource resource)
	{
		// Since dirty == checkout/hijacked for files, redundant to show files as dirty
		if (resource.getType() == IResource.FILE)
			return CLEAN_STATE;

		if (!ClearcasePlugin.isDeepDecoration())
			return CLEAN_STATE;

		try
		{
			resource.accept(new IResourceVisitor()
			{
				public boolean visit(IResource resource) throws CoreException
				{
					ClearcaseProvider p =
						ClearcaseProvider.getProvider(resource);
					if (p == null)
						return false;

					if (p.isUnknownState(resource))
						throw CORE_UNKNOWN_EXCEPTION;
						
					if (!p.hasRemote(resource))
						return false;

					if (p.isCheckedOut(resource) || p.isHijacked(resource))
						throw CORE_DIRTY_EXCEPTION;

					return true;
				}
			}, IResource.DEPTH_INFINITE, true);
		}
		catch (CoreException e)
		{
			//if our exception was caught, we know there's a dirty child
			if (e == CORE_DIRTY_EXCEPTION)
			{
				return DIRTY_STATE;
			}
			else if (e == CORE_UNKNOWN_EXCEPTION)
			{
				return UNKNOWN_STATE;
			}
		}
		return CLEAN_STATE;
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
					ClearcaseProvider p =
						ClearcaseProvider.getProvider(resource);
					if (p == null)
						return false;

					// don't care about deletions
					if (delta.getKind() == IResourceDelta.REMOVED)
					{
						return false;
					}

					// ignore subtrees that don't have remotes - exception is project which may not be a clearcase element
					if (type != IResource.PROJECT && !p.hasRemote(resource))
					{
						return false;
					}

					events.addFirst(resource);
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
		if (events.size() > 0)
		{
			postLabelEvent(
				new LabelProviderChangedEvent(
					this,
					(IResource[]) events.toArray(new IResource[events.size()])));
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
			return (IResource) ((IAdaptable) object).getAdapter(
				IResource.class);
		}
		return null;
	}

	private void postLabelEvent(final LabelProviderChangedEvent event)
	{
		if (event != null)
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					fireLabelProviderChanged(event);
				}
			});
		}
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

	public static void labelResource(IResource resource)
	{
		IDecoratorManager manager =
			ClearcasePlugin.getDefault().getWorkbench().getDecoratorManager();
		if (manager.getEnabled(ID))
		{
			ClearcaseDecorator activeDecorator =
				(ClearcaseDecorator) manager.getLabelDecorator(ID);
			activeDecorator.postLabelResource(resource);
		}
	}

	public void postLabelResource(IResource resource)
	{
		labelQueue.add(resource);
		for (IResource parent = resource.getParent();
			parent != null;
			parent = parent.getParent())
		{
			if (! parentLabelQueue.contains(parent))
				parentLabelQueue.add(parent);
		}
	}

}