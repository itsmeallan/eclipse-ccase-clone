/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 8, 2004
 */

package net.sourceforge.eclipseccase.views;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 * TODO Provide description for CheckoutsViewContentProvider.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class ClearcaseContentProvider implements ITreeContentProvider
{
    DeferredTreeContentManager manager;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        if (viewer instanceof AbstractTreeViewer)
        {
            manager = new DeferredTreeContentManager(this,
                    (AbstractTreeViewer) viewer);
        }
    }

    public boolean hasChildren(Object element)
    {
        // the + box will always appear, but then disappear
        // if not needed after you first click on it.
        if (element instanceof ClearcaseViewRoot)
        {
            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object element)
    {
        if (element instanceof ClearcaseViewRoot)
        {
            ((ClearcaseViewRoot) element).setWorkingSet(getWorkingSet());
            if (manager != null)
            {
                Object[] children = manager.getChildren(element);
                if (children != null)
                {
                    // This will be a placeholder to indicate
                    // that the real children are being fetched
                    return children;
                }
            }
        }
        return new Object[0];
    }

    public void cancelJobs(ClearcaseViewRoot root)
    {
        if (manager != null)
        {
            manager.cancel(root);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement)
    {
        return getChildren(inputElement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {}

    private IWorkingSet workingSet;

    /**
     * Returns the workingSet.
     * 
     * @return IWorkingSet
     */
    public IWorkingSet getWorkingSet()
    {
        return workingSet;
    }

    /**
     * Sets the workingSet.
     * 
     * @param workingSet
     *            The workingSet to set
     */
    public void setWorkingSet(IWorkingSet workingSet)
    {
        this.workingSet = workingSet;
    }

}