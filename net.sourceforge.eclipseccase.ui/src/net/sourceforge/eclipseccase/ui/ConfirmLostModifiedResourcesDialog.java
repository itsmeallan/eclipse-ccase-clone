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

import java.util.Arrays;

import net.sourceforge.eclipseccase.ui.viewsupport.ListContentProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * This dialog displays a list of <code>IResource</code>s and asks the user
 * to confirm loosing all changed to them.
 * <p>
 * This concrete dialog class can be instantiated as is. It is not intended to
 * be subclassed.
 * </p>
 */
public class ConfirmLostModifiedResourcesDialog extends MessageDialog {

	// String constants for widgets
	private static String TITLE = "Modified Resources";

	private static String MESSAGE = "All changes to the following resources will be lost if you proceed.";

	private TableViewer fList;

	private IResource[] fModifiedResources;

	private WorkbenchLabelProvider workbenchLabelProvider;

	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell
	 * @param dialogTitle
	 * @param dialogTitleImage
	 * @param dialogMessage
	 * @param dialogImageType
	 * @param dialogButtonLabels
	 * @param defaultIndex
	 * @param modifiedResources
	 */
	protected ConfirmLostModifiedResourcesDialog(Shell parentShell,
			String dialogTitle, String dialogMessage,
			IResource[] modifiedResources) {
		super(parentShell, dialogTitle, null, dialogMessage, WARNING,
				new String[] { IDialogConstants.OK_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		fModifiedResources = modifiedResources;
	}

	public ConfirmLostModifiedResourcesDialog(Shell parentShell,
			IResource[] modifiedResources) {
		this(parentShell, TITLE, MESSAGE, modifiedResources);
	}

	protected Control createCustomArea(Composite parent) {
		fList = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		fList.setContentProvider(new ListContentProvider());
		workbenchLabelProvider = new WorkbenchLabelProvider();
		fList.setLabelProvider(workbenchLabelProvider);
		fList.setInput(Arrays.asList(fModifiedResources));
		Control control = fList.getControl();
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = convertWidthInCharsToPixels(20);
		data.heightHint = convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
		applyDialogFont(control);
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	public boolean close() {
		if (null != workbenchLabelProvider) {
			workbenchLabelProvider.dispose();
			workbenchLabelProvider = null;
		}
		return super.close();
	}

}
