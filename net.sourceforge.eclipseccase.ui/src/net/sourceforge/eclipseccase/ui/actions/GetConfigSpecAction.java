package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.views.ConfigSpecView;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

public class GetConfigSpecAction extends ClearcaseWorkspaceAction {
	private ConfigSpecView view = null;

	private IResource[] resources = null;

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean isEnabled() {
		boolean bRes = true;

		IResource[] resources = getSelectedResources();
		if (resources.length != 0) {
			for (int i = 0; (i < resources.length) && (bRes); i++) {
				IResource resource = resources[i];
				ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
				if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearcaseElement(resource)) {
					bRes = false;
				}
			}
		} else {
			bRes = false;
		}

		return bRes;

	}

	@Override
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		resources = getSelectedResources();

		try {
			view = (ConfigSpecView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("net.sourceforge.eclipseccase.views.ConfigSpecView");
		} catch (Exception e) {
			e.printStackTrace();
		}

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				monitor.done();
				try {
					if (resources != null && resources.length != 0) {
						view.loadConfigSpec(resources[0]);
					}
				} catch (Exception e) {
				} finally {
				}
			}
		};

		executeInBackground(runnable, "Get Config Spec");
	}
}
