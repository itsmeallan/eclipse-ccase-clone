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

import org.eclipse.swt.events.ModifyEvent;

import org.eclipse.swt.widgets.Event;

import org.eclipse.swt.widgets.Listener;

import org.eclipse.swt.widgets.Button;

import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.events.ModifyListener;

import org.eclipse.jface.dialogs.IDialogConstants;

import java.util.HashMap;

import java.util.Map;

import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;

import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UcmPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor useUcm;

	private StringFieldEditor activityPattern;

	private StringFieldEditor activityIdFormatHelpString;

	private TextAreaFieldEditor activityId;
	
	private Text fileTextFormat;

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
		
		Group group = new Group(composite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout(1, false));
		group.setText("Test");
		
		TextPair format = createFormatEditorControl(group, 
	            "format",  //$NON-NLS-1$
	            "text", getFileBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
			fileTextFormat = format.t1;
			
			activityIdFormatHelpString = new StringFieldEditor(IClearCasePreferenceConstants.ACTIVITY_FORMAT_HELP_STRING, PreferenceMessages.getString("UcmPreferences.label.activityForHelpString"), composite);
			addFieldEditor(activityPattern);	
		
		activityPattern = new StringFieldEditor(IClearCasePreferenceConstants.ACTIVITY_PATTERN, PreferenceMessages.getString("UcmPreferences.label.activityPattern"), composite);
		addFieldEditor(activityPattern);

		
		

		activityId = new TextAreaFieldEditor(IClearCasePreferenceConstants.ACTIVITY_MSG_FORMAT, PreferenceMessages.getString("UcmPreferences.activityFormatMsg"), composite);
		addFieldEditor(activityId);

		return composite;
	}

	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub

	}

	// Needs to be done for each fieldeditor.
	private void addFieldEditor(FieldEditor fieldEditor) {

		fieldEditor.setPreferencePage(this);
		fieldEditor.setPreferenceStore(getPreferenceStore());
		fieldEditor.load();
	}

	@Override
	protected void performDefaults() {
		useUcm.loadDefault();
		activityIdFormatHelpString.loadDefault();
		activityPattern.loadDefault();
		activityId.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		useUcm.store();
		activityIdFormatHelpString.store();
		activityPattern.store();
		activityId.store();
		return super.performOk();
	}
	
	protected TextPair createFormatEditorControl(
	        Composite composite, 
	        String title, 
	        String buttonText, 
	        final Map supportedBindings) {
	        
	        createLabel(composite, title, 1);
			
	        Text format = new Text(composite, SWT.BORDER);
			format.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			format.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {				
					updateExamples();
				}
			});
			Button b = new Button(composite, SWT.NONE);
			b.setText(buttonText);
			GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
			data.widthHint = Math.max(widthHint, b.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
			b.setLayoutData(data);
			final Text formatToInsert = format;
			b.addListener(SWT.Selection, new Listener() {
				public void handleEvent (Event event) {
					addVariables(formatToInsert, supportedBindings);
				}			
			});
			
			return new TextPair(format, null);
		}
	/**
     * get the map of {variable,description} to use for files with createFormatEditorControl
     */     
    private Map getFileBindingDescriptions() {
		Map bindings = new HashMap();
//		bindings.put(SVNDecoratorConfiguration.RESOURCE_NAME, Policy.bind("SVNDecoratorPreferencesPage.nameResourceVariable")); //$NON-NLS-1$
//		bindings.put(SVNDecoratorConfiguration.RESOURCE_REVISION, Policy.bind("SVNDecoratorPreferencesPage.revisionResourceVariable")); //$NON-NLS-1$
//		bindings.put(SVNDecoratorConfiguration.DIRTY_FLAG, Policy.bind("SVNDecoratorPreferencesPage.flagDirtyVariable")); //$NON-NLS-1$
//		bindings.put(SVNDecoratorConfiguration.ADDED_FLAG, Policy.bind("SVNDecoratorPreferencesPage.flagAddedVariable")); //$NON-NLS-1$
//        bindings.put(SVNDecoratorConfiguration.RESOURCE_AUTHOR, Policy.bind("SVNDecoratorPreferencesPage.authorVariable")); //$NON-NLS-1$
//        bindings.put(SVNDecoratorConfiguration.RESOURCE_DATE, Policy.bind("SVNDecoratorPreferencesPage.dateVariable")); //$NON-NLS-1$                
		return bindings;
	}
	
	
	class TextPair {
		TextPair(Text t1, Text t2) {
			this.t1 = t1;
			this.t2 = t2;
		}
		Text t1;
		Text t2;
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
	
	/**
    * updates the examples
    */
	protected void updateExamples() {
     //  if (fPreview != null) fPreview.refresh();
	}
	
	
	/**
	 * Add another variable to the given target. The variable is inserted at current position
     * A ListSelectionDialog is shown and the choose the variables to add 
	 */
	private void addVariables(Text target, Map bindings) {
	
//		final List variables = new ArrayList(bindings.size());
//		
//		ILabelProvider labelProvider = new LabelProvider() {
//			public String getText(Object element) {
//				return ((StringPair)element).s1 + " - " + ((StringPair)element).s2; //$NON-NLS-1$
//			}
//		};
//		
//		IStructuredContentProvider contentsProvider = new IStructuredContentProvider() {
//			public Object[] getElements(Object inputElement) {
//				return variables.toArray(new StringPair[variables.size()]);
//			}
//			public void dispose() {}
//			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
//		};
//		
//		for (Iterator it = bindings.keySet().iterator(); it.hasNext();) {
//			StringPair variable = new StringPair();
//			variable.s1 = (String) it.next(); // variable
//			variable.s2 = (String) bindings.get(variable.s1); // description
//			variables.add(variable);				
//		}
//	
//		ListSelectionDialog dialog =
//			new ListSelectionDialog(
//				this.getShell(),
//				this,
//				contentsProvider,
//				labelProvider,
//				Policy.bind("SVNDecoratorPreferencesPage.selectVariablesToAdd")); //$NON-NLS-1$
//		dialog.setTitle(Policy.bind("SVNDecoratorPreferencesPage.AddVariables")); //$NON-NLS-1$
//		if (dialog.open() != ListSelectionDialog.OK)
//			return;
//	
//		Object[] result = dialog.getResult();
//		
//		for (int i = 0; i < result.length; i++) {
//			target.insert("{"+((StringPair)result[i]).s1 +"}"); //$NON-NLS-1$ //$NON-NLS-2$
//		}		
	}

}
