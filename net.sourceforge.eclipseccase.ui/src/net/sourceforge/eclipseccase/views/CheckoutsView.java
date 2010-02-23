package net.sourceforge.eclipseccase.views;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.ClearcaseUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * The Checkouts view
 */
public class CheckoutsView extends ClearcaseViewPart {
	private static final String SETTING_HIDE_CHECKOUTS = "hideCheckouts";

	private static final String SETTING_HIDE_NEW_ELEMENTS = "hideNewElements";

	private static final String SETTING_HIDE_HIJACKED_ELEMENTS = "hideHijackedElements";

	private static final String DIALOG_SETTINGS_STORE = "CheckoutsView";

	/** the dialog settings */
	private IDialogSettings settings;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.eclipseccase.views.ClearcaseViewPart#shouldAdd(org.eclipse
	 * .core.resources.IResource)
	 */
	@Override
	protected boolean shouldAdd(IResource resource) {
		ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);

		if (null == provider)
			return false;

		// don't show resources with unknown state
		if (provider.isUnknownState(resource))
			return false;

		// show checkouts if enabled
		if (provider.isCheckedOut(resource))
			return !hideCheckouts();

		// show Hijacked files if enabled
		if (provider.isHijacked(resource))
			return !hideHijackedElements();

		// show new elements if enabled
		if (!provider.isClearcaseElement(resource))
			return !hideNewElements();

		// hide all other
		return false;
	}

	/**
	 * Indicates if checkouts should not be shown.
	 * 
	 * @return
	 */
	public boolean hideCheckouts() {
		return settings.getBoolean(SETTING_HIDE_CHECKOUTS);
	}

	/**
	 * @param hide
	 */
	public void setHideCheckouts(boolean hide) {
		if (hideCheckouts() != hide) {
			settings.put(SETTING_HIDE_CHECKOUTS, hide);
			refresh();
		}
	}

	/**
	 * Indicates if new elements should not be shown.
	 * 
	 * @return
	 */
	public boolean hideNewElements() {
		return settings.getBoolean(SETTING_HIDE_NEW_ELEMENTS);
	}

	/**
	 * @param hide
	 */
	public void setHideNewElements(boolean hide) {
		if (hideNewElements() != hide) {
			settings.put(SETTING_HIDE_NEW_ELEMENTS, hide);
			refresh();
		}
	}

	/**
	 * Indicates if hijacked should not be shown.
	 * 
	 * @return
	 */
	public boolean hideHijackedElements() {
		return settings.getBoolean(SETTING_HIDE_HIJACKED_ELEMENTS);
	}

	/**
	 * @param hide
	 */
	public void setHideHijackedElements(boolean hide) {
		if (hideHijackedElements() != hide) {
			settings.put(SETTING_HIDE_HIJACKED_ELEMENTS, hide);
			refresh();
		}
	}

	/**
	 * Creates a new instance.
	 */
	public CheckoutsView() {
		super();
		IDialogSettings dialogSettings = ClearcaseUI.getInstance().getDialogSettings();
		settings = dialogSettings.getSection(DIALOG_SETTINGS_STORE);
		if (null == settings) {
			settings = dialogSettings.addNewSection(DIALOG_SETTINGS_STORE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.views.ClearcaseViewPart#makeActions()
	 */
	@Override
	protected void makeActions() {
		setActionGroup(new CheckoutsViewActionGroup(this));
	}
}