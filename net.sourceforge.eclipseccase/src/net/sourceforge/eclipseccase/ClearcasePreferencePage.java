package net.sourceforge.eclipseccase;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ClearcasePreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage
{
	private Button reservedCheckoutButton;
	private Button persistStateButton;
	private Button checkinCommentButton;
	private Button checkoutCommentButton;
	private Button addCommentButton;
	private Button checkoutOnEditButton;
	private Button refactorAddsDirButton;
	private Button textVersionDecorationButton;
	
	/**
	 * Constructor for ClearcasePreferencePage.
	 */
	public ClearcasePreferencePage()
	{
		setDescription("Settings for the eclipse-ccase Clearcase plugin");

	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
		setPreferenceStore(getPreferenceStore());
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NULL);

		// GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		// GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
			
		// Create the checkbox for sync mode
		addCommentButton = createCheckBox(composite, "Prompt for comment when adding to clearcase");
		checkinCommentButton = createCheckBox(composite, "Prompt for comment on checkin");
		checkoutCommentButton = createCheckBox(composite, "Prompt for comment on checkout");
		reservedCheckoutButton = createCheckBox(composite, "Reserved Checkouts");
		checkoutOnEditButton = createCheckBox(composite, "Automatically checkout file when edited");
		refactorAddsDirButton = createCheckBox(composite, "Automatically add dest dir to clearcase when refactoring");
		textVersionDecorationButton = createCheckBox(composite, "Add text labels for element versions when decorating");
		persistStateButton = createCheckBox(composite, "Persist element state cache across sessions");

		initializeValues();
		
		return composite;
	}
	
	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues()
	{
		IPreferenceStore store = getPreferenceStore();
		reservedCheckoutButton.setSelection(store.getBoolean(ClearcasePlugin.PREF_RESERVED_CHECKOUT));
		persistStateButton.setSelection(store.getBoolean(ClearcasePlugin.PREF_PERSIST_STATE));
		checkinCommentButton.setSelection(store.getBoolean(ClearcasePlugin.PREF_CHECKIN_COMMENT));
		checkoutCommentButton.setSelection(store.getBoolean(ClearcasePlugin.PREF_CHECKOUT_COMMENT));
		addCommentButton.setSelection(store.getBoolean(ClearcasePlugin.PREF_ADD_COMMENT));
		checkoutOnEditButton.setSelection(store.getBoolean(ClearcasePlugin.PREF_CHECKOUT_ON_EDIT));
		refactorAddsDirButton.setSelection(store.getBoolean(ClearcasePlugin.PREF_REFACTOR_ADDS_DIR));
		textVersionDecorationButton.setSelection(store.getBoolean(ClearcasePlugin.PREF_TEXT_VERSION_DECORATION));
	}

	/**
	 * Creates an new checkbox instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @param label  the string to set into the checkbox
	 * @return the new checkbox
	 */
	private Button createCheckBox(Composite group, String label)
	{
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		button.setLayoutData(data);
		return button;
	}

	/**
	 * OK was clicked. Store the preferences.
	 *
	 * @return whether it is okay to close the preference page
	 */
	public boolean performOk()
	{
		IPreferenceStore store = getPreferenceStore();
		store.setValue(ClearcasePlugin.PREF_RESERVED_CHECKOUT,
					   reservedCheckoutButton.getSelection());
		store.setValue(ClearcasePlugin.PREF_PERSIST_STATE,
					   persistStateButton.getSelection());
		store.setValue(ClearcasePlugin.PREF_CHECKIN_COMMENT,
					   checkinCommentButton.getSelection());
		store.setValue(ClearcasePlugin.PREF_CHECKOUT_COMMENT,
					   checkoutCommentButton.getSelection());
		store.setValue(ClearcasePlugin.PREF_ADD_COMMENT,
					   addCommentButton.getSelection());
		store.setValue(ClearcasePlugin.PREF_CHECKOUT_ON_EDIT,
					   checkoutOnEditButton.getSelection());
		store.setValue(ClearcasePlugin.PREF_REFACTOR_ADDS_DIR,
					   refactorAddsDirButton.getSelection());
		store.setValue(ClearcasePlugin.PREF_TEXT_VERSION_DECORATION,
					   textVersionDecorationButton.getSelection());
		savePreferenceStore();
		return true;
	}

	protected void performDefaults()
	{
		super.performDefaults();
		IPreferenceStore store = getPreferenceStore();
		reservedCheckoutButton.setSelection(store.getDefaultBoolean(ClearcasePlugin.PREF_RESERVED_CHECKOUT));
		persistStateButton.setSelection(store.getDefaultBoolean(ClearcasePlugin.PREF_PERSIST_STATE));
		checkinCommentButton.setSelection(store.getDefaultBoolean(ClearcasePlugin.PREF_CHECKIN_COMMENT));
		checkoutCommentButton.setSelection(store.getDefaultBoolean(ClearcasePlugin.PREF_CHECKOUT_COMMENT));
		addCommentButton.setSelection(store.getDefaultBoolean(ClearcasePlugin.PREF_ADD_COMMENT));
		checkoutOnEditButton.setSelection(store.getDefaultBoolean(ClearcasePlugin.PREF_CHECKOUT_ON_EDIT));
		refactorAddsDirButton.setSelection(store.getDefaultBoolean(ClearcasePlugin.PREF_REFACTOR_ADDS_DIR));
		textVersionDecorationButton.setSelection(store.getDefaultBoolean(ClearcasePlugin.PREF_TEXT_VERSION_DECORATION));
	}

	public IPreferenceStore getPreferenceStore()
	{
		return ClearcasePlugin.getDefault().getPreferenceStore();
	}

	public void savePreferenceStore()
	{
		ClearcasePlugin.getDefault().savePluginPreferences();
	}
}
