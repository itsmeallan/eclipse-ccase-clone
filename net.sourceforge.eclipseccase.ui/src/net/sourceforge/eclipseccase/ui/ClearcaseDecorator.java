
package net.sourceforge.eclipseccase.ui;

import java.util.HashSet;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.StateCache;
import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.StateChangeListener;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.decorators.DecoratorManager;

public class ClearcaseDecorator extends LabelProvider implements
        ILightweightLabelDecorator, StateChangeListener
{
    public static final String ID = "net.sourceforge.eclipseccase.ui.decorator";

    // Used to exit the isDirty resource visitor
    static final CoreException CORE_DIRTY_EXCEPTION = new CoreException(
            new Status(IStatus.OK, "dirty", 1, "", null));

    static final CoreException CORE_UNKNOWN_EXCEPTION = new CoreException(
            new Status(IStatus.OK, "unknown", 1, "", null));

    private final int CLEAN_STATE = 0;

    private final int DIRTY_STATE = 1;

    private final int UNKNOWN_STATE = 2;

    //private Timer parentUpdateTimer;
    //boolean decorationInProcess = false;
    //List parentQueue = new LinkedList();

    public ClearcaseDecorator()
    {
        super();
        DecoratorManager manager = (DecoratorManager) ClearcaseUI.getInstance()
                .getWorkbench().getDecoratorManager();
        addListener(manager);
        StateCacheFactory.getInstance().addStateChangeListerer(this);

        //parentUpdateTimer = new Timer();
        //parentUpdateTimer.schedule(new TimerTask()
        //{
        //    public void run()
        //    {
        //        //synchronized (ClearcaseDecorator.this)
        //        {
        //            if (decorationInProcess)
        //            {
        //                //decorationInProcess = false;
        //                return;
        //            }
        //        }
        //        synchronized (parentQueue)
        //        {
        //            if (parentQueue.size() > 0)
        //            {
        //                resourceStateChanged(
        //                    (IResource[]) parentQueue.toArray(new
        // IResource[parentQueue.size()]));
        //                parentQueue.clear();
        //            }
        //        }
        //    }
        //}, 0, 500);
    }

    public void dispose()
    {
    //parentUpdateTimer.cancel();
    }

    /**
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
     *      org.eclipse.jface.viewers.IDecoration)
     */
    public void decorate(Object element, IDecoration decoration)
    {
        IResource resource = getResource(element);
        if (resource == null || resource.getType() == IResource.ROOT
                || resource.getLocation() == null || !resource.exists())
                return;

        ClearcaseProvider p = ClearcaseProvider.getClearcaseProvider(resource);
        if (p == null) return;

        if (p.isIgnored(resource)) return;

        // Projects may be the view directory containing the VOBS, if so,
        // don't want to be able to add em, or any resource diretcly under them
        if ((resource.getType() == IResource.PROJECT && !p.hasRemote(resource))
                || (resource.getParent().getType() == IResource.PROJECT && !p
                        .hasRemote(resource.getParent()))) return;

        if (p.isUnknownState(resource))
        {
            decoration.addOverlay(IMG_DESC_UNKOWN_STATE);
            
            // no further decoration
            return;
        }
        else if (resource.getType() != IResource.PROJECT
                && !p.hasRemote(resource))
        {
            // decorate new elements not added to ClearCase
            decoration.addOverlay(IMG_DESC_NEW_RESOURCE);

            if (ClearcaseUI.isTextNewDecoration()) decoration.addPrefix("*");
            
            // no further decoration
            return;
        }
        else if (p.isCheckedOut(resource))
        {
            // check out
            decoration.addOverlay(IMG_DESC_CHECKED_OUT);

            // no further decoration
            return;
        }
        else if (p.isHijacked(resource))
        {
            // hijacked
            decoration.addOverlay(IMG_DESC_HIJACKED);
            
            // no further decoration
            return;
        }
        else if (p.isSymbolicLink(resource))
        {
            // symbolic link
            if(p.isSymbolicLinkTargetValid(resource))
                decoration.addOverlay(IMG_DESC_LINK);
            else
                decoration.addOverlay(IMG_DESC_LINK_WARNING);
            
            decoration.addSuffix(" --> " + p.getSymbolicLinkTarget(resource));
            
            // no further decoration
            return;
        }
        else
        {
            int dirty = isDirty(resource);

            if (dirty == DIRTY_STATE)
            {
                decoration.addOverlay(IMG_DESC_DIRTY);
            }
            else if (dirty == UNKNOWN_STATE)
            {
                decoration.addOverlay(IMG_DESC_UNKOWN_STATE);
            }
            else if (p.hasRemote(resource))
            {
                decoration.addOverlay(IMG_DESC_CHECKED_IN);
            }

            if (ClearcaseUI.isTextDirtyDecoration())
            {
                if (dirty == DIRTY_STATE)
                {
                    decoration.addPrefix(">");
                }
                else if (dirty == UNKNOWN_STATE)
                {
                    decoration.addPrefix("?");
                }
            }
        }

        if (ClearcaseUI.isTextViewDecoration()
                && resource.getType() == IResource.PROJECT)
        {
            decoration.addSuffix(" [view: " + p.getViewName(resource) + "]");
        }

        if (ClearcaseUI.isTextVersionDecoration())
        {
            decoration.addSuffix("  " + p.getVersion(resource));
        }

    }

    /**
     * The state of the specified resources has changed.
     * 
     * @param changedResources
     */
    private void resourceStateChanged(IResource[] changedResources)
    {
        if (changedResources.length == 0) return;

        boolean deepDecoration = ClearcaseUI.isDeepDecoration();

        if (!deepDecoration)
        {
            fireChange(new LabelProviderChangedEvent(this, changedResources));
            return;
        }

        final HashSet changedElements = new HashSet(
                changedResources.length * 20);
        for (int i = 0; i < changedResources.length; i++)
        {
            IResource resource = changedResources[i];

            // process children
            if (resource.exists())
            {

                try
                {
                    // refresh children
                    resource.accept(new IResourceVisitor()
                    {
                        public boolean visit(IResource child)
                                throws CoreException
                        {
                            return changedElements.add(child);
                        }
                    });
                }
                catch (CoreException ex)
                {
                    ClearcasePlugin.log(IStatus.ERROR,
                            "Could not access resource: "
                                    + resource.getFullPath().toString(), ex);
                }

            }

            // refresh also parents
            changedElements.add(resource);
            resource = resource.getParent();
            while (null != resource && changedElements.add(resource))
                resource = resource.getParent();
        }
        fireChange(new LabelProviderChangedEvent(this, changedElements
                .toArray()));
    }

    /**
     * Fire event in the ui thread.
     * 
     * @param event
     */
    private void fireChange(final LabelProviderChangedEvent event)
    {
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (null != display && !display.isDisposed())
        {
            display.asyncExec(new Runnable()
            {
                public void run()
                {
                    fireLabelProviderChanged(event);
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#fireLabelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
     */
    protected void fireLabelProviderChanged(LabelProviderChangedEvent event)
    {
        super.fireLabelProviderChanged(event);
    }

    private int isDirty(IResource resource)
    {
        // Since dirty == checkout/hijacked for files, redundant to show files
        // as dirty
        if (resource.getType() == IResource.FILE || !resource.exists()
                || resource.getLocation() == null) return CLEAN_STATE;

        if (!ClearcaseUI.isDeepDecoration()) return CLEAN_STATE;

        try
        {
            resource.accept(new IResourceVisitor()
            {
                public boolean visit(IResource childResource)
                        throws CoreException
                {
                    ClearcaseProvider p = ClearcaseProvider
                            .getClearcaseProvider(childResource);
                    if (p == null) return false;

                    if (p.isIgnored(childResource)) return false;

                    if (p.isUnknownState(childResource))
                            throw CORE_UNKNOWN_EXCEPTION;

                    if (!p.hasRemote(childResource))
                            throw CORE_DIRTY_EXCEPTION;

                    if (p.isCheckedOut(childResource)
                            || p.isHijacked(childResource))
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

    /**
     * Returns the resource for the given input object, or null if there is no
     * resource associated with it.
     * 
     * @param object
     *            the object to find the resource for
     * @return the resource for the given object, or null
     */
    private IResource getResource(Object object)
    {
        if (object instanceof IResource)
        {
            return (IResource) object;
        }
        if (object instanceof IAdaptable)
        {
            return (IResource) ((IAdaptable) object)
                    .getAdapter(IResource.class);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.eclipseccase.StateChangeListener#stateChanged(net.sourceforge.eclipseccase.StateCache)
     */
    public void stateChanged(StateCache stateCache)
    {
        resourceStateChanged(new IResource[]{stateCache.getResource()});
    }

    /**
     * Refreshes this label provider.
     */
    public void refresh()
    {
        fireChange(new LabelProviderChangedEvent(this));
    }

    // Images cached for better performance
    private static ImageDescriptor IMG_DESC_DIRTY;

    private static ImageDescriptor IMG_DESC_CHECKED_IN;

    private static ImageDescriptor IMG_DESC_CHECKED_OUT;

    private static ImageDescriptor IMG_DESC_HIJACKED;

    private static ImageDescriptor IMG_DESC_NEW_RESOURCE;

    private static ImageDescriptor IMG_DESC_LINK;

    private static ImageDescriptor IMG_DESC_LINK_WARNING;

    private static ImageDescriptor IMG_DESC_EDITED;

    private static ImageDescriptor IMG_DESC_UNKOWN_STATE;

    static
    {
        IMG_DESC_DIRTY = new CachedImageDescriptor(TeamUIPlugin
                .getImageDescriptor(ISharedImages.IMG_DIRTY_OVR));
        IMG_DESC_CHECKED_IN = new CachedImageDescriptor(TeamUIPlugin
                .getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR));
        IMG_DESC_CHECKED_OUT = new CachedImageDescriptor(TeamUIPlugin
                .getImageDescriptor(ISharedImages.IMG_CHECKEDOUT_OVR));
        IMG_DESC_NEW_RESOURCE = new CachedImageDescriptor(ClearcaseImages
                .getImageDescriptor(ClearcaseImages.IMG_QUESTIONABLE));
        IMG_DESC_EDITED = new CachedImageDescriptor(ClearcaseImages
                .getImageDescriptor(ClearcaseImages.IMG_EDITED));
        IMG_DESC_UNKOWN_STATE = new CachedImageDescriptor(ClearcaseImages
                .getImageDescriptor(ClearcaseImages.IMG_NO_REMOTEDIR));
        IMG_DESC_LINK = new CachedImageDescriptor(ClearcaseImages
                .getImageDescriptor(ClearcaseImages.IMG_LINK));
        IMG_DESC_LINK_WARNING = new CachedImageDescriptor(ClearcaseImages
                .getImageDescriptor(ClearcaseImages.IMG_LINK_WARNING));
        IMG_DESC_HIJACKED = new CachedImageDescriptor(ClearcaseImages
                .getImageDescriptor(ClearcaseImages.IMG_HIJACKED));
    }

    /*
     * Define a cached image descriptor which only creates the image data once
     */
    public static class CachedImageDescriptor extends ImageDescriptor
    {
        ImageDescriptor descriptor;

        ImageData data;

        public CachedImageDescriptor(ImageDescriptor descriptor)
        {
            assert null != descriptor;
            this.descriptor = descriptor;
        }

        public ImageData getImageData()
        {
            if (data == null)
            {
                data = descriptor.getImageData();
            }
            return data;
        }
    }
}

