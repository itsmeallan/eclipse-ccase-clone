/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.eclipseccase.ui.preferences;

import net.sourceforge.eclipseccase.ui.ClearcaseUI;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.*;

/**
 * An initializer for Clearcase UI default preferences and a class for accessing
 * ClearCase UI preferences.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
public class ClearcaseUIPreferences extends AbstractPreferenceInitializer {

	/** prefix for UI preferences */
	private static final String _PREFIX = "net.sourceforge.eclipseccase.ui.preferences."; //$NON-NLS-1$

	/** prefix for decorator preferences */
	private static final String _PREFIX_DECORATOR = _PREFIX + "decorator."; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_CHECKED_IN_ELEMENTS = _PREFIX_DECORATOR + "elements.checkedIn"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_CLEARCASE_ELEMENTS = _PREFIX_DECORATOR + "elements"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_EDITED_ELEMENTS = _PREFIX_DECORATOR + "elements.edited"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_ELEMENT_STATES_WITH_TEXT_PREFIX = _PREFIX_DECORATOR + "elements.textPrefix"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_ELEMENTS_WITH_VERSION_INFO = _PREFIX_DECORATOR + "elements.versionInfo"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_FOLDERS_CONTAINING_VIEW_PRIVATE_DIRTY = _PREFIX_DECORATOR + "folders.dirty.withViewPrivate"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_FOLDERS_DIRTY = _PREFIX_DECORATOR + "folders.dirty"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_HIJACKED_ELEMENTS = _PREFIX_DECORATOR + "elements.hijacked"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_PROJECTS_WITH_VIEW_INFO = _PREFIX_DECORATOR + "projects.viewInfo"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_UNKNOWN_ELEMENTS = _PREFIX_DECORATOR + "elements.unkown"; //$NON-NLS-1$

	/** decorator preference */
	public static final String DECORATE_VIEW_PRIVATE_ELEMENTS = _PREFIX_DECORATOR + "elements.viewPrivate"; //$NON-NLS-1$

	/** decorator preference */
	public static final String IMAGE_CLEARCASE_ELEMENTS_BACKGROUND_CUSTOM = _PREFIX_DECORATOR + "elements.image.background.custom"; //$NON-NLS-1$

	/** decorator preference */
	public static final String IMAGE_CLEARCASE_ELEMENTS_BACKGROUND = _PREFIX_DECORATOR + "elements.image.background"; //$NON-NLS-1$

	/** decorator preference */
	public static final String TEXT_PREFIX_CHECKED_IN_ELEMENTS = _PREFIX_DECORATOR + "elements.textPrefix.checkedIn"; //$NON-NLS-1$

	/** decorator preference */
	public static final String TEXT_PREFIX_DIRTY_ELEMENTS = _PREFIX_DECORATOR + "elements.textPrefix.dirty"; //$NON-NLS-1$

	/** decorator preference */
	public static final String TEXT_PREFIX_EDITED_ELEMENTS = _PREFIX_DECORATOR + "elements.textPrefix.edited"; //$NON-NLS-1$

	/** decorator preference */
	public static final String TEXT_PREFIX_HIJACKED_ELEMENTS = _PREFIX_DECORATOR + "elements.textPrefix.hijacked"; //$NON-NLS-1$

	/** decorator preference */
	public static final String TEXT_PREFIX_UNKNOWN_ELEMENTS = _PREFIX_DECORATOR + ".elements.textPrefix.unknown"; //$NON-NLS-1$

	/** decorator preference */
	public static final String TEXT_PREFIX_VIEW_PRIVATE_ELEMENTS = _PREFIX_DECORATOR + "elements.textPrefix.viewPrivate"; //$NON-NLS-1$

	/**
	 * Indicates if checked in elements should be decorated.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateCheckedInElements() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_CHECKED_IN_ELEMENTS);
	}

	/**
	 * Indicates if all ClearCase elements should be decorated with a background
	 * icon.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateClearCaseElements() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_CLEARCASE_ELEMENTS);
	}

	/**
	 * Indicates if elementes checked out in onther view or branch should be
	 * decorated.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateEditedElements() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_EDITED_ELEMENTS);
	}

	/**
	 * Indicates if elements should be decorated with text prefixes.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateElementStatesWithTextPrefix() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_ELEMENT_STATES_WITH_TEXT_PREFIX);
	}

	/**
	 * Indicates if elements should be decorated with their version info.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateElementsWithVersionInfo() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_ELEMENTS_WITH_VERSION_INFO);
	}

	/**
	 * Indicates if folders containing view private elements should be decorated
	 * as dirty.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateFoldersContainingViewPrivateElementsDirty() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_FOLDERS_CONTAINING_VIEW_PRIVATE_DIRTY);
	}

	/**
	 * Indicates if folder decorations should be calculated depending on their
	 * member states.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateFoldersDirty() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_FOLDERS_DIRTY);
	}

	/**
	 * Indicates if hijacked elements should be decorated.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateHijackedElements() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_HIJACKED_ELEMENTS);
	}

	/**
	 * Indicates if projects should be decorated with the view information.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateProjectsWithViewInfo() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_PROJECTS_WITH_VIEW_INFO);
	}

	/**
	 * Indicates if elements with an unknown state should be decorated.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateUnknownElements() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_UNKNOWN_ELEMENTS);
	}

	/**
	 * Indicates if view private elements should be decorated.
	 * 
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public static boolean decorateViewPrivateElements() {
		return getPluginPreferences().getBoolean(ClearcaseUIPreferences.DECORATE_VIEW_PRIVATE_ELEMENTS);
	}

	/**
	 * Returns the plugin preferences.
	 * 
	 * @return the plugin preferences
	 */
	public static Preferences getPluginPreferences() {
		return ClearcaseUI.getInstance().getPluginPreferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {

		IEclipsePreferences defaults = new DefaultScope().getNode(ClearcaseUI.PLUGIN_ID);

		// Decorator preferences
		defaults.putBoolean(DECORATE_FOLDERS_DIRTY, true);
		defaults.putBoolean(DECORATE_FOLDERS_CONTAINING_VIEW_PRIVATE_DIRTY, true);

		// default text decorations
		defaults.putBoolean(DECORATE_PROJECTS_WITH_VIEW_INFO, true);
		defaults.putBoolean(DECORATE_ELEMENTS_WITH_VERSION_INFO, false);
		defaults.putBoolean(DECORATE_ELEMENT_STATES_WITH_TEXT_PREFIX, false);

		// default prefixes
		defaults.put(TEXT_PREFIX_CHECKED_IN_ELEMENTS, ">"); //$NON-NLS-1$
		defaults.put(TEXT_PREFIX_DIRTY_ELEMENTS, "*"); //$NON-NLS-1$
		defaults.put(TEXT_PREFIX_HIJACKED_ELEMENTS, "$"); //$NON-NLS-1$
		defaults.put(TEXT_PREFIX_VIEW_PRIVATE_ELEMENTS, "+"); //$NON-NLS-1$
		defaults.put(TEXT_PREFIX_EDITED_ELEMENTS, "#"); //$NON-NLS-1$
		defaults.put(TEXT_PREFIX_UNKNOWN_ELEMENTS, "?"); //$NON-NLS-1$

		// default icon decorations
		defaults.putBoolean(DECORATE_CHECKED_IN_ELEMENTS, true);
		defaults.putBoolean(DECORATE_CLEARCASE_ELEMENTS, false);
		defaults.putBoolean(DECORATE_VIEW_PRIVATE_ELEMENTS, false);
		defaults.putBoolean(DECORATE_EDITED_ELEMENTS, true);
		defaults.putBoolean(DECORATE_UNKNOWN_ELEMENTS, true);
		defaults.putBoolean(DECORATE_HIJACKED_ELEMENTS, true);

		// default image locations
		defaults.putBoolean(IMAGE_CLEARCASE_ELEMENTS_BACKGROUND_CUSTOM, false);
	}

}