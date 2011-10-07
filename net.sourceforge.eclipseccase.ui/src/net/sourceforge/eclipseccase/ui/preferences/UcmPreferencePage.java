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
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Sets the layout for the top composite's
		// children to populate.
		composite.setLayout(new GridLayout());

		useUcm = new BooleanFieldEditor(IClearCasePreferenceConstants.USE_UCM, PreferenceMessages.getString("UcmPreferences.UseUcm"), //$NON-NLS-1$
				composite);
		addFieldEditor(useUcm);
		
		
		preventCheckout = new StringFieldEditor(IClearCasePreferenceConstants.PREVENT_CHECKOUT, PreferenceMessages.getString("UcmPreferences.PreventCheckOut"),composite);
		addFieldEditor(preventCheckout);
		silentPrevent = new BooleanFieldEditor(IClearCasePreferenceConstants.SILENT_PREVENT, PreferenceMessages.getString("UcmPreferences.SilentPrevent"),composite);
		addFieldEditor(silentPrevent);
		
		// getFieldEditorParent(SOURCE_MANAGEMENT)));
		// Label listLabel = new Label(top, SWT.NONE);
		// listLabel.setText("Tags which do not require closing tags:");

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
