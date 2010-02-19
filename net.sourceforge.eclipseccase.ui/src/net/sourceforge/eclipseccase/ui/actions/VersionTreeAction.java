package net.sourceforge.eclipseccase.ui.actions;

import java.io.File;
import java.io.IOException;
import net.sourceforge.clearcase.commandline.CleartoolCommandLine;
import net.sourceforge.clearcase.commandline.CommandLauncher;
import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
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
public class VersionTreeAction extends ClearcaseWorkspaceAction {
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
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.hasRemote(resource))
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
					ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						String path = resource.getLocation().toOSString();
						File workingDir = null;
						if (resource.getType() == IResource.FOLDER) {
							workingDir = new File(resource.getLocation().toOSString());
						} else {
							workingDir = new File(resource.getLocation().toOSString()).getParentFile();
						}

						if (ClearcasePlugin.isUseCleartool()) {
							ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
							if (provider.isHijacked(resource)) {
								new CommandLauncher().execute(new CleartoolCommandLine("lsvtree").addOption("-graphical").addElement(path + "@@/").create(), workingDir, null, opListener);
							} else {
								new CommandLauncher().execute(new CleartoolCommandLine("lsvtree").addOption("-graphical").addElement(path).create(), workingDir, null, opListener);
							}
						} else {
							Runtime.getRuntime().exec(new String[] { "clearvtree", resource.getLocation().toOSString() });

						}
					}
				} catch (IOException ex) {

				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Version Tree");
	}
}