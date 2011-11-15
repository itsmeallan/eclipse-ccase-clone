/*******************************************************************************
 * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     mikael - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.preferences;

import org.eclipse.swt.widgets.Group;

import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.layout.GridLayout;

import org.eclipse.ui.IWorkbench;

import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.preference.PreferencePage;

/**
 * @author mikael
 *
 */
public class ExtDiffViewerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage{
	
	protected String groupLabel;
	
	public ExtDiffViewerPreferencePage(){
		setDescription(PreferenceMessages.getString("ExtDiffViewerPreference.Description")); //$NON-NLS-1$
		// Set the preference store for the preference page.
		setPreferenceStore(new ClearCasePreferenceStore());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(layout);
		composite.setLayoutData(data);
		
		Group parametersGroup = new Group(composite, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 1;
		data = new GridData(GridData.FILL_HORIZONTAL);
		parametersGroup.setLayout(layout);
		parametersGroup.setLayoutData(data);
		parametersGroup.setText("Diff/Merge Viewer");
		return composite;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

}
