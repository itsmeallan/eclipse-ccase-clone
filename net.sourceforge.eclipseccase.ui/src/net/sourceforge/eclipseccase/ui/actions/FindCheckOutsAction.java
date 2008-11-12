package net.sourceforge.eclipseccase.ui.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import net.sourceforge.clearcase.commandline.CleartoolCommandLine;
import net.sourceforge.clearcase.commandline.CommandLauncher;
import net.sourceforge.clearcase.utils.Os;
import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the clearcase version tree for the element
 */
public class FindCheckOutsAction extends ClearcaseWorkspaceAction {

	/**
	 * @see TeamAction#isEnabled()
	 */
	public boolean isEnabled() {
		// workaround for bug 960292
		if (!Os.isFamily(Os.WINDOWS))
			return false;

		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() == IResource.FILE)
				return false;

			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource))
				return false;
		}
		return true;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IResource[] resources = getSelectedResources();
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						String path = resource.getLocation().toOSString();
						if (ClearcasePlugin.isUseCleartool()) {
							new CommandLauncher().execute(new CleartoolCommandLine("lscheckout").addOption("-graphical").addElement(path).create(), null, null, null);
						} else {
							Runtime.getRuntime().exec(new String[] { "clearfindco", resource.getLocation().toOSString() });
						}
					}
				} catch (IOException ex) {
				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Find checkouts");
	}

}