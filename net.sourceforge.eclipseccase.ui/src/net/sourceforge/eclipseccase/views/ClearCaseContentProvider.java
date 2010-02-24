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

import org.eclipse.jface.viewers.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 * A content provider for the ClearCaseViewPart.
 * 
 * @author Gunnar Wagenknecht (gunnar@wagenknecht.org)
 */
public class ClearCaseContentProvider implements ITreeContentProvider {
	DeferredTreeContentManager manager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof AbstractTreeViewer) {
			manager = new DeferredTreeContentManager(this, (AbstractTreeViewer) viewer);
		}
	}

	public boolean hasChildren(Object element) {
		// the + box will always appear, but then disappear
		// if not needed after you first click on it.
		if (element instanceof ClearCaseViewRoot)
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	public Object[] getChildren(Object element) {
		if (element instanceof ClearCaseViewRoot) {
			((ClearCaseViewRoot) element).setWorkingSet(getWorkingSet());
			if (manager != null) {
				Object[] children = manager.getChildren(element);
				if (children != null)
					// This will be a placeholder to indicate
					// that the real children are being fetched
					return children;
			}
		}
		return new Object[0];
	}

	/**
	 * Cancels all pending jobs for the specified root.
	 * 
	 * @param root
	 */
	public void cancelJobs(ClearCaseViewRoot root) {
		if (manager != null) {
			manager.cancel(root);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
	 * )
	 */
	public Object getParent(Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// nothing to dispose
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

}