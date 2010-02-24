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
public class CheckoutsViewActionGroup extends ClearCaseViewActionGroup {

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

	private Action hideHijackedElements;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.eclipseccase.views.ClearCaseViewActionGroup#fillActionBars
	 * (org.eclipse.ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		IMenuManager menu = actionBars.getMenuManager();
		IMenuManager submenu = new MenuManager("Hide...");
		menu.add(submenu);
		submenu.add(hideCheckouts);
		submenu.add(hideHijackedElements);
		submenu.add(hideNewElements);
		menu.add(new Separator());

		super.fillActionBars(actionBars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.eclipseccase.views.ClearCaseViewActionGroup#makeActions()
	 */
	@Override
	protected void makeActions() {
		super.makeActions();

		hideCheckouts = new Action("checkouts") {
			@Override
			public void run() {
				getCheckoutsView().setHideCheckouts(!getCheckoutsView().hideCheckouts());
			}

		};
		hideCheckouts.setToolTipText("Hide checked out elements");
		hideHijackedElements = new Action("Hijacked elements") {
			@Override
			public void run() {
				getCheckoutsView().setHideHijackedElements(!getCheckoutsView().hideHijackedElements());
			}

		};
		hideHijackedElements.setToolTipText("Hide Hijacked elements");

		hideNewElements = new Action("new elements") {
			@Override
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
		return (CheckoutsView) getClearCaseView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#updateActionBars()
	 */
	@Override
	public void updateActionBars() {
		super.updateActionBars();

		hideCheckouts.setChecked(getCheckoutsView().hideCheckouts());
		hideHijackedElements.setChecked(getCheckoutsView().hideHijackedElements());
		hideNewElements.setChecked(getCheckoutsView().hideNewElements());
	}
}
