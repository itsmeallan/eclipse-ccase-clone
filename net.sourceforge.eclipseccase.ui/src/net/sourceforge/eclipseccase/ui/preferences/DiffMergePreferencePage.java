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

	private String selectedValue;

	private BooleanFieldEditor useExternal;

	private ComboViewer comboViewer;

	private TextAreaFieldEditor pathTextArea;

	private Text pathText;

	private static final int SPAN = 1;

	private static final String EMPTY_STR = "";

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
		layoutDiff.numColumns = 3;
		groupDiff.setLayout(layoutDiff);
		GridData dataDiff = new GridData();
		dataDiff.horizontalAlignment = GridData.FILL;
		groupDiff.setLayoutData(dataDiff);
		groupDiff.setText("External Diff tool settings:");

		String[] tools = new String[] { "kdiff3", "ibm" };
		comboViewer = createComboViewer(groupDiff, tools);
		// select first,
		comboViewer.setSelection(new StructuredSelection(tools[0]), true);

		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				System.out.println("combo changed!" + selection.getFirstElement());
				// setSelectedTool((String) (selection.getFirstElement()));
			}
		});
		// kidff3 is default
		String path = getPath(tools[0]);

		diffExecPath = new StringFieldEditor(EXTERNAL_DIFF_TOOL_EXEC_PATH, PreferenceMessages.getString("DiffMergePreferencePage.External.Diff.Tool.ExecPath"), composite); //$NON-NLS-1$
		if (!path.equals(EMPTY_STR)) {
			diffExecPath.setStringValue(path);
		}
		addFieldEditor(diffExecPath);
		//		createLabel(groupDiff, PreferenceMessages.getString("DiffMergePreferencePage.diff.exec.path"), SPAN); //$NON-NLS-1$
		// pathText = new Text(groupDiff, SWT.BORDER);
		// pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group groupMerge = new Group(composite, SWT.NULL);
		GridLayout layoutMerge = new GridLayout();
		layoutMerge.numColumns = 3;
		groupMerge.setLayout(layoutMerge);
		GridData dataMerge = new GridData();
		dataMerge.horizontalAlignment = GridData.FILL;
		groupMerge.setLayoutData(dataMerge);
		groupMerge.setText("External Merge tool settings:");

		return null;
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

	protected ComboViewer createComboViewer(Composite composite, String[] elements) {
		ComboViewer comboViewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboViewer.setLabelProvider(new ToolListLabelProvider());
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setInput(elements);
		// comboViewer.setSorter(new ViewerSorter() {
		// @Override
		// public int compare(Viewer viewer, Object p1, Object p2) {
		// return ((Activity) p1).getHeadline().compareToIgnoreCase(((Activity)
		// p2).getHeadline());
		// }
		//
		// });

		return comboViewer;
	}

	// Needs to be done for each fieldeditor.
	private void addFieldEditor(FieldEditor fieldEditor) {

		fieldEditor.setPreferencePage(this);
		fieldEditor.setPreferenceStore(getPreferenceStore());
		fieldEditor.load();
	}

	/**
	 * creates a label
	 */
	private Label createLabel(Composite parent, String text, int span) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	// @Override
	// protected void createFieldEditors() {
	//
	// // TODO: Create a diff table here.!!
	//
	// String[][] nameValues = new String[][] { { "Kdiff3", "kdiff3" }, { "IBM",
	// "clearcase" } };
	//
	// diff = new ComboFieldEditor(EXTERNAL_DIFF_TOOL,
	// PreferenceMessages.getString("DiffMergePreferencePage.External.Diff.Tool"),
	// nameValues, getFieldEditorParent(DIFF));
	//
	// addField(diff);
	//
	//		diffExecPath = new StringFieldEditor(EXTERNAL_DIFF_TOOL_EXEC_PATH, PreferenceMessages.getString("DiffMergePreferencePage.External.Diff.Tool.ExecPath"), //$NON-NLS-1$
	// getFieldEditorParent(DIFF));
	//
	// // general settings
	// addField(diffExecPath);
	// }

	// @Override
	// public void propertyChange(PropertyChangeEvent event) {
	// super.propertyChange(event);
	// if (event.getSource() == diff) {
	// // clearcase or kdiff3
	// System.out.println("New value " + event.getNewValue());
	// selectedValue = (String) event.getNewValue();
	// String path = getPath(selectedValue);
	//
	// System.out.println("Path " + path);
	// if (!path.equals("")) {
	// diffExecPath.setStringValue(path);
	// } else {
	// diffExecPath.setFocus();
	// }
	// }
	//
	// }

	/**
	 * Convert the stored preference string value to a HashMap.
	 * 
	 * @param preferenceValue
	 * @return
	 */
	public Map<String, String> stringToMap(String preferenceValue) {
		StringTokenizer tokenizer = new StringTokenizer(preferenceValue, TOOL_DELIMITER);
		int tokenCount = tokenizer.countTokens();
		Map<String, String> keyValue = new HashMap<String, String>(tokenCount);
		// create array of kdiff3:path/to/kdiff3
		String[] elements = new String[tokenCount];
		for (int i = 0; i < tokenCount; i++) {
			elements[i] = tokenizer.nextToken();
			String[] nameValue = elements[i].split(PATH_DELIMITER);
			keyValue.put(nameValue[0], nameValue[1]);
		}

		return keyValue;

	}

	private String getPath(String toolValue) {
		String result = "";
		Map<String, String> nameValue = (HashMap<String, String>) stringToMap(diffExecPath.getStringValue());
		if (nameValue.containsKey(toolValue)) {
			result = nameValue.get(toolValue);
		}

		return result;
	}

	public void storePath(String tool, String path) {
		// set tool and path
		Map<String, String> nameValue = (HashMap<String, String>) stringToMap(diffExecPath.getStringValue());
		// update
		nameValue.put(tool, path);
		String pref = mapToString(nameValue);

		// store
		diffExecPath.getPreferenceStore().setValue(EXTERNAL_DIFF_TOOL_EXEC_PATH, pref);
	}

	private String mapToString(Map<String, String> myMap) {
		// more elegant way
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : myMap.entrySet()) {
			sb.append(entry.getKey());
			sb.append(PATH_DELIMITER);
			sb.append(entry.getValue());
			sb.append(TOOL_DELIMITER);

		}
		String str;
		// remove TOOL_DELIMITER if last char.
		if ((sb.length() - 1) == sb.lastIndexOf(TOOL_DELIMITER)) {
			str = sb.substring(0, sb.lastIndexOf(TOOL_DELIMITER) - 1);
		} else {
			str = sb.toString();
		}

		return str;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		storePath(selectedValue, diffExecPath.getStringValue());
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
	// @Override
	// protected String[] getCategories() {
	// return CATEGORIES;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @seenet.sourceforge.eclipseccase.ui.preferences.
	// *
	// FieldEditorPreferencePageWithCategories#getDescription(java.lang.String)
	// */
	// @Override
	// protected String getDescription(String category) {
	// // if (GENERAL.equals(category))
	//		//			return PreferenceMessages.getString("Preferences.Description.Category.General"); //$NON-NLS-1$
	// // if (SOURCE_MANAGEMENT.equals(category))
	//		//			return PreferenceMessages.getString("Preferences.Description.Category.Source"); //$NON-NLS-1$
	// // if (COMMENTS.equals(category))
	//		//			return PreferenceMessages.getString("Preferences.Description.Category.Comments"); //$NON-NLS-1$
	// return null;
	// }

}
