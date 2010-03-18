/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 8, 2004
 */

package net.sourceforge.eclipseccase.views;

import org.eclipse.ui.actions.OpenFileAction;

import net.sourceforge.eclipseccase.ui.ClearCaseImages;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.navigator.MainActionGroup;
import org.eclipse.ui.views.navigator.ShowInNavigatorAction;

/**
 * TODO Provide description for ClearCaseViewActionGroup.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class ClearCaseViewActionGroup extends MainActionGroup {

	/**
	 * Creates a new instance.
	 * 
	 * @param navigator
	 */
	public ClearCaseViewActionGroup(ClearCaseViewPart navigator) {
		super(navigator);
	}

	protected Action refreshAction;

	protected ShowInNavigatorAction showInNavigatorAction;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#makeActions()
	 */
	@Override
	protected void makeActions() {
		super.makeActions();

		refreshAction = new Action(Messages.getString("ClearCaseViewActionGroup.refresh.name"), ClearCaseImages //$NON-NLS-1$
				.getImageDescriptor(ClearCaseImages.IMG_REFRESH)) {

			@Override
			public void run() {
				getClearCaseView().refresh();
			}

		};
		refreshAction.setToolTipText(Messages.getString("ClearCaseViewActionGroup.refresh.description")); //$NON-NLS-1$
		refreshAction.setDisabledImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_REFRESH));

		// showInNavigatorAction = new
		// ShowInNavigatorAction(getClearCaseView().getSite().getPage(),
		// getClearCaseView().getViewer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.navigator.MainActionGroup#fillActionBars(org.eclipse
	 * .ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {

		// add only part of super.fillActionBars(actionBars) :
		workingSetGroup.fillActionBars(actionBars);
		sortAndFilterGroup.fillActionBars(actionBars);

		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);

		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new Separator());
		toolBar.add(refreshAction);
		toolBar.add(new Separator());
	}

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
		OpenFileAction ofa = new OpenFileAction(getClearCaseView().getSite().getPage());
		ofa.selectionChanged(selection);
		if (ofa.isEnabled()) {
			ofa.run();
		}
	}

	/**
	 * @return
	 */
	protected ClearCaseViewPart getClearCaseView() {
		return ((ClearCaseViewPart) getNavigator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#updateActionBars()
	 */
	@Override
	public void updateActionBars() {
		super.updateActionBars();

		// IStructuredSelection selection = (IStructuredSelection)
		// getContext().getSelection();
		// showInNavigatorAction.selectionChanged(selection);
	}
}