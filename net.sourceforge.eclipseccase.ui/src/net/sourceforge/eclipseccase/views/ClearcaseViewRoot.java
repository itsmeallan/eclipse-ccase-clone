/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 8, 2004
 */

package net.sourceforge.eclipseccase.views;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipseccase.ClearcaseProvider;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.progress.IElementCollector;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * TODO Provide description for ClearcaseViewRoot.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public abstract class ClearcaseViewRoot implements IDeferredWorkbenchAdapter, IAdaptable
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object,
     *      org.eclipse.jface.progress.IElementCollector,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void fetchDeferredChildren(Object object,
            IElementCollector collector, IProgressMonitor monitor)
    {
        // find projects
        List projectsToSearch = null;
        if(null != getWorkingSet())
        {
            IAdaptable[] adaptables = getWorkingSet().getElements();
            projectsToSearch = new ArrayList(adaptables.length);
            for (int i = 0; i < adaptables.length; i++)
            {
                IResource resource = (IResource) adaptables[i].getAdapter(IResource.class);
                if(null != resource)
                {
                    IProject project = resource.getProject();
                    if(null != project && !projectsToSearch.contains(project) && null != ClearcaseProvider.getClearcaseProvider(project))
                    {
                        projectsToSearch.add(project);
                    }
                }
            }
        }
        else
        {
            // use all workspace projects
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            projectsToSearch = new ArrayList(projects.length);
            for (int i = 0; i < projects.length; i++)
            {
                IProject project = projects[i];
                if(!projectsToSearch.contains(project) && null != ClearcaseProvider.getClearcaseProvider(project))
                {
                    projectsToSearch.add(project);
                }
            }
        }
        
        if(null == projectsToSearch || projectsToSearch.isEmpty())
            return;

        // collect elements
    	collectElements((IProject[]) projectsToSearch.toArray(new IProject[projectsToSearch.size()]), collector, monitor);
    }

    /**
     * @param clearcaseProjects
     * @param collector
     * @param monitor
     */
    protected abstract void collectElements(IProject[] clearcaseProjects, IElementCollector collector, IProgressMonitor monitor);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
     */
    public boolean isContainer()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
     */
    public ISchedulingRule getRule(Object object)
    {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o)
    {
        return new Object[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object)
    {
        return getWorkspaceWorkbenchAdapter().getImageDescriptor(object);
    }

    /**
     * @return
     */
    private IWorkbenchAdapter getWorkspaceWorkbenchAdapter()
    {
        return ((IWorkbenchAdapter) ResourcesPlugin.getWorkspace().getRoot()
                .getAdapter(IWorkbenchAdapter.class));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o)
    {
        return getWorkspaceWorkbenchAdapter().getLabel(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o)
    {
        return null;
    }

    private IWorkingSet workingSet;

    /**
     * Returns the workingSet.
     * @return IWorkingSet
     */
    public IWorkingSet getWorkingSet() {
    	return workingSet;
    }

    /**
     * Sets the workingSet.
     * @param workingSet The workingSet to set
     */
    public void setWorkingSet(IWorkingSet workingSet) {
    	this.workingSet = workingSet;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter)
    {
        return ResourcesPlugin.getWorkspace().getRoot().getAdapter(adapter);
    }
}