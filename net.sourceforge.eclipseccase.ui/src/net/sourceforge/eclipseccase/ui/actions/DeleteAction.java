package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.internal.ui.actions.TeamAction;

public class DeleteAction extends ClearCaseWorkspaceAction {
	@Override
	public void execute(IAction action) {
		boolean confirmed = MessageDialog.openConfirm(getShell(), "Confirm delete", "Are you sure you want to remove the selected elements from clearcase (rmname)?");
		if (!confirmed)
			return;
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				try {
					IResource[] resources = getSelectedResources();
					beginTask(monitor, "Deleting...", resources.length);
					ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);

						provider.setOperationListener(opListener);
						provider.delete(new IResource[] { resource }, subMonitor(monitor));
					}
				} finally {
					monitor.done();
				}
			}
		};

		executeInForeground(runnable, TeamAction.PROGRESS_DIALOG, "Deleting resources from ClearCase");
	}

	/**
	 * @see TeamAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource))
				return false;
		}
		return true;
	}

}