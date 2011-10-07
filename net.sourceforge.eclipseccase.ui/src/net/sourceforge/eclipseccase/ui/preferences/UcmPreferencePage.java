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

		// Sets the layout data for the top composite's 
		// place in its parent's layout.
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		// Sets the layout for the top composite's 
		// children to populate.
		composite.setLayout(layout);

		useUcm = new BooleanFieldEditor(IClearCasePreferenceConstants.USE_UCM, PreferenceMessages.getString("UcmPreferences.UseUcm"), //$NON-NLS-1$
				composite);
		useUcm.fillIntoGrid(composite, 2);
		addFieldEditor(useUcm);
		Group group = new Group(composite,SWT.NULL);
		group.setText(PreferenceMessages.getString("UcmPreferences.group.preventCheckout"));
		group.setBounds(25,150,150,125);
		preventCheckout = new StringFieldEditor(IClearCasePreferenceConstants.PREVENT_CHECKOUT, PreferenceMessages.getString("UcmPreferences.PreventCheckOut"),group);
		addFieldEditor(preventCheckout);
		silentPrevent = new BooleanFieldEditor(IClearCasePreferenceConstants.SILENT_PREVENT, PreferenceMessages.getString("UcmPreferences.SilentPrevent"),group);
		addFieldEditor(silentPrevent);
		
		Group group2 = new Group(composite,SWT.NULL);
		group2.setText("ActivityPattern");
		group2.setBounds(25,150,150,125);
		activityPattern = new StringFieldEditor(IClearCasePreferenceConstants.ACTIVITY_PATTERN, PreferenceMessages.getString("UcmPreferences.activityPattern"),group2);
		addFieldEditor(activityPattern);		
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
