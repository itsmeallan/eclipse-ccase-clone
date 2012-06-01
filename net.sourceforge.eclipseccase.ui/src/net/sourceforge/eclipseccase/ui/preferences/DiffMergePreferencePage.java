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

import net.sourceforge.eclipseccase.ClearCasePreferences;

import java.util.Collections;

import java.text.Collator;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.jface.viewers.ISelectionChangedListener;

import net.sourceforge.eclipseccase.ui.provider.ToolListLabelProvider;

import net.sourceforge.eclipseccase.ui.provider.ActivityListLabelProvider;

import org.eclipse.jface.viewers.ArrayContentProvider;

import org.eclipse.jface.viewers.ComboViewer;

import org.eclipse.swt.widgets.Combo;

import org.eclipse.jface.preference.FieldEditor;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;

import org.eclipse.jface.preference.BooleanFieldEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.Map;

import java.util.HashMap;

import org.eclipse.jface.util.PropertyChangeEvent;

import java.util.StringTokenizer;
import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
import org.eclipse.jface.preference.*;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author eraonel
 *
 */
/**
 * The main preference page for the Eclipse ClearCase integration.
 */
public class DiffMergePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IClearCasePreferenceConstants {

	//	private static final String GENERAL = PreferenceMessages.getString("DiffMergePreferencePage.Category.General"); //$NON-NLS-1$
	//
	//	private static final String DIFF = PreferenceMessages.getString("DiffMergePreferencePage.Category.Diff"); //$NON-NLS-1$
	//
	//	private static final String MERGE = PreferenceMessages.getString("DiffMergePreferencePage.Category.Merge"); //$NON-NLS-1$
	//
	// private static final String[] CATEGORIES = new String[] { GENERAL, DIFF,
	// MERGE };

	private StringFieldEditor diffExecPath;

	private ComboFieldEditor diff;

	private static final String TOOL_DELIMITER = ";";

	private static final String PATH_DELIMITER = ":";

	private String selectedTool = "";// Initial value.

	private BooleanFieldEditor useExternal;

	private ComboViewer comboViewer;

	private TextAreaFieldEditor pathTextArea;

	private Text pathText;

	private static final int SPAN = 1;

	private static final String EMPTY_STR = "";

	private static final String[] tools = new String[] { "kdiff3", "ibm" };

	/**
	 * Creates a new instance.
	 */
	public DiffMergePreferencePage() {
		setDescription(PreferenceMessages.getString("DiffMergePreferencePage.Description")); //$NON-NLS-1$

		// Set the preference store for the preference page.
		setPreferenceStore(new ClearCasePreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		useExternal = new BooleanFieldEditor(COMPARE_EXTERNAL, PreferenceMessages.getString("Preferences.General.CompareWithExternalTool"), //$NON-NLS-1$
				composite);
		addFieldEditor(useExternal);
		// TODO Auto-generated method stub
		Group groupDiff = new Group(composite, SWT.NULL);
		GridLayout layoutDiff = new GridLayout();
		layoutDiff.numColumns = 1;
		groupDiff.setLayout(layoutDiff);
		GridData dataDiff = new GridData();
		dataDiff.horizontalAlignment = GridData.FILL;
		groupDiff.setLayoutData(dataDiff);
		groupDiff.setText("External Diff tool settings:");

		comboViewer = new ComboViewer(groupDiff);
		Combo combo = comboViewer.getCombo();
        combo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		comboViewer.setContentProvider(new IStructuredContentProvider() {
			String[] vals;

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				vals = (String[]) newInput;
			}

			public Object[] getElements(Object inputElement) {
				return vals;
			}

		});

		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				ISelection selection = evt.getSelection();
				if (selection instanceof StructuredSelection) {
					StructuredSelection sel = (StructuredSelection) selection;
					if (!selection.isEmpty())
						selectedTool = sel.getFirstElement().toString();
				}
			}
		});

		initializeValues();

		// comboViewer = createComboViewer(groupDiff);
		// // select first,
		// comboViewer.setSelection(new StructuredSelection(), true);
		//
		// comboViewer.addSelectionChangedListener(new
		// ISelectionChangedListener() {
		// public void selectionChanged(SelectionChangedEvent event) {
		// IStructuredSelection selection = (IStructuredSelection)
		// event.getSelection();
		// System.out.println("combo changed!" + selection.getFirstElement());
		// setSelectedTool((String) (selection.getFirstElement()));
		// //set the matching path
		// String path = getPath(getSelectedTool());
		// diffExecPath.setStringValue(path);
		// }
		// });

		// kidff3 is default
		//		diffExecPath = new StringFieldEditor(EXTERNAL_DIFF_TOOL_EXEC_PATH, PreferenceMessages.getString("DiffMergePreferencePage.External.Diff.Tool.ExecPath"), groupDiff); //$NON-NLS-1$
		// String path = getPath(tools[0]);
		//
		// //If no path stored for kdiff3
		// if (!path.equals(EMPTY_STR)) {
		// diffExecPath.setStringValue(path);
		// }
		// addFieldEditor(diffExecPath);

		Group groupMerge = new Group(composite, SWT.NULL);
		GridLayout layoutMerge = new GridLayout();
		layoutMerge.numColumns = 3;
		groupMerge.setLayout(layoutMerge);
		GridData dataMerge = new GridData();
		dataMerge.horizontalAlignment = GridData.FILL;
		groupMerge.setLayoutData(dataMerge);
		groupMerge.setText("External Merge tool settings:");

		return parent;
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

	/**
	 * Initializes values for non-FieldEditors.
	 */
	private void initializeValues() {
		final IPreferenceStore store = getPreferenceStore();
		
		selectedTool = store.getString(IClearCasePreferenceConstants.EXTERNAL_DIFF_TOOL);
		ArrayList<String> tools = new ArrayList<String>();
		tools.add("IBM");
		tools.add("Kdiff3");
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.PRIMARY);
		Collections.sort(tools, collator);
		comboViewer.setInput((String[]) tools.toArray(new String[tools.size()]));
		comboViewer.reveal(selectedTool);

		// TODO: not sure if needed.
		
		//comboViewer.getCombo().setEnabled(false);
				
		comboViewer.setSelection(new StructuredSelection(selectedTool), true);

	}

	
	// Needs to be done for each fieldeditor.
	private void addFieldEditor(FieldEditor fieldEditor) {

		fieldEditor.setPreferencePage(this);
		fieldEditor.setPreferenceStore(getPreferenceStore());
		fieldEditor.load();
	}

	//This should be called when we have chnaged selectedTool.
	private void updateTextValue(){
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (selectedTool != null)
			getPreferenceStore().setValue(IClearCasePreferenceConstants.EXTERNAL_DIFF_TOOL, selectedTool);
		if (super.performOk()) {
			ClearCasePlugin.getDefault().resetClearCase();
			return true;
		}
		return false;
	}



}
