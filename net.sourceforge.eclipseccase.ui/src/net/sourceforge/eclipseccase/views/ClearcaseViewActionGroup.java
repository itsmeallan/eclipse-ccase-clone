/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 8, 2004
 */

package net.sourceforge.eclipseccase.views;

import net.sourceforge.eclipseccase.ui.ClearcaseImages;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.navigator.MainActionGroup;
import org.eclipse.ui.views.navigator.ShowInNavigatorAction;

/**
 * TODO Provide description for ClearcaseViewActionGroup.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class ClearcaseViewActionGroup extends MainActionGroup {

	/**
	 * Creates a new instance.
	 * 
	 * @param navigator
	 */
	public ClearcaseViewActionGroup(ClearcaseViewPart navigator) {
		super(navigator);
	}

	protected Action refreshAction;

	protected ShowInNavigatorAction showInNavigatorAction;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#makeActions()
	 */
	protected void makeActions() {
		super.makeActions();

		refreshAction = new Action(Messages.getString("ClearcaseViewActionGroup.refresh.name"), ClearcaseImages //$NON-NLS-1$
				.getImageDescriptor(ClearcaseImages.IMG_REFRESH)) {

			public void run() {
				getClearcaseView().refresh();
			}

		};
		refreshAction.setToolTipText(Messages.getString("ClearcaseViewActionGroup.refresh.description")); //$NON-NLS-1$
		refreshAction.setDisabledImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_REFRESH));

		showInNavigatorAction = new ShowInNavigatorAction(getClearcaseView().getSite().getPage(), getClearcaseView().getViewer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		IMenuManager menu = actionBars.getMenuManager();
		menu.add(showInNavigatorAction);
		menu.add(new Separator());

		super.fillActionBars(actionBars);

		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);

		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new Separator());
		toolBar.add(refreshAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		menu.add(showInNavigatorAction);
		menu.add(new Separator());

		super.fillContextMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#runDefaultAction(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void runDefaultAction(IStructuredSelection selection) {
		showInNavigatorAction.selectionChanged(selection);
		showInNavigatorAction.run();
	}

	/**
	 * @return
	 */
	protected ClearcaseViewPart getClearcaseView() {
		return ((ClearcaseViewPart) getNavigator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.navigator.MainActionGroup#updateActionBars()
	 */
	public void updateActionBars() {
		super.updateActionBars();

		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		showInNavigatorAction.selectionChanged(selection);
	}
}