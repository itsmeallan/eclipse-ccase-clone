
package net.sourceforge.eclipseccase.views;

import org.eclipse.jface.dialogs.IDialogSettings;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.StateCache;
import net.sourceforge.eclipseccase.ui.ClearcaseUI;

/**
 * The Checkouts view
 */
public class CheckoutsView extends ClearcaseViewPart
{
    private static final String SETTING_HIDE_CHECKOUTS = "hideCheckouts";

    private static final String SETTING_HIDE_NEW_ELEMENTS = "hideNewElements";

    private static final String DIALOG_SETTINGS_STORE = "CheckoutsView";

    /** the dialog settings */
    private IDialogSettings settings;

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.eclipseccase.views.ClearcaseViewPart#shouldAdd(net.sourceforge.eclipseccase.StateCache)
     */
    protected boolean shouldAdd(StateCache stateCache)
    {
        if (null == stateCache
                || null == ClearcaseProvider.getClearcaseProvider(stateCache
                        .getResource())) return false;

        if (stateCache.isUninitialized()) return false;

        if (stateCache.isCheckedOut()) return !hideCheckouts();

        if ((!stateCache.hasRemote() && !ClearcaseProvider
                .getClearcaseProvider(stateCache.getResource()).isIgnored(
                        stateCache.getResource()))) return !hideNewElements();

        return false;
    }

    /**
     * Indicates if checkouts should not be shown.
     * 
     * @return
     */
    public boolean hideCheckouts()
    {
        return settings.getBoolean(SETTING_HIDE_CHECKOUTS);
    }

    /**
     * @param hide
     */
    public void setHideCheckouts(boolean hide)
    {
        if (hideCheckouts() != hide)
        {
            settings.put(SETTING_HIDE_CHECKOUTS, hide);
            refresh();
        }
    }

    /**
     * Indicates if new elements should not be shown.
     * 
     * @return
     */
    public boolean hideNewElements()
    {
        return settings.getBoolean(SETTING_HIDE_NEW_ELEMENTS);
    }

    /**
     * @param hide
     */
    public void setHideNewElements(boolean hide)
    {
        if (hideNewElements() != hide)
        {
            settings.put(SETTING_HIDE_NEW_ELEMENTS, hide);
            refresh();
        }
    }

    /**
     * Creates a new instance.
     */
    public CheckoutsView()
    {
        super();
        IDialogSettings dialogSettings = ClearcaseUI.getInstance()
                .getDialogSettings();
        settings = dialogSettings.getSection(DIALOG_SETTINGS_STORE);
        if (null == settings)
        {
            settings = dialogSettings.addNewSection(DIALOG_SETTINGS_STORE);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.eclipseccase.views.ClearcaseViewPart#makeActions()
     */
    protected void makeActions()
    {
        setActionGroup(new CheckoutsViewActionGroup(this));
    }
}