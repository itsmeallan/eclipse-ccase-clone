/*
 * Copyright (c) 2004 Intershop (www.intershop.de)
 * Created on Apr 13, 2004
 */
package net.sourceforge.eclipseccase.views;

import net.sourceforge.eclipseccase.ui.ClearCaseImages;
import org.eclipse.jface.action.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;

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
		IMenuManager submenu = new MenuManager("Show...");
		menu.add(submenu);
		submenu.add(hideCheckouts);
		submenu.add(hideHijackedElements);
		submenu.add(hideNewElements);
		menu.add(new Separator());

		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new Separator());
		toolBar.add(hideCheckouts);
		toolBar.add(hideHijackedElements);
		toolBar.add(hideNewElements);
		toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

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

		hideCheckouts = new Action("Checked-Out Elements") {
			@Override
			public void run() {
				getCheckoutsView().setHideCheckouts(!getCheckoutsView().hideCheckouts());
			}
		};
		hideCheckouts.setToolTipText("Show Checked-Out Elements");
		hideCheckouts.setImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_ELEM_CO));

		hideHijackedElements = new Action("Hijacked Elements") {
			@Override
			public void run() {
				getCheckoutsView().setHideHijackedElements(!getCheckoutsView().hideHijackedElements());
			}

		};
		hideHijackedElements.setToolTipText("Show Hijacked Elements");
		hideHijackedElements.setImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_ELEM_HJ));

		hideNewElements = new Action("Other View-Private Files/Folders") {
			@Override
			public void run() {
				getCheckoutsView().setHideNewElements(!getCheckoutsView().hideNewElements());
			}

		};
		hideNewElements.setImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_ELEM_UNK));
		hideNewElements.setToolTipText("Show other view-private stuff, e.g. new elements");
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

		hideCheckouts.setChecked(!getCheckoutsView().hideCheckouts());
		hideHijackedElements.setChecked(!getCheckoutsView().hideHijackedElements());
		hideNewElements.setChecked(!getCheckoutsView().hideNewElements());
	}
}
