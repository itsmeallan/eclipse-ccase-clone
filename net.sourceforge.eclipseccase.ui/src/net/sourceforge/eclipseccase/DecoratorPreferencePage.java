/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *******************************************************************************/
package net.sourceforge.eclipseccase;

import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;
import net.sourceforge.eclipseccase.ui.ClearcaseUI;
import net.sourceforge.eclipseccase.ui.IClearcaseUIPreferenceConstants;
import net.sourceforge.eclipseccase.ui.LabelFieldEditor;
import net.sourceforge.eclipseccase.ui.SpacerFieldEditor;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * The preference page for the ClearCase label decorator.
 */
public class DecoratorPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, IClearcaseUIPreferenceConstants
{

    /**
     * Creates a new instance.
     */
    public DecoratorPreferencePage() {
        super(FieldEditorPreferencePage.GRID);

        // Set the preference store for the preference page.
        setPreferenceStore(ClearcaseUI.getInstance().getPreferenceStore());

        setDescription("Customize the ClearCase label decorator to suite your needs.");
    }

    /**
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors()
    {
        // some space
        //addField(new SpacerFieldEditor(getFieldEditorParent()));

        // general
        addField(new LabelFieldEditor("General:", getFieldEditorParent()));

        addField(new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.DEEP_DECORATIONS,
                "Compute deep dirty state for folders and projects",
                getFieldEditorParent()));

        // some space
        addField(new SpacerFieldEditor(getFieldEditorParent()));

        // text decorations
        addField(new LabelFieldEditor("Text decorations:",
                getFieldEditorParent()));

        addField(new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION,
                "Add the name of the associated view project names",
                getFieldEditorParent()));

        addField(new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION,
                "Append version information to resource names",
                getFieldEditorParent()));

        addField(new StringFieldEditor(TEXT_PREFIX_DIRTY,
                "Prefix for dirty resources:", 4, getFieldEditorParent()));

        addField(new StringFieldEditor(TEXT_PREFIX_HIJACKED,
                "Prefix for hijacked resources:", 4, getFieldEditorParent()));

        addField(new StringFieldEditor(TEXT_PREFIX_NEW,
                "Prefix for new resources:", 4, getFieldEditorParent()));

        addField(new StringFieldEditor(TEXT_PREFIX_EDITED,
                "Prefix for resources edited by someone else:", 4,
                getFieldEditorParent()));

        addField(new StringFieldEditor(TEXT_PREFIX_UNKNOWN,
                "Prefix for resources with an unknown state:", 4,
                getFieldEditorParent()));

        // some space
        addField(new SpacerFieldEditor(getFieldEditorParent()));

        // icon decoration
        addField(new LabelFieldEditor("Image decorations:",
                getFieldEditorParent()));

        addField(new BooleanFieldEditor(ICON_DECORATE_EDITED,
                "Edited by someone else", getFieldEditorParent()));

        addField(new BooleanFieldEditor(ICON_DECORATE_HIJACKED, "Hijacked",
                getFieldEditorParent()));

        addField(new BooleanFieldEditor(ICON_DECORATE_NEW, "New",
                getFieldEditorParent()));

        addField(new BooleanFieldEditor(ICON_DECORATE_UNKNOWN, "Unknown state",
                getFieldEditorParent()));
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
            // refresh the decorator
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
    // nothing
    }

}