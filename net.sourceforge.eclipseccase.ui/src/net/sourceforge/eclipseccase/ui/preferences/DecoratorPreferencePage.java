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
package net.sourceforge.eclipseccase.ui.preferences;

import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;
import net.sourceforge.eclipseccase.ui.ClearcaseUI;
import net.sourceforge.eclipseccase.ui.IClearcaseUIPreferenceConstants;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * The preference page for the ClearCase label decorator.
 */
public class DecoratorPreferencePage extends FieldEditorPreferencePageWithCategories
        implements IWorkbenchPreferencePage, IClearcaseUIPreferenceConstants
{
    private static final String TEXT = "Text";

    private static final String IMAGES = "Images";

    private static final String GENERAL = "General";

    private static final String[] CATEGORIES = new String[] { GENERAL,
            IMAGES, TEXT};


    /**
     * Creates a new instance.
     */
    public DecoratorPreferencePage() {
        super();

        // Set the preference store for the preference page.
        setPreferenceStore(ClearcaseUI.getInstance().getPreferenceStore());

        setDescription("Customize the ClearCase label decorator to suite your needs.");
    }

    /**
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors()
    {
        // general

        addField(new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.DEEP_DECORATIONS,
                "Compute deep dirty state for folders and projects",
                getFieldEditorParent(GENERAL)));

        // image decoration

        addField(new BooleanFieldEditor(ICON_DECORATE_EDITED,
                "Edited by someone else", getFieldEditorParent(IMAGES)));

        addField(new BooleanFieldEditor(ICON_DECORATE_HIJACKED, "Hijacked",
                getFieldEditorParent(IMAGES)));

        addField(new BooleanFieldEditor(ICON_DECORATE_NEW, "New",
                getFieldEditorParent(IMAGES)));

        addField(new BooleanFieldEditor(ICON_DECORATE_UNKNOWN, "Unknown state",
                getFieldEditorParent(IMAGES)));

        // text decorations

        addField(new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION,
                "Add the name of the associated view to project names",
                getFieldEditorParent(TEXT)));

        addField(new BooleanFieldEditor(
                IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION,
                "Append version information to resource names",
                getFieldEditorParent(TEXT)));

        addField(new StringFieldEditor(TEXT_PREFIX_DIRTY,
                "Prefix for dirty resources:", 4, getFieldEditorParent(TEXT)));

        addField(new StringFieldEditor(TEXT_PREFIX_HIJACKED,
                "Prefix for hijacked resources:", 4, getFieldEditorParent(TEXT)));

        addField(new StringFieldEditor(TEXT_PREFIX_NEW,
                "Prefix for new resources:", 4, getFieldEditorParent(TEXT)));

        addField(new StringFieldEditor(TEXT_PREFIX_EDITED,
                "Prefix for resources edited by someone else:", 4,
                getFieldEditorParent(TEXT)));

        addField(new StringFieldEditor(TEXT_PREFIX_UNKNOWN,
                "Prefix for resources with an unknown state:", 4,
                getFieldEditorParent(TEXT)));
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

    /* (non-Javadoc)
     * @see net.sourceforge.eclipseccase.ui.preferences.FieldEditorPreferencePageWithCategories#getDescription(java.lang.String)
     */
    protected String getDescription(String category) {
        if(GENERAL.equals(category))
            return "General settings:";
        if(IMAGES.equals(category))
            return "Image decorations:";
        if(TEXT.equals(category))
            return "Text decorations:";
        
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipseccase.ui.preferences.FieldEditorPreferencePageWithCategories#getCategories()
     */
    protected String[] getCategories() {
        return CATEGORIES;
    }

}