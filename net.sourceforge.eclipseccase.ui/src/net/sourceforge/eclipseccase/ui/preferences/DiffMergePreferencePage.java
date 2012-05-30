/*******************************************************************************
 * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
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

import org.eclipse.jface.util.IPropertyChangeListener;

import java.util.StringTokenizer;

import net.sourceforge.eclipseccase.ClearCasePreferences;

import org.eclipse.jface.preference.FieldEditor;

import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.preference.ComboFieldEditor;

import org.eclipse.swt.custom.CCombo;

import java.util.ArrayList;
import java.util.Iterator;
import net.sourceforge.eclipseccase.ui.preferences.UcmPreferencePage.StringPair;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.sourceforge.eclipseccase.ui.preferences.UcmPreferencePage.TextPair;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;

import net.sourceforge.clearcase.commandline.CommandLauncher;

import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.swt.events.SelectionAdapter;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Button;

import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author eraonel
 *
 */
/**
 * The main preference page for the Eclipse ClearCase integration.
 */
public class DiffMergePreferencePage extends FieldEditorPreferencePageWithCategories implements IWorkbenchPreferencePage, IClearCasePreferenceConstants {

	private static final String GENERAL = PreferenceMessages.getString("DiffMergePreferencePage.Category.General"); //$NON-NLS-1$

	private static final String DIFF = PreferenceMessages.getString("DiffMergePreferencePage.Category.Diff"); //$NON-NLS-1$

	private static final String MERGE = PreferenceMessages.getString("DiffMergePreferencePage.Category.Merge"); //$NON-NLS-1$

	private static final String[] CATEGORIES = new String[] { GENERAL, DIFF, MERGE };

	private StringFieldEditor diffExecPath;

	private ComboFieldEditor diff;

	private static final String TOOL_DELIMITER = ";";

	private static final String PATH_DELIMITER = ":";

	/**
	 * Creates a new instance.
	 */
	public DiffMergePreferencePage() {
		setDescription(PreferenceMessages.getString("DiffMergePreferencePage.Description")); //$NON-NLS-1$

		// Set the preference store for the preference page.
		setPreferenceStore(new ClearCasePreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {

		// general settings

		addField(new BooleanFieldEditor(COMPARE_EXTERNAL, PreferenceMessages.getString("Preferences.General.CompareWithExternalTool"), //$NON-NLS-1$
				getFieldEditorParent(GENERAL)));
		// diff

		// TODO: Create a diff table here.!!

		String[][] nameValues = new String[][] { { "Kdiff3", "kdiff3" }, { "IBM", "clearcase" } };

		diff = new ComboFieldEditor(EXTERNAL_DIFF_TOOL, PreferenceMessages.getString("DiffMergePreferencePage.External.Diff.Tool"), nameValues, getFieldEditorParent(DIFF));
		diff.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				System.out.print(event.getNewValue());
			}

		});

		addField(diff);

		diffExecPath = new StringFieldEditor(EXTERNAL_DIFF_TOOL_EXEC_PATH, PreferenceMessages.getString("DiffMergePreferencePage.External.Diff.Tool.ExecPath"), //$NON-NLS-1$
				getFieldEditorParent(DIFF));

		// general settings
		addField(diffExecPath);
	}

	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() == diff) {
			System.out.println("Diff ");
			String path = getMatchingPath(diff.getPreferenceStore().getString(EXTERNAL_DIFF_TOOL));
			System.out.println("Path " + path);
			if (!path.equals("")) {
				diffExecPath.setStringValue(path);
			} else {
				diffExecPath.setFocus();
			}
		}

	}

	/**
	 * Converted the stored preference string value.
	 * 
	 * @param preferenceValue
	 * @return
	 */
	public String[][] fromStringToArray(String preferenceValue) {
		StringTokenizer tokenizer = new StringTokenizer(preferenceValue, TOOL_DELIMITER);
		int tokenCount = tokenizer.countTokens();
		// create array of kdiff3:path/to/kdiff3
		String[][] myArray = new String[tokenCount][2];
		String[] elements = new String[tokenCount];
		for (int i = 0; i < tokenCount; i++) {
			elements[i] = tokenizer.nextToken();
			String[] values = getNamePath(elements[i]);
			for (int j = 0; j < values.length; j++) {
				myArray[i][j] = values[j];
			}
		}

		return myArray;

	}

	private String[] getNamePath(String element) {
		return element.split(PATH_DELIMITER);

	}

	private String getMatchingPath(String toolValue) {
		String result = "";
		String[][] nameValue = fromStringToArray(diffExecPath.getStringValue());

		for (int i = 0; i < nameValue.length; i++) {
			for (int j = 0; j < nameValue[i].length; j++) {
				if (nameValue[i][0].equals(toolValue))
					result = nameValue[i][1];
			}
		}

		return result;
	}

	private String fromArrayToString() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {

		if (super.performOk()) {
			ClearCasePlugin.getDefault().resetClearCase();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.eclipseccase.ui.preferences.TabFieldEditorPreferencePage
	 * #getCategories()
	 */
	@Override
	protected String[] getCategories() {
		return CATEGORIES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sourceforge.eclipseccase.ui.preferences.
	 * FieldEditorPreferencePageWithCategories#getDescription(java.lang.String)
	 */
	@Override
	protected String getDescription(String category) {
		// if (GENERAL.equals(category))
		//			return PreferenceMessages.getString("Preferences.Description.Category.General"); //$NON-NLS-1$
		// if (SOURCE_MANAGEMENT.equals(category))
		//			return PreferenceMessages.getString("Preferences.Description.Category.Source"); //$NON-NLS-1$
		// if (COMMENTS.equals(category))
		//			return PreferenceMessages.getString("Preferences.Description.Category.Comments"); //$NON-NLS-1$
		return null;
	}

}
