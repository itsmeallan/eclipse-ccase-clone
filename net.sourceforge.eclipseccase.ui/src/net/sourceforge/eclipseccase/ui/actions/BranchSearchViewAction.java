package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.views.BranchSearchView;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

public class BranchSearchViewAction extends ClearCaseWorkspaceAction {
	IResource[] resources = null;

	private BranchSearchView view = null;

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
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource.getProject());
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource))
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
		if (resources.length == 0)
			return;

		ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resources[0].getProject());
		if (provider == null)
			return;

		try {
			view = (BranchSearchView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("net.sourceforge.eclipseccase.views.BranchSearchView");

			view.setProject(resources[0].getProject());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
