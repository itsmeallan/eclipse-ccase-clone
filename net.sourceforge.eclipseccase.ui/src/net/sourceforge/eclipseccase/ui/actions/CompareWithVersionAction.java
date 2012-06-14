package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.operation.CompareResourcesOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the compare with predecessor dialog.
 */
public class CompareWithVersionAction extends ClearCaseWorkspaceAction {

	private IResource resource = null;

	private String versionA = null;

	private String versionB = null;

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public void setVersionA(String versionA) {
		this.versionA = versionA;
	}

	public void setVersionB(String versionB) {
		this.versionB = versionB;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		if (resource != null) {
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider != null && !provider.isUnknownState(resource) && !provider.isIgnored(resource) && provider.isClearCaseElement(resource))
				return true;
		}

		return false;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {
		ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
		if (provider != null) {
			CompareResourcesOperation mainOp = new CompareResourcesOperation(resource, versionA, versionB, provider);
			mainOp.compare();

		}
	}
}
