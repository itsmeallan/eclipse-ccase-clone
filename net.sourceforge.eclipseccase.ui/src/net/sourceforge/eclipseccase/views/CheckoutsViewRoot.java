/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *     Gunnar Wagenknecht - new features, enhancements and bug fixes
 *******************************************************************************/
package net.sourceforge.eclipseccase.views;

import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.ui.ClearCaseUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

/**
 * A generic root element for the <code>ClearCaseViewPart</code>.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class CheckoutsViewRoot implements IDeferredWorkbenchAdapter, IAdaptable {

	CheckoutsView checkoutsView;

	public CheckoutsViewRoot(CheckoutsView checkoutsView) {
		this.checkoutsView = checkoutsView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren
	 * (java.lang.Object, org.eclipse.jface.progress.IElementCollector,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		if (!StateCacheFactory.getInstance().isInitialized()) {
			// during startup, don't update from uninitialized cache
			// TODO: schedule an automatic refresh after 2 seconds
			collector.done();
			return;
		}

		if (ClearCaseUI.DEBUG_VIEWPRIV) {
			ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, "fetchDeferredChildren: starting"); //$NON-NLS-1$
		}

		Iterable<IResource> resources = StateCacheFactory.getInstance().getContainedResources();
		for (IResource resource : resources) {
			// determine state
			if (checkoutsView.shouldAdd(resource)) {
				if (ClearCaseUI.DEBUG_VIEWPRIV) {
					ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, "adding to collector: " + resource.getFullPath()); //$NON-NLS-1$
				}
				collector.add(resource, new SubProgressMonitor(monitor, 1000, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			}
		}
		boolean waited = false;
		while (checkoutsView.isRefreshActive()) {
			//			ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, "fetchDeferredChildren: waiting..."); //$NON-NLS-1$
			try {
				Thread.sleep(300);
				waited = true;
			} catch (InterruptedException e) {
			}
		}
		if (waited && ClearCaseUI.DEBUG_VIEWPRIV) {
			ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, "fetchDeferredChildren: waiting for CC refresh done"); //$NON-NLS-1$
		}
		
		waited = false;
		while (StateCacheFactory.getInstance().hasPendingUpdates()) {
			ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, "fetchDeferredChildren: waiting..."); //$NON-NLS-1$
			try {
				Thread.sleep(300);
				waited = true;
			} catch (InterruptedException e) {
			}
		}
		
		if (ClearCaseUI.DEBUG_VIEWPRIV) {
			if (waited) {
				ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, "fetchDeferredChildren: waiting for async refreshes done"); //$NON-NLS-1$
			}
			ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, "fetchDeferredChildren: done"); //$NON-NLS-1$
		}
		collector.done();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
	 */
	public boolean isContainer() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object
	 * )
	 */
	public ISchedulingRule getRule(Object object) {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object
	 * )
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return getWorkspaceWorkbenchAdapter().getImageDescriptor(object);
	}

	/**
	 * @return
	 */
	private IWorkbenchAdapter getWorkspaceWorkbenchAdapter() {
		return ((IWorkbenchAdapter) ResourcesPlugin.getWorkspace().getRoot().getAdapter(IWorkbenchAdapter.class));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return getWorkspaceWorkbenchAdapter().getLabel(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return null;
	}

	private IWorkingSet workingSet;

	/**
	 * Returns the workingSet.
	 * 
	 * @return IWorkingSet
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}

	/**
	 * Sets the workingSet.
	 * 
	 * @param workingSet
	 *            The workingSet to set
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return ResourcesPlugin.getWorkspace().getRoot().getAdapter(adapter);
	}
}