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
package net.sourceforge.eclipseccase.ui;

/**
 * Interface with shared UI preference constants.
 */
public interface IClearcaseUIPreferenceConstants
{
    /** decorator preference */
    String TEXT_VERSION_DECORATION = ClearcaseUI.PLUGIN_ID
            + ".textVersionDecoration"; //$NON-NLS-1$

    /** decorator preference */
    String TEXT_VIEW_DECORATION = ClearcaseUI.PLUGIN_ID + ".textViewDecoration"; //$NON-NLS-1$

    /** decorator preference */
    String TEXT_PREFIX_DECORATION = ClearcaseUI.PLUGIN_ID + ".textPrefixDecoration"; //$NON-NLS-1$

    /** decorator preference */
    String GENERAL_DEEP_DECORATIONS = ClearcaseUI.PLUGIN_ID + ".deepDecorations"; //$NON-NLS-1$

    /** decorator preference */
    String GENERAL_DEEP_NEW = ClearcaseUI.PLUGIN_ID + ".general.deepNew"; //$NON-NLS-1$

    /** decorator preference */
    String TEXT_PREFIX_DIRTY = ClearcaseUI.PLUGIN_ID + ".text.prefixDirty"; //$NON-NLS-1$

    /** decorator preference */
    String TEXT_PREFIX_UNKNOWN = ClearcaseUI.PLUGIN_ID + ".text.prefixUnknown"; //$NON-NLS-1$

    /** decorator preference */
    String TEXT_PREFIX_NEW = ClearcaseUI.PLUGIN_ID + ".text.prefixNew"; //$NON-NLS-1$

    /** decorator preference */
    String TEXT_PREFIX_EDITED = ClearcaseUI.PLUGIN_ID + ".text.prefixEdited"; //$NON-NLS-1$

    /** decorator preference */
    String TEXT_PREFIX_HIJACKED = ClearcaseUI.PLUGIN_ID + ".text.prefixHijacked"; //$NON-NLS-1$

    /** decorator preference */
    String ICON_DECORATE_NEW = ClearcaseUI.PLUGIN_ID + ".icon.decorateNew"; //$NON-NLS-1$

    /** decorator preference */
    String ICON_DECORATE_UNKNOWN = ClearcaseUI.PLUGIN_ID + ".icon.decorateUnknown"; //$NON-NLS-1$

    /** decorator preference */
    String ICON_DECORATE_EDITED = ClearcaseUI.PLUGIN_ID + ".icon.decorateEdited"; //$NON-NLS-1$

    /** decorator preference */
    String ICON_DECORATE_HIJACKED = ClearcaseUI.PLUGIN_ID + ".icon.decorateHijacked"; //$NON-NLS-1$
}