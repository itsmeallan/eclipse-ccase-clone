
package net.sourceforge.eclipseccase;

import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;
import net.sourceforge.eclipseccase.ui.ClearcaseUI;
import net.sourceforge.eclipseccase.ui.IClearcaseUIPreferenceConstants;
import net.sourceforge.eclipseccase.ui.SpacerFieldEditor;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * DecoratorPreferencePage.
 */
public class DecoratorPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage
{

    /**
     * Creates a new instance.
     */
    public DecoratorPreferencePage()
    {
        super(FieldEditorPreferencePage.GRID);

        // Set the preference store for the preference page.
        setPreferenceStore(ClearcaseUI.getInstance().getPreferenceStore());
    }

    /**
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors()
    {
        BooleanFieldEditor textViewDecoration = new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION,
                "Enable text decoration of the viewname for projects",
                getFieldEditorParent());
        addField(textViewDecoration);

        BooleanFieldEditor textVersionDecoration = new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION,
                "Enable text decoration of the clearcase version for resources",
                getFieldEditorParent());
        addField(textVersionDecoration);

        BooleanFieldEditor textDirtyDecoration = new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.TEXT_DIRTY_DECORATION,
                "Enable text decoration of the dirty state for resources",
                getFieldEditorParent());
        addField(textDirtyDecoration);

        BooleanFieldEditor textNewDecoration = new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.TEXT_NEW_DECORATION,
                "Enable text decoration for new resources not in ClearCase.",
                getFieldEditorParent());
        addField(textNewDecoration);

        SpacerFieldEditor spacer1 = new SpacerFieldEditor(
                getFieldEditorParent());
        addField(spacer1);

        BooleanFieldEditor deepDecorations = new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.DEEP_DECORATIONS,
                "Compute deep state for dirty elements", getFieldEditorParent());
        addField(deepDecorations);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {
        if (super.performOk())
        {
            IDecoratorManager manager = PlatformUI.getWorkbench()
                    .getDecoratorManager();
            if (manager.getEnabled(ClearcaseDecorator.ID))
            {
                ClearcaseDecorator activeDecorator = (ClearcaseDecorator) manager
                        .getBaseLabelProvider(ClearcaseDecorator.ID);
                if (activeDecorator != null)
                {
                    activeDecorator.refresh();
                }
            }
            return true;
        }

        return false;
    }

    public void init(IWorkbench workbench)
    {
    // ignore
    }

}