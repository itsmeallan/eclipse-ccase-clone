package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Pulls up the clearcase version tree for the element
 */
public class FindCheckOutsAction extends ClearcaseWorkspaceAction {

	/**
	 * {@inheritDoc}
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

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("net.sourceforge.eclipseccase.views.CheckoutsView");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}