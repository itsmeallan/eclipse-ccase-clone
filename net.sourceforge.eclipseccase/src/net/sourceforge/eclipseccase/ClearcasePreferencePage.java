package net.sourceforge.eclipseccase;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ClearcasePreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{

    public ClearcasePreferencePage()
    {
        super(FieldEditorPreferencePage.GRID);
        setDescription("Settings for the eclipse-ccase Clearcase plugin");

        // Set the preference store for the preference page.
        IPreferenceStore store = ClearcasePlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    public void init(IWorkbench workbench)
    {}

    protected void createFieldEditors()
    {
        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.ADD_COMMENT,
                "Prompt for comment when adding to clearcase",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.CHECKIN_COMMENT,
                "Prompt for comment on checkin",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.CHECKIN_PRESERVE_TIME,
                "Preserve the file modification time on checkin",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.CHECKOUT_COMMENT,
                "Prompt for comment on checkout",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.RESERVED_CHECKOUT,
                "Reserved Checkouts",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.CHECKOUT_ON_EDIT,
                "Automatically checkout file when edited",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.REFACTOR_ADDS_DIR,
                "Automatically add dest dir to clearcase when refactoring",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.USE_CLEARTOOL,
                "Use the cleartool executable for clearcase operations",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.PERSIST_STATE,
                "Persist element state cache across sessions",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.REFRESH_ON_CHANGE,
                "Quick update clearcase state on resource changed event",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.ESCAPE_COMMENTS,
                "Escape special characters (\"<>'&&) in comments with XML entities",
                getFieldEditorParent()));

        addField(
            new BooleanFieldEditor(
                IPreferenceConstants.MULTILINE_COMMENTS,
                "Enable multi-line comments",
                getFieldEditorParent()));
    }

    public boolean performOk()
    {
        ClearcasePlugin.getDefault().resetClearcase();
        return super.performOk();
    }

}
