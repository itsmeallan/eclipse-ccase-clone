package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;

public class UpdateAction extends ClearcaseWorkspaceAction {
	public void run(IAction action) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IResource[] resources = getSelectedResources();
					beginTask(monitor, "Updating...", resources.length);
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
						if (null != provider)
							provider.get(new IResource[] { resource }, IResource.DEPTH_ZERO, subMonitor(monitor));
					}
				} finally {
					monitor.done();
				}
			}
		};
		executeInBackground(runnable, "Updating resources from ClearCase");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.hasRemote(resource))
				return false;
			if (!provider.isSnapShot(resource))
				return false;
		}
		return true;
	}

}