package net.sourceforge.eclipseccase;

import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;
import net.sourceforge.eclipseccase.ui.SpacerFieldEditor;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
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
		BooleanFieldEditor textViewDecoration =
			new BooleanFieldEditor(
				IPreferenceConstants.TEXT_VIEW_DECORATION,
				"Enable text decoration of the viewname for projects",
				getFieldEditorParent());
		addField(textViewDecoration);

		BooleanFieldEditor textVersionDecoration =
			new BooleanFieldEditor(
				IPreferenceConstants.TEXT_VERSION_DECORATION,
				"Enable text decoration of the clearcase version for resources",
				getFieldEditorParent());
		addField(textVersionDecoration);

		BooleanFieldEditor textDirtyDecoration =
			new BooleanFieldEditor(
				IPreferenceConstants.TEXT_DIRTY_DECORATION,
				"Enable text decoration of the dirty state for resources",
				getFieldEditorParent());
		addField(textDirtyDecoration);

        BooleanFieldEditor textNewDecoration =
            new BooleanFieldEditor(
                IPreferenceConstants.TEXT_NEW_DECORATION,
                "Enable text decoration for new resources not in ClearCase.",
                getFieldEditorParent());
        addField(textNewDecoration);

		SpacerFieldEditor spacer1 = new SpacerFieldEditor(
			getFieldEditorParent());
		addField(spacer1);
		
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
