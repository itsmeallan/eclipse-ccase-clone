package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ui.ClearcaseUI;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

// Dummy action which activates plugin by very act of being called.
//
public class ActivatePluginAction extends ActionDelegate implements IObjectActionDelegate, IWorkbenchWindowActionDelegate
{

	private static boolean firstTime = true;

	public ActivatePluginAction()
	{
		super();
	}

	public void dispose()
	{
	}

	public void init(IWorkbenchWindow window)
	{
	}

	public void run(IAction action)
	{
		MessageDialog.openInformation(
			ClearcaseUI.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell(),
			"Clearcase Plugin",
			"The Clearcase plugin has been activated");
		if (action != null)
			action.setEnabled(false);
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
		if (action != null && firstTime)
		{
			firstTime = false;
			action.setEnabled(true);
		}
		else
		{
			action.setEnabled(false);
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
	}

}
