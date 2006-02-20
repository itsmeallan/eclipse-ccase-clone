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
package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.StateCacheFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Simple action to cancel all pending state refreshes.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
public class CancelPendingRefreshesAction extends ActionDelegate implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {

	public CancelPendingRefreshesAction() {
		super();
	}

	public void dispose() {
		shell = null;
	}

	Shell shell;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.shell = window.getShell();
	}

	public void run(IAction action) {
		if (MessageDialog.openQuestion(shell, "Clearcase Plugin", "Do you want to cancel all pending state refreshes?")) {
			StateCacheFactory.getInstance().cancelPendingRefreshes();
			if (action != null)
				action.setEnabled(false);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (action != null)
			action.setEnabled(null != shell && ClearcasePlugin.getInstance().hasPendingRefreshes());
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.shell = targetPart.getSite().getShell();
		if (action != null && shell == null)
			action.setEnabled(false);
	}

}