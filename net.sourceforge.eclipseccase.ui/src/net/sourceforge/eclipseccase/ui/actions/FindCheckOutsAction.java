package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.views.ConfigSpecView;
import org.eclipse.ui.PlatformUI;

import java.io.IOException;
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
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the clearcase version tree for the element
 */
public class FindCheckOutsAction extends ClearcaseWorkspaceAction {

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		boolean bRes = true;

		IResource[] resources = getSelectedResources();
		if (resources.length != 0)
		{
			for (int i = 0; (i < resources.length) && (bRes); i++)
			{
				IResource resource = resources[i];
				ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
				if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.hasRemote(resource))
					bRes = false;
			}
		}
		else
		{
			bRes = false;
		}

		return bRes;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("net.sourceforge.eclipseccase.views.CheckoutsView");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}