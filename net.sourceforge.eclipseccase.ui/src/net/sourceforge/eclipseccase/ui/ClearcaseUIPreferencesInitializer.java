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

package net.sourceforge.eclipseccase.ui;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * A initializer for Clearcase UI preferences.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
public class ClearcaseUIPreferencesInitializer extends AbstractPreferenceInitializer implements IClearcaseUIPreferenceConstants {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences preferences = ClearcaseUI.getInstance().getPluginPreferences();

		// Decorator preferences
		preferences.setDefault(IClearcaseUIPreferenceConstants.GENERAL_DEEP_DECORATIONS, true);
		preferences.setDefault(IClearcaseUIPreferenceConstants.GENERAL_DEEP_NEW, true);

		// default text decorations
		preferences.setDefault(IClearcaseUIPreferenceConstants.TEXT_VIEW_DECORATION, true);
		preferences.setDefault(IClearcaseUIPreferenceConstants.TEXT_VERSION_DECORATION, false);
		preferences.setDefault(IClearcaseUIPreferenceConstants.TEXT_PREFIX_DECORATION, false);

		// default prefixes
		preferences.setDefault(IClearcaseUIPreferenceConstants.TEXT_PREFIX_DIRTY, Messages.getString("ClearcaseUIPreferencesInitializer.Default.Prefix.Dirty")); //$NON-NLS-1$
		preferences.setDefault(IClearcaseUIPreferenceConstants.TEXT_PREFIX_HIJACKED, Messages.getString("ClearcaseUIPreferencesInitializer.Default.Prefix.Hijacked")); //$NON-NLS-1$
		preferences.setDefault(IClearcaseUIPreferenceConstants.TEXT_PREFIX_NEW, Messages.getString("ClearcaseUIPreferencesInitializer.Default.Prefix.New")); //$NON-NLS-1$
		preferences.setDefault(IClearcaseUIPreferenceConstants.TEXT_PREFIX_EDITED, Messages.getString("ClearcaseUIPreferencesInitializer.Default.Prefix.Edited")); //$NON-NLS-1$
		preferences.setDefault(IClearcaseUIPreferenceConstants.TEXT_PREFIX_UNKNOWN, Messages.getString("ClearcaseUIPreferencesInitializer.Default.Prefix.Unknown")); //$NON-NLS-1$

		// default icon decorations
		preferences.setDefault(IClearcaseUIPreferenceConstants.ICON_DECORATE_NEW, false);
		preferences.setDefault(IClearcaseUIPreferenceConstants.ICON_DECORATE_EDITED, true);
		preferences.setDefault(IClearcaseUIPreferenceConstants.ICON_DECORATE_UNKNOWN, true);
		preferences.setDefault(IClearcaseUIPreferenceConstants.ICON_DECORATE_HIJACKED, true);
	}

}