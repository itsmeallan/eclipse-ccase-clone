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



import java.util.*;
import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class UcmPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor useUcm;

	private StringFieldEditor activityPattern;

	// private StringFieldEditor activityIdFormatHelpString;

	private TextAreaFieldEditor activityId;

	private Text activityPatternText;

	private Text activityIdHelpTextFormat;

	private static final int SPAN = 1;

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

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		useUcm = new BooleanFieldEditor(IClearCasePreferenceConstants.USE_UCM, PreferenceMessages.getString("UcmPreferences.UseUcm"), //$NON-NLS-1$
				composite);
		addFieldEditor(useUcm);

		Group group = new Group(composite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		group.setLayoutData(data);
		group.setText("Activity ID Format:");

		TextPair format = createFormatEditorControl(group, PreferenceMessages.getString("UcmPreferences.label.activityForHelpString"), //$NON-NLS-1$
				PreferenceMessages.getString("UcmPreferences.button.addVariables"), getActivityIdBindingDescriptions()); //$NON-NLS-1$ 
		activityIdHelpTextFormat = format.t1;

		createLabel(group, PreferenceMessages.getString("UcmPreferences.label.activityPattern"), SPAN); //$NON-NLS-1$
		activityPatternText = new Text(group, SWT.BORDER);
		activityPatternText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		activityId = new TextAreaFieldEditor(IClearCasePreferenceConstants.ACTIVITY_MSG_FORMAT, PreferenceMessages.getString("UcmPreferences.activityFormatMsg"), composite);
		addFieldEditor(activityId);
		
		initializeValues();
		
		return composite;
	}

	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub

	}
	
	
	/**
	 * Initializes values for non-FieldEditors.
	 */
	private void initializeValues() {
		final IPreferenceStore store = getPreferenceStore();
		
		activityPatternText.setText(store.getString(IClearCasePreferenceConstants.ACTIVITY_PATTERN));
		activityIdHelpTextFormat.setText(store.getString(IClearCasePreferenceConstants.ACTIVITY_FORMAT_HELP_STRING));
		
		
		setValid(true);
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
		// Since not a FieldEditor
		activityPatternText.setText(getPreferenceStore().getDefaultString(IClearCasePreferenceConstants.ACTIVITY_PATTERN));
		activityIdHelpTextFormat.setText(getPreferenceStore().getDefaultString(IClearCasePreferenceConstants.ACTIVITY_FORMAT_HELP_STRING));
		activityId.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		useUcm.store();
		// Since not a FieldEditor
		getPreferenceStore().setValue(IClearCasePreferenceConstants.ACTIVITY_PATTERN, activityPatternText.getText());
		getPreferenceStore().setValue(IClearCasePreferenceConstants.ACTIVITY_FORMAT_HELP_STRING, activityIdHelpTextFormat.getText());
		activityId.store();

		return super.performOk();
	}

	protected TextPair createFormatEditorControl(Composite composite, String title, String buttonText, final Map<String, String> supportedBindings) {

		createLabel(composite, title, SPAN);
		Text format = new Text(composite, SWT.BORDER);
		format.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		format.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateExamples();
			}
		});
		Button add = new Button(composite, SWT.PUSH);
		add.setText(buttonText);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, add.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		add.setLayoutData(data);
		final Text formatToInsert = format;
		add.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				addVariables(formatToInsert, supportedBindings);
			}
		});

		return new TextPair(format, null);
	}

	/**
	 * get the map of {variable,description} to use for files with
	 * createFormatEditorControl
	 */
	private Map getActivityIdBindingDescriptions() {
		Map bindings = new HashMap();
		bindings.put("stream", "current stream name"); //$NON-NLS-1$        
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

	class StringPair {
		String s1;

		String s2;
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
		// if (fPreview != null) fPreview.refresh();
	}

	/**
	 * Add another variable to the given target. The variable is inserted at
	 * current position A ListSelectionDialog is shown and the choose the
	 * variables to add
	 */
	private void addVariables(Text target, Map<String, String> bindings) {

		final ArrayList variables = new ArrayList(bindings.size());

		ILabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((StringPair) element).s1 + " - " + ((StringPair) element).s2; //$NON-NLS-1$
			}
		};

		IStructuredContentProvider contentsProvider = new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return variables.toArray(new StringPair[variables.size()]);
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		};

		for (Iterator it = bindings.keySet().iterator(); it.hasNext();) {
			StringPair variable = new StringPair();
			variable.s1 = (String) it.next(); // variable
			variable.s2 = bindings.get(variable.s1); // description
			variables.add(variable);
		}

		ListSelectionDialog dialog = new ListSelectionDialog(this.getShell(), this, contentsProvider, labelProvider, "Select variables that will be substituted with runtime values:"); //$NON-NLS-1$
		dialog.setTitle("Add Substitution Variables"); //$NON-NLS-1$
		if (dialog.open() != Window.OK)
			return;

		Object[] result = dialog.getResult();

		for (int i = 0; i < result.length; i++) {
			target.insert("{" + ((StringPair) result[i]).s1 + "}"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
