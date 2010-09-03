/*******************************************************************************
 * Copyright (c) 2010 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.action.IAction;

/**
 * @author mike
 * 
 */
public class CheckoutToReservedAction extends ClearCaseWorkspaceAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void execute(IAction action) {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IResource[] resources = getSelectedResources();
					beginTask(monitor, "Changing checkout state to reserved...", resources.length);
					ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
					ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resources[0]);
					if (provider != null) {

						for (int i = 0; i < resources.length; i++) {
							IResource resource = resources[i];
							provider.setOperationListener(opListener);
							provider.reserved(new IResource[] { resource }, 0, subMonitor(monitor));

						}

					}
				} finally {
					updateActionEnablement();
					monitor.done();
				}
			}

		};

		executeInBackground(runnable, "Changing checkout state to reserved");
	}

	private static final String DEBUG_ID = "CheckoutToReservedAction";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource))
				return false;
			if (!provider.isCheckedOut(resource))
				return false;
		}
		return true;

	}
}
