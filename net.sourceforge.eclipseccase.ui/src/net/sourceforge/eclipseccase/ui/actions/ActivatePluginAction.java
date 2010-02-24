package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ui.ClearCaseUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;

// Dummy action which activates plugin by very act of being called.
//
public class ActivatePluginAction extends ActionDelegate implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {

	private static boolean firstTime = true;

	public ActivatePluginAction() {
		super();
	}

	@Override
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	@Override
	public void run(IAction action) {
		MessageDialog.openInformation(ClearCaseUI.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell(), "ClearCase Plugin", "The ClearCase plugin has been activated");
		if (action != null) {
			action.setEnabled(false);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (action != null && firstTime) {
			firstTime = false;
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
