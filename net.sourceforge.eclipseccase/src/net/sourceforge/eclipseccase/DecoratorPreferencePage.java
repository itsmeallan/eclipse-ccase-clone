package net.sourceforge.eclipseccase;

import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DecoratorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
		
	public DecoratorPreferencePage()
	{
		super(FieldEditorPreferencePage.GRID);

		// Set the preference store for the preference page.
		IPreferenceStore store =
			ClearcasePlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors()
	{
		BooleanFieldEditor textDecorations =
			new BooleanFieldEditor(
				IPreferenceConstants.TEXT_DECORATIONS,
				"Enable text decorations",
				getFieldEditorParent());
		addField(textDecorations);

		BooleanFieldEditor deepDecorations =
			new BooleanFieldEditor(
				IPreferenceConstants.DEEP_DECORATIONS,
				"Compute deep state for dirty elements",
				getFieldEditorParent());
		addField(deepDecorations);

	}

	
	public boolean performOk()
	{
		ClearcaseDecorator.refresh();
		return super.performOk();
	}

	public void init(IWorkbench workbench)
	{
	}

}
