/*
 * Copyright (c) 2004 Intershop (www.intershop.de)
 * Created on Apr 13, 2004
 */
package net.sourceforge.eclipseccase.views;

import net.sourceforge.eclipseccase.ui.ClearCaseImages;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.views.navigator.MainActionGroup;
import org.eclipse.ui.views.navigator.ShowInNavigatorAction;

/**
 * TODO Provide description for CheckoutsViewActionGroup.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class CheckoutsViewActionGroup extends MainActionGroup {

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

	private Action refreshAction;

	private ShowInNavigatorAction showInNavigatorAction;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.navigator.MainActionGroup#fillContextMenu(org.eclipse
	 * .jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		// menu.add(showInNavigatorAction);
		// menu.add(new Separator());

		super.fillContextMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.navigator.MainActionGroup#runDefaultAction(org.eclipse
	 * .jface.viewers.IStructuredSelection)
	 */
	@Override
	public void runDefaultAction(IStructuredSelection selection) {
		// double click should open file in editor, not in navigator
		// see bug 2964016: Not possible to open file from 'view private files'

		// showInNavigatorAction.selectionChanged(selection);
		// showInNavigatorAction.run();
		OpenFileAction ofa = new OpenFileAction(getCheckoutsView().getSite().getPage());
		ofa.selectionChanged(selection);
		if (ofa.isEnabled()) {
			ofa.run();
		}
	}

	/**
	 * @return
	 */
	protected CheckoutsView getCheckoutsView() {
		return ((CheckoutsView) getNavigator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.eclipseccase.views.ClearCaseViewActionGroup#fillActionBars
	 * (org.eclipse.ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		workingSetGroup.fillActionBars(actionBars);
		sortAndFilterGroup.fillActionBars(actionBars);

		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);

		IToolBarManager toolBar = actionBars.getToolBarManager();
		IMenuManager menu = actionBars.getMenuManager();
		IMenuManager submenu = new MenuManager("Show...");

		menu.add(submenu);
		submenu.add(hideCheckouts);
		submenu.add(hideHijackedElements);
		submenu.add(hideNewElements);
		menu.add(new Separator());

		toolBar.add(new Separator());
		toolBar.add(hideCheckouts);
		toolBar.add(hideHijackedElements);
		toolBar.add(hideNewElements);
		toolBar.add(new Separator());
		toolBar.add(refreshAction);
		toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

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

		refreshAction = new Action(Messages.getString("ClearCaseViewActionGroup.refresh.name"), ClearCaseImages //$NON-NLS-1$
				.getImageDescriptor(ClearCaseImages.IMG_REFRESH)) {

			@Override
			public void run() {
				getCheckoutsView().refreshFromClearCase();
				getCheckoutsView().refresh();
			}
		};
		refreshAction.setToolTipText(Messages.getString("ClearCaseViewActionGroup.refresh.description")); //$NON-NLS-1$
		refreshAction.setDisabledImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_REFRESH));

		// showInNavigatorAction = new
		// ShowInNavigatorAction(getClearCaseView().getSite().getPage(),
		// getClearCaseView().getViewer());

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
