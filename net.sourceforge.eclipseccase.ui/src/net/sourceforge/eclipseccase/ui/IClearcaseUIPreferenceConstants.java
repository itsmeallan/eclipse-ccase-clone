/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 13, 2004
 */
package net.sourceforge.eclipseccase.ui;

/**
 * Interface with shared UI constants.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public interface IClearcaseUIPreferenceConstants
{
    /** decorator preference */
    String TEXT_VERSION_DECORATION = ClearcaseUI.PLUGIN_ID
            + ".textVersionDecoration";

    /** decorator preference */
    String TEXT_VIEW_DECORATION = ClearcaseUI.PLUGIN_ID + ".textViewDecoration";

    /** decorator preference */
    String DEEP_DECORATIONS = ClearcaseUI.PLUGIN_ID + ".deepDecorations";

    /** decorator preference */
    String TEXT_PREFIX_DIRTY = ClearcaseUI.PLUGIN_ID + ".text.prefixDirty";

    /** decorator preference */
    String TEXT_PREFIX_UNKNOWN = ClearcaseUI.PLUGIN_ID + ".text.prefixUnknown";

    /** decorator preference */
    String TEXT_PREFIX_NEW = ClearcaseUI.PLUGIN_ID + ".text.prefixNew";

    /** decorator preference */
    String TEXT_PREFIX_EDITED = ClearcaseUI.PLUGIN_ID + ".text.prefixEdited";

    /** decorator preference */
    String TEXT_PREFIX_HIJACKED = ClearcaseUI.PLUGIN_ID + ".text.prefixHijacked";

    /** decorator preference */
    String ICON_DECORATE_NEW = ClearcaseUI.PLUGIN_ID + ".icon.decorateNew";

    /** decorator preference */
    String ICON_DECORATE_UNKNOWN = ClearcaseUI.PLUGIN_ID + ".icon.decorateUnknown";

    /** decorator preference */
    String ICON_DECORATE_EDITED = ClearcaseUI.PLUGIN_ID + ".icon.decorateEdited";

    /** decorator preference */
    String ICON_DECORATE_HIJACKED = ClearcaseUI.PLUGIN_ID + ".icon.decorateHijacked";
}