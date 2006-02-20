/*
 * Copyright (c) 2004 Intershop (www.intershop.de)
 * Created on Apr 13, 2004
 */
package net.sourceforge.eclipseccase.views;

import org.eclipse.jface.action.*;
import org.eclipse.ui.IActionBars;

/**
 * TODO Provide description for CheckoutsViewActionGroup.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class CheckoutsViewActionGroup extends ClearcaseViewActionGroup {

	/**
	 * Creates a new instance.
	 * 
	 * @param navigator
	 */
	public CheckoutsViewActionGroup(CheckoutsView checkoutsView) {
		super(checkoutsView);
	}

	private Action hideCheckouts;

	private Action hideNewElements;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.views.ClearcaseViewActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		IMenuManager menu = actionBars.getMenuManager();
		IMenuManager submenu = new MenuManager("Hide...");
		menu.add(submenu);
		submenu.add(hideCheckouts);
		submenu.add(hideNewElements);
		menu.add(new Separator());

		super.fillActionBars(actionBars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.views.ClearcaseViewActionGroup#makeActions()
	 */
	protected void makeActions() {
		super.makeActions();

		hideCheckouts = new Action("checkouts") {
			public void run() {
				getCheckoutsView().setHideCheckouts(!getCheckoutsView().hideCheckouts());
			}

		};
		hideCheckouts.setToolTipText("Hide checked out elements");
		hideNewElements = new Action("new elements") {
			public void run() {
				getCheckoutsView().setHideNewElements(!getCheckoutsView().hideNewElements());
			}

		};
		hideNewElements.setToolTipText("Hide new elements");
	}

	/**
	 * @return
	 */
	protected CheckoutsView getCheckoutsView() {
		return (CheckoutsView) getClearcaseView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#updateActionBars()
	 */
	public void updateActionBars() {
		super.updateActionBars();

		hideCheckouts.setChecked(getCheckoutsView().hideCheckouts());
		hideNewElements.setChecked(getCheckoutsView().hideNewElements());
	}
}
