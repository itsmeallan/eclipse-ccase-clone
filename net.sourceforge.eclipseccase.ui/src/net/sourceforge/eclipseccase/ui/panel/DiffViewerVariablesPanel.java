/*******************************************************************************
a * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package net.sourceforge.eclipseccase.ui.panel;

import net.sourceforge.eclipseccase.ui.utility.SwtHelper;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


/**
 * Pre-defined variables selection panel for diff viewer's external program
 * 
 * @author Igor Burilo
 */
public class DiffViewerVariablesPanel extends AbstractDialogPanel {

	protected List variablesList;

	protected Text variableDescription;

	protected Map<String, String> variablesMap = new LinkedHashMap<String, String>();

	protected String variable;

	public DiffViewerVariablesPanel() {
		this.dialogTitle = Messages.getString("DiffViewerVariablesPanel_DialogTitle");
		this.dialogDescription = Messages.getString("DiffViewerVariablesPanel_DialogDescription");
		this.defaultMessage = Messages.getString("DiffViewerVariablesPanel_DialogDefaultMessage");

		this.variablesMap.put("base", Messages.getString("DiffViewerVariablesPanel_BaseVariable_Description")); //$NON-NLS-1$
		this.variablesMap.put("mine", Messages.getString("SVNUIMessages.DiffViewerVariablesPanel_MineVariable_Description")); //$NON-NLS-1$
		this.variablesMap.put("theirs", Messages.getString("SVNUIMessages.DiffViewerVariablesPanel_TheirsVariable_Description")); //$NON-NLS-1$
		this.variablesMap.put("merged", Messages.getString("DiffViewerVariablesPanel_MergedVariable_Description")); //$NON-NLS-1$

		this.variablesMap.put("default-doc-program", Messages.bind(Messages.getString("DiffViewerVariablesPanel_DefaultVariable_Description"), Messages.getString("DiffViewerVariablesPanel_DefaultDocVariable_Program")));
		this.variablesMap.put("default-xls-program", Messages.bind(Messages.getString("DiffViewerVariablesPanel_DefaultVariable_Description"), Messages.getString("DiffViewerVariablesPanel_DefaultDocVariable_Program")));
		this.variablesMap.put("default-ppt-program", Messages.bind(Messages.getString("DiffViewerVariablesPanel_DefaultVariable_Description"), Messages.getString("DiffViewerVariablesPanel_DefaultDocVariable_Program")));

		this.variablesMap.put("default-odt-program", Messages.bind(Messages.getString("DiffViewerVariablesPanel_DefaultVariable_Description"), Messages.getString("DiffViewerVariablesPanel_DefaultDocVariable_Program")));
		this.variablesMap.put("default-ods-program", Messages.bind(Messages.getString("DiffViewerVariablesPanel_DefaultVariable_Description"), Messages.getString("DiffViewerVariablesPanel_DefaultDocVariable_Program")));

	}

	protected void createControlsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayout(layout);
		composite.setLayoutData(data);

		this.variablesList = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SwtHelper.convertHeightInCharsToPixels(this.variablesList, 10);
		this.variablesList.setLayoutData(data);

		Label variableDescriptionLabel = new Label(composite, SWT.NONE);
		data = new GridData();
		variableDescriptionLabel.setLayoutData(data);
		variableDescriptionLabel.setText("DiffViewerVariablesPanel_VariableDescriptionLabel");

		this.variableDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = SwtHelper.convertHeightInCharsToPixels(this.variableDescription, 4);
		this.variableDescription.setLayoutData(data);
		this.variableDescription.setBackground(variableDescription.getBackground());
		this.variableDescription.setEditable(false);

		// handlers

		this.variablesList.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String variableName = DiffViewerVariablesPanel.this.getSelectedVariable();
				if (variableName != null) {
					// init description
					DiffViewerVariablesPanel.this.variableDescription.setText(DiffViewerVariablesPanel.this.variablesMap.get(variableName));

					// run validation
					DiffViewerVariablesPanel.this.validateContent();
				}
			}
		});

		this.initializeControls();
	}

	protected String getSelectedVariable() {
		String variable = null;
		String[] selected = DiffViewerVariablesPanel.this.variablesList.getSelection();
		if (selected.length > 0) {
			variable = selected[0];
		}
		return variable;
	}

	protected void initializeControls() {
		String firstVariable = null;
		for (String variableName : this.variablesMap.keySet()) {
			this.variablesList.add(variableName);
			if (firstVariable == null) {
				firstVariable = variableName;
			}
		}
		this.variablesList.select(0);
		this.variableDescription.setText(this.variablesMap.get(firstVariable));
	}

	protected void saveChangesImpl() {
		String var = this.getSelectedVariable();
		this.variable = var != null ? ("\"${" + var + "}\"") : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected void cancelChangesImpl() {
		this.variable = null;
	}

	public String getVariable() {
		return this.variable;
	}

}
