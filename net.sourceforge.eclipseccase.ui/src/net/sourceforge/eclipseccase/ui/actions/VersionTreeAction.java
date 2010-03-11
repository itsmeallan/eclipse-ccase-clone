package net.sourceforge.eclipseccase.ui.actions;

import java.io.File;
import java.io.IOException;
import net.sourceforge.clearcase.commandline.CleartoolCommandLine;
import net.sourceforge.clearcase.commandline.CommandLauncher;
import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the clearcase version tree for the element
 */
public class VersionTreeAction extends ClearCaseWorkspaceAction {
	IResource forceResource = null;

	public void setResource(IResource resource) {
		this.forceResource = resource;
	}

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
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
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
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IResource[] resources;

					if (forceResource != null) {
						resources = new IResource[1];
						resources[0] = forceResource;
					} else {
						resources = getSelectedResources();
					}
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						String path = resource.getLocation().toOSString();
						ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);
						if (p.isHijacked(resource)) {
							p.showVersionTree(path + "@@/");
						} else {
							p.showVersionTree(path);
						}
					}

				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Version Tree");
	}
}