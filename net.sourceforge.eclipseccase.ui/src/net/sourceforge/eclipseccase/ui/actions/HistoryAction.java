package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import java.util.Vector;
import net.sourceforge.clearcase.*;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.views.HistoryView;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Pulls up the clearcase history
 */
public class HistoryAction extends ClearcaseWorkspaceAction {
	IResource[] resources = null;

	private HistoryView view = null;

	/**
	 * {@inheritDoc

	 */
	@Override
	public boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearcaseElement(resource))
				return false;
		}
		return true;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {
		resources = getSelectedResources();
		try {
			view = (HistoryView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("net.sourceforge.eclipseccase.views.HistoryView");
		} catch (Exception e) {
			e.printStackTrace();
		}

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					if (resources != null && resources.length > 0) {
						IResource resource = resources[0];
						String path = resource.getLocation().toOSString();

						// ClearCaseInterface cci =
						// ClearCase.createInterface(ClearCase.INTERFACE_CLI);
						ClearCaseInterface cci = ClearcasePlugin.getEngine();
						Vector<ElementHistory> result = cci.getElementHistory(path);
						view.setHistoryInformation(resources[0], result);
					}
				} catch (Exception e) {

				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "History");

	}

}