
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
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
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

        ClearcaseProvider p = ClearcaseProvider.getProvider(resource);
        if (p == null) return;

        if (p.isIgnored(resource)) return;

        // Projects may be the view directory containing the VOBS, if so,
        // don't want to be able to add em, or any resource diretcly under them
        if ((resource.getType() == IResource.PROJECT && !p.hasRemote(resource))
                || (resource.getParent().getType() == IResource.PROJECT && !p
                        .hasRemote(resource.getParent()))) return;

        if (p.isUnknownState(resource))
        {
            decoration.addOverlay(ClearcaseImages
                    .getImageDescriptor(ClearcaseImages.IMG_UNKNOWN_OVR));
        }
        else if (resource.getType() != IResource.PROJECT
                && !p.hasRemote(resource))
        {
            // decorate new elements not added to ClearCase
            decoration.addOverlay(ClearcaseImages
                    .getImageDescriptor(ClearcaseImages.IMG_NEW_OVR));

            if (ClearcasePlugin.isTextNewDecoration())
                    decoration.addPrefix("*");
        }
        else
        {
            int dirty = isDirty(resource);

            if (p.isCheckedOut(resource))
            {
                decoration
                        .addOverlay(ClearcaseImages
                                .getImageDescriptor(ClearcaseImages.IMG_CHECKEDOUT_OVR));
            }
            else if (dirty == DIRTY_STATE)
            {
                decoration.addOverlay(ClearcaseImages
                        .getImageDescriptor(ClearcaseImages.IMG_DIRTY_OVR));
            }
            else if (dirty == UNKNOWN_STATE)
            {
                decoration
                        .addOverlay(ClearcaseImages
                                .getImageDescriptor(ClearcaseImages.IMG_DIRTY_UNKNOWN_OVR));
            }
            else if (p.isHijacked(resource))
            {
                decoration.addOverlay(ClearcaseImages
                        .getImageDescriptor(ClearcaseImages.IMG_HIJACKED_OVR));
            }
            else if (p.hasRemote(resource))
            {
                decoration.addOverlay(ClearcaseImages
                        .getImageDescriptor(ClearcaseImages.IMG_CHECKEDIN_OVR));
            }

            StringBuffer prefix = new StringBuffer();
            StringBuffer suffix = new StringBuffer();

            if (ClearcasePlugin.isTextViewDecoration()
                    && resource.getType() == IResource.PROJECT)
            {
                suffix.append(" [view: ");
                suffix.append(p.getViewName(resource));
                suffix.append("]");
            }

            if (ClearcasePlugin.isTextDirtyDecoration())
            {
                if (dirty == DIRTY_STATE)
                {
                    prefix.append(">");
                }
                else if (dirty == UNKNOWN_STATE)
                {
                    prefix.append("?>");
                }
            }

            if (ClearcasePlugin.isTextVersionDecoration())
            {
                suffix.append(" : ");
                suffix.append(p.getVersion(resource));
            }

            decoration.addPrefix(prefix.toString());
            decoration.addSuffix(suffix.toString());
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

        boolean deepDecoration = ClearcasePlugin.isDeepDecoration();

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

        if (!ClearcasePlugin.isDeepDecoration()) return CLEAN_STATE;

        try
        {
            resource.accept(new IResourceVisitor()
            {
                public boolean visit(IResource childResource)
                        throws CoreException
                {
                    ClearcaseProvider p = ClearcaseProvider
                            .getProvider(childResource);
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

}