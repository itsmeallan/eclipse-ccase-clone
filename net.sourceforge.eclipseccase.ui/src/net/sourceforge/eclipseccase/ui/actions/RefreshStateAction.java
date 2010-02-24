package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.Team;

public class RefreshStateAction extends ClearCaseWorkspaceAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	@Override
	public void execute(IAction action) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(final IProgressMonitor monitor) throws CoreException {
				try {
					IResource[] resources = getSelectedResources();
					beginTask(monitor, "Refreshing state...", resources.length);

					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						checkCanceled(monitor);
						ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
						provider.refreshRecursive(resource, new SubProgressMonitor(monitor, 1));
					}

				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Refreshing state");
	}

	@Override
	public boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		if (resources.length == 1) {

			// always ignore derived resources
			if (Team.isIgnoredHint(resources[0]))
				return false;

			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resources[0]);
			if (provider == null)
				return false;
		}
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null)
				return false;
		}
		return true;
	}

}