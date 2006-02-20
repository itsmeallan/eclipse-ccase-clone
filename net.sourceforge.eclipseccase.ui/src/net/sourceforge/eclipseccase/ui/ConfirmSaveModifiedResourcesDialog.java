/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - adaption for eclipse-ccase
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;

/**
 * This dialog displays a list of <code>IFile</code> and asks the user to
 * confirm saving all of them.
 * <p>
 * This concrete dialog class can be instantiated as is. It is not intended to
 * be subclassed.
 * </p>
 */
public class ConfirmSaveModifiedResourcesDialog extends ConfirmLostModifiedResourcesDialog {

	// String constants for widgets
	private static String TITLE = "Modified Resources";

	private static String MESSAGE = "The following files must be saved in order to proceed.";

	public ConfirmSaveModifiedResourcesDialog(Shell parentShell, IFile[] unsavedFiles) {
		super(parentShell, TITLE, MESSAGE, unsavedFiles);
	}
}
