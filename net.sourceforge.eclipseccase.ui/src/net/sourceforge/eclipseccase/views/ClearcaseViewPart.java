/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 8, 2004
 */

package net.sourceforge.eclipseccase.views;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.StateCache;
import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.StateChangeListener;
import net.sourceforge.eclipseccase.ui.ClearcaseUI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.progress.IElementCollector;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.views.navigator.ResourceNavigator;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * TODO Provide description for ClearcaseView.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public abstract class ClearcaseViewPart extends ResourceNavigator implements
        StateChangeListener, IResourceChangeListener
{

    private ClearcaseContentProvider contentProvider;

    /**
     * @see org.eclipse.team.internal.ccvs.ui.repo.RemoteViewPart#getContentProvider()
     */
    protected ClearcaseContentProvider getContentProvider()
    {
        if (contentProvider == null)
        {
            contentProvider = new ClearcaseContentProvider();
        }
        return contentProvider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.navigator.ResourceNavigator#initContentProvider(org.eclipse.jface.viewers.TreeViewer)
     */
    protected void initContentProvider(TreeViewer viewer)
    {
        viewer.setContentProvider(getContentProvider());
        StateCacheFactory.getInstance().addStateChangeListerer(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.views.navigator.ResourceNavigator#initLabelProvider(org.eclipse.jface.viewers.TreeViewer)
     */
    protected void initLabelProvider(TreeViewer viewer)
    {
		viewer.setLabelProvider(
				new DecoratingLabelProvider(
					new ClearcaseViewLabelProvider(),
					getPlugin().getWorkbench().getDecoratorManager().getLabelDecorator()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.navigator.ResourceNavigator#setSorter(org.eclipse.ui.views.navigator.ResourceSorter)
     */
    public void setSorter(ResourceSorter sorter)
    {
        super.setSorter(new ResourceSorter(sorter.getCriteria())
        {
            protected int compareNames(IResource resource1, IResource resource2)
            {
                return collator.compare(resource1.getFullPath().toString(), resource2.getFullPath().toString());
            }
        });
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.navigator.ResourceNavigator#getInitialInput()
     */
    protected IAdaptable getInitialInput()
    {
        return getRoot();
    }

    private ClearcaseViewRoot myRoot;

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.eclipseccase.views.ClearcaseViewPart#getRoot()
     */
    protected ClearcaseViewRoot getRoot()
    {
        if (null == myRoot)
        {
            myRoot = new ClearcaseViewRoot()
            {

                protected void collectElements(IProject[] clearcaseProjects,
                        IElementCollector collector, IProgressMonitor monitor)
                {
                    findResources(clearcaseProjects, collector, monitor);
                }
            };
        }

        return myRoot;
    }

    /**
     * Finds all checkouts
     * 
     * @param collector
     * @param monitor
     */
    protected void findResources(IProject[] clearcaseProjects,
            IElementCollector collector, IProgressMonitor monitor)
    {
        try
        {
            monitor.beginTask("Searching for resource",
                    clearcaseProjects.length * 100000);
            for (int i = 0; i < clearcaseProjects.length; i++)
            {
                IProject project = clearcaseProjects[i];
                monitor.subTask("Searching in project " + project.getName());
                try
                {
                    findResources(project, collector, new SubProgressMonitor(
                            monitor, 100000,
                            SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
                }
                catch (CoreException ex)
                {
                    handleError(ex, "Error",
                            "An error occured while searching for resources in project "
                                    + project.getName() + ".");
                }
            }
        }
        finally
        {
            monitor.done();
        }

    }

    protected void findResources(IResource resource,
            final IElementCollector collector, final IProgressMonitor monitor)
            throws CoreException
    {
        try
        {
            // determine children
            IResource[] children = (resource instanceof IContainer) ? ((IContainer) resource)
                    .members()
                    : new IResource[0];

            monitor.beginTask("processing", (children.length + 1) * 1000);

            // determine state
            if (shouldAdd(StateCacheFactory.getInstance().get(resource)))
                    collector.add(resource, new SubProgressMonitor(monitor,
                            1000, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));

            // process state
            if (children.length > 0)
            {
                for (int i = 0; i < children.length; i++)
                {
                    findResources(children[i], collector,
                            new SubProgressMonitor(monitor, 1000,
                                    SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
                }
            }
        }
        finally
        {
            monitor.done();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.navigator.ResourceNavigator#setWorkingSet(org.eclipse.ui.IWorkingSet)
     */
    public void setWorkingSet(IWorkingSet workingSet)
    {
        getContentProvider().setWorkingSet(workingSet);
        super.setWorkingSet(workingSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.navigator.ResourceNavigator#makeActions()
     */
    protected void makeActions()
    {
        setActionGroup(new ClearcaseViewActionGroup(this));
    }

    /**
     * Refreshes the viewer.
     */
    public void refresh()
    {
        if (getViewer() == null) return;
        getContentProvider().cancelJobs(getRoot());
        getViewer().refresh();
    }

    /**
     * Shows the given errors to the user.
     * 
     * @param Exception
     *            the exception containing the error
     * @param title
     *            the title of the error dialog
     * @param message
     *            the message for the error dialog
     * @param shell
     *            the shell to open the error dialog in
     */
    public void handleError(Exception exception, String title, String message)
    {
        IStatus status = null;
        boolean log = false;
        boolean dialog = false;
        Throwable t = exception;
        if (exception instanceof TeamException)
        {
            status = ((TeamException) exception).getStatus();
            log = false;
            dialog = true;
        }
        else if (exception instanceof InvocationTargetException)
        {
            t = ((InvocationTargetException) exception).getTargetException();
            if (t instanceof TeamException)
            {
                status = ((TeamException) t).getStatus();
                log = false;
                dialog = true;
            }
            else if (t instanceof CoreException)
            {
                status = ((CoreException) t).getStatus();
                log = true;
                dialog = true;
            }
            else if (t instanceof InterruptedException)
            {
                return;
            }
            else
            {
                status = new Status(IStatus.ERROR, ClearcaseUI.PLUGIN_ID, 1,
                        "An unknown exception occured: "
                                + t.getLocalizedMessage(), t);
                log = true;
                dialog = true;
            }
        }
        if (status == null) return;
        if (!status.isOK())
        {
            IStatus toShow = status;
            if (status.isMultiStatus())
            {
                IStatus[] children = status.getChildren();
                if (children.length == 1)
                {
                    toShow = children[0];
                }
            }
            if (title == null)
            {
                title = status.getMessage();
            }
            if (message == null)
            {
                message = status.getMessage();
            }
            if (dialog && getViewSite() != null
                    && getViewSite().getShell() != null)
            {
                ErrorDialog.openError(getViewSite().getShell(), title, message,
                        toShow);
            }
            if (log || getViewSite() == null
                    || getViewSite().getShell() == null)
            {
                ClearcasePlugin.log(toShow.getSeverity(), message, t);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.navigator.ResourceNavigator#dispose()
     */
    public void dispose()
    {
        StateCacheFactory.getInstance().removeStateChangeListerer(this);
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.eclipseccase.StateChangeListener#stateChanged(net.sourceforge.eclipseccase.StateCache)
     */
    public void stateChanged(StateCache stateCache)
    {
        final boolean shouldAdd = shouldAdd(stateCache);
        final IResource resource = stateCache.getResource();
        if (null != getViewer() && null != getViewer().getControl()
                && !getViewer().getControl().isDisposed())
        {
            getViewer().getControl().getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                    if (null != getViewer() && null != getViewer().getControl()
                            && !getViewer().getControl().isDisposed())
                    {
                        // we remove in every case
                        getViewer().remove(resource);

                        // only add if desired
                        if (shouldAdd) getViewer().add(getRoot(), resource);
                    }
                }
            });
        }
    }

    /**
     * Indicates if the given state cache change should add the resource to the
     * viewer.
     * 
     * @param stateCache
     * @return <code>true</code> if the state change should add the resource
     */
    protected abstract boolean shouldAdd(StateCache stateCache);

    protected static final CoreException IS_AFFECTED_EX = new CoreException(
            Status.CANCEL_STATUS);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event)
    {
        IResourceDelta rootDelta = event.getDelta();
        if (null != rootDelta)
        {
            try
            {
                rootDelta.accept(new IResourceDeltaVisitor()
                {
                    public boolean visit(IResourceDelta delta)
                            throws CoreException
                    {
                        switch (delta.getKind())
                        {
                            case IResourceDelta.ADDED:
                            case IResourceDelta.REMOVED:
                                throw IS_AFFECTED_EX;

                            default:
                                IResource resource = delta.getResource();
                                if (null != resource)
                                {
                                    // filter out non clear case projects
                                    if (resource.getType() == IResource.PROJECT)
                                            return null != ClearcaseProvider
                                                    .getClearcaseProvider(delta
                                                            .getResource());

                                    return true;
                                }
                                return false;
                        }
                    }
                });
            }
            catch (CoreException ex)
            {
                // refresh on exception
                if (IS_AFFECTED_EX == ex) refresh();
            }
        }
    }
}