/*******************************************************************************
 * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.preference.FieldEditor;

/**
 * @author mikael petterson
 * 
 */
public class TextAreaFieldEditor extends FieldEditor {

	private static final int WIDTH_HINT = 350;

	private static final int HEIGHT_HINT = 150;

	// The top-level control for the field editor.
	private Composite composite;

	private Text activityFormatText;

	public TextAreaFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		// Set the preference store for the preference page.
		setPreferenceStore(new ClearCasePreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) composite.getLayoutData()).horizontalSpan = numColumns;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt
	 * .widgets.Composite, int)
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		composite = parent;

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		composite.setLayoutData(gd);

		Label label = getLabelControl(composite);
		GridData labelData = new GridData();
		labelData.horizontalSpan = numColumns;
		label.setLayoutData(labelData);

		activityFormatText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		activityFormatText.setBounds(25, 150, 150, 125);
		GridData gridData2 = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		gridData2.widthHint = WIDTH_HINT;
		gridData2.heightHint = HEIGHT_HINT;
		activityFormatText.setLayoutData(gridData2);
		activityFormatText.selectAll();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		String msg = getPreferenceStore().getString(getPreferenceName());
		activityFormatText.setText(msg);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		String msg = getPreferenceStore().getDefaultString(getPreferenceName());
		activityFormatText.setText(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		String msg = activityFormatText.getText();
		if (msg != null)
			getPreferenceStore().setValue(getPreferenceName(), msg);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		return 1;
	}

}
