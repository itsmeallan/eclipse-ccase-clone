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
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * The preference page for the ClearCase label decorator.
 */
public class DecoratorPreferencePage
		extends
			FieldEditorPreferencePageWithCategories
		implements
			IWorkbenchPreferencePage,
			IClearcaseUIPreferenceConstants {
	private static final String TEXT = PreferenceMessages.getString("Decorator.Category.Text"); //$NON-NLS-1$

	private static final String IMAGES = PreferenceMessages.getString("Decorator.Category.Images"); //$NON-NLS-1$

	private static final String GENERAL = PreferenceMessages.getString("Decorator.Category.General"); //$NON-NLS-1$

	private static final String[] CATEGORIES = new String[]{TEXT, IMAGES
			,GENERAL};

	MasterBooleanFieldEditor masterPrefix, masterDeep;
	/**
	 * Creates a new instance.
	 */
	public DecoratorPreferencePage() {
		super();

		// Set the preference store for the preference page.
		setPreferenceStore(ClearcaseUI.getInstance().getPreferenceStore());

		setDescription(PreferenceMessages.getString("Decorator.Description")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		FieldEditor slave;
		// general
		masterDeep = new MasterBooleanFieldEditor(
				IClearcaseUIPreferenceConstants.GENERAL_DEEP_DECORATIONS,
				PreferenceMessages.getString("Decorator.General.DeepDecorations"), //$NON-NLS-1$
				getFieldEditorParent(GENERAL));
		addField(masterDeep);
		slave = new BooleanFieldEditor(GENERAL_DEEP_NEW,
				PreferenceMessages.getString("Decorator.General.DeepNew"), getFieldEditorParent(GENERAL)); //$NON-NLS-1$

		addField(slave);
		masterDeep.addSlave(slave);
		
		// image decoration

		addField(new BooleanFieldEditor(ICON_DECORATE_EDITED,
				PreferenceMessages.getString("Decorator.Images.DecorateEdited"), getFieldEditorParent(IMAGES))); //$NON-NLS-1$

		addField(new BooleanFieldEditor(ICON_DECORATE_HIJACKED, PreferenceMessages.getString("Decorator.Images.DecorateHijacked"), //$NON-NLS-1$
				getFieldEditorParent(IMAGES)));

		addField(new BooleanFieldEditor(ICON_DECORATE_NEW, PreferenceMessages.getString("Decorator.Images.DecorateNew"), //$NON-NLS-1$
				getFieldEditorParent(IMAGES)));

		addField(new BooleanFieldEditor(ICON_DECORATE_UNKNOWN, PreferenceMessages.getString("Decorator.Images.DecorateUnknown"), //$NON-NLS-1$
				getFieldEditorParent(IMAGES)));

		// text decorations

		addField(new BooleanFieldEditor(
				IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION,
				PreferenceMessages.getString("Decorator.Text.ViewDecoration"), //$NON-NLS-1$
				getFieldEditorParent(TEXT)));

		addField(new BooleanFieldEditor(
				IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION,
				PreferenceMessages.getString("Decorator.Text.VersionDecoration"), //$NON-NLS-1$
				getFieldEditorParent(TEXT)));

		masterPrefix = new MasterBooleanFieldEditor(
				IClearcaseUIPreferenceConstants.TEXT_PREFIX_DECORATION,
				PreferenceMessages.getString("Decorator.Text.PrefixDecoration"), //$NON-NLS-1$
				getFieldEditorParent(TEXT));

		addField(masterPrefix);

		slave = new StringFieldEditor(TEXT_PREFIX_DIRTY,
				PreferenceMessages.getString("Decorator.Text.PrefixDirty"), 4, getFieldEditorParent(TEXT)); //$NON-NLS-1$
		addField(slave);
		masterPrefix.addSlave(slave);

		slave = new StringFieldEditor(TEXT_PREFIX_HIJACKED,
				PreferenceMessages.getString("Decorator.Text.PrefixHijacked"), 4, getFieldEditorParent(TEXT)); //$NON-NLS-1$
		addField(slave);
		masterPrefix.addSlave(slave);

		slave = new StringFieldEditor(TEXT_PREFIX_NEW,
				PreferenceMessages.getString("Decorator.Text.PrefixNew"), 4, getFieldEditorParent(TEXT)); //$NON-NLS-1$
		addField(slave);
		masterPrefix.addSlave(slave);

		slave = new StringFieldEditor(TEXT_PREFIX_EDITED,
				PreferenceMessages.getString("Decorator.Text.PrefixEdited"), 4, //$NON-NLS-1$
				getFieldEditorParent(TEXT));
		addField(slave);
		masterPrefix.addSlave(slave);

		slave = new StringFieldEditor(TEXT_PREFIX_UNKNOWN,
				PreferenceMessages.getString("Decorator.Text.PrefixUnknown"), 4, //$NON-NLS-1$
				getFieldEditorParent(TEXT));
		addField(slave);
		masterPrefix.addSlave(slave);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (super.performOk()) {
			// refresh the decorator
			IDecoratorManager manager = PlatformUI.getWorkbench()
					.getDecoratorManager();
			if (manager.getEnabled(ClearcaseDecorator.ID)) {
				ClearcaseDecorator activeDecorator = (ClearcaseDecorator) manager
						.getBaseLabelProvider(ClearcaseDecorator.ID);
				if (activeDecorator != null) {
					activeDecorator.refresh();
				}
			}
			return true;
		}

		return false;
	}

	public void init(IWorkbench workbench) {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	protected void initialize() {
		super.initialize();
		if (masterPrefix != null)
			masterPrefix.listen();
		if (masterDeep != null)
			masterDeep.listen();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.ui.preferences.FieldEditorPreferencePageWithCategories#getDescription(java.lang.String)
	 */
	protected String getDescription(String category) {
		if (GENERAL.equals(category))
			return PreferenceMessages.getString("Decorator.Description.Category.General"); //$NON-NLS-1$
		if (IMAGES.equals(category))
			return PreferenceMessages.getString("Decorator.Description.Category.Images"); //$NON-NLS-1$
		if (TEXT.equals(category))
			return PreferenceMessages.getString("Decorator.Description.Category.Text"); //$NON-NLS-1$

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.ui.preferences.FieldEditorPreferencePageWithCategories#getCategories()
	 */
	protected String[] getCategories() {
		return CATEGORIES;
	}

}