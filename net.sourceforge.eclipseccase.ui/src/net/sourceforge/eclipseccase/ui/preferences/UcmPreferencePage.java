/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Maik Schreiber - initial API and implementation
 *    Mikael Petterson adaptation.
 *******************************************************************************/

package net.sourceforge.eclipseccase.ui.preferences;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.layout.FillLayout;

import net.sourceforge.eclipseccase.ui.DialogArea;

import org.eclipse.swt.widgets.Group;

import org.eclipse.jface.preference.FieldEditor;

import org.eclipse.jface.preference.StringFieldEditor;

import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UcmPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private static final int WIDTH_HINT = 350;

	private static final int HEIGHT_HINT = 150;
	
	private BooleanFieldEditor useUcm;
	private BooleanFieldEditor silentPrevent;
	private StringFieldEditor preventCheckout;
	private StringFieldEditor activityPattern;

	/**
	 * Creates a new instance.
	 */
	public UcmPreferencePage() {
		setDescription(PreferenceMessages.getString("UcmPreferences.Description")); //$NON-NLS-1$

		// Set the preference store for the preference page.
		setPreferenceStore(new ClearCasePreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.LEFT);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);
		
		useUcm = new BooleanFieldEditor(IClearCasePreferenceConstants.USE_UCM, PreferenceMessages.getString("UcmPreferences.UseUcm"), //$NON-NLS-1$
				composite);
		addFieldEditor(useUcm);
		
		Group group = new Group(composite,SWT.NULL);
		group.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		group.setLayoutData(gridData);
		group.setText(PreferenceMessages.getString("UcmPreferences.group.preventCheckout"));
		group.setBounds(25,150,150,125);
		preventCheckout = new StringFieldEditor(IClearCasePreferenceConstants.PREVENT_CHECKOUT, PreferenceMessages.getString("UcmPreferences.PreventCheckOut"),group);
		addFieldEditor(preventCheckout);
		silentPrevent = new BooleanFieldEditor(IClearCasePreferenceConstants.SILENT_PREVENT, PreferenceMessages.getString("UcmPreferences.SilentPrevent"),group);
		addFieldEditor(silentPrevent);
//		Group activity =new Group(composite , SWT.NULL);
//		activity.setLayout(gridLayout);
//		activity.setLayoutData(gridData);
		
		Group activity =new Group(composite , SWT.NULL);
		activity.setLayout(gridLayout);
		activity.setLayoutData(gridData);
		activity.setText("Activity ID");
		
		Label activityPaternLabel = new Label(activity, SWT.NULL);
		activityPaternLabel.setText(PreferenceMessages.getString("UcmPreferences.label.activityPattern"));
		// Create a single line text field with no label.
	    Text text = new Text(activity, SWT.FILL);
	    //text.setSize(width, height)
	    
		
		//activityPattern = new StringFieldEditor(IClearCasePreferenceConstants.ACTIVITY_PATTERN, "",activity);
	    //addFieldEditor(activityPattern);
		
		
		//Create activity pattern text area.
		Label label = new Label(activity, SWT.NULL);
		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));
		label.setText(PreferenceMessages.getString("UcmPreferences.activityPattern"));

		
		
		Text activityFormatText = new Text(activity, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		GridData gridData2 = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
//		gridData2.widthHint = WIDTH_HINT;
//		gridData2.heightHint = HEIGHT_HINT;
		activityFormatText.setText(PreferenceMessages.getString("UcmPreferences.activityPattern"));
		//activityFormatText.setLayoutData(gridData2);
		activityFormatText.selectAll();
		
		return composite;
	}

	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub

	}
	
	
	private void addFieldEditor(FieldEditor fieldEditor){
		
		fieldEditor.setPreferencePage(this);
		fieldEditor.setPreferenceStore(getPreferenceStore());
		fieldEditor.load();
	}

}
