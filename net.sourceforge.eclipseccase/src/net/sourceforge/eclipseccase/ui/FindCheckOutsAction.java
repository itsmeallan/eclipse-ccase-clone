/**
 * Created on Apr 10, 2002
 *
 * To change this generated comment edit the template variable "filecomment":
 * Workbench>Preferences>Java>Templates.
 */
package net.sourceforge.eclipseccase.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 *  Pulls up the clearcase version tree for the element
 */
public class FindCheckOutsAction extends TeamAction
{

	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			if (resource.getType() == IResource.FILE)
				return false;

			ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
			if (provider == null || provider.isUnknownState(resource))
				return false;
			if (! provider.hasRemote(resource))
				return false;
		}
		return true;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action)
	{
		run(new WorkspaceModifyOperation()
		{
			public void execute(IProgressMonitor monitor)
				throws InterruptedException, InvocationTargetException
			{
				try
				{
					IResource[] resources = getSelectedResources();
					for (int i = 0; i < resources.length; i++)
					{
						IResource resource = resources[i];
						String path = resource.getLocation().toOSString();
						if (ClearcasePlugin.isUseCleartool())
						{
							ClearcasePlugin.getEngine().cleartool("lscheckout -graphical \"" + path + "\"");
						}
						else
						{
							Runtime.getRuntime().exec(new String[] {"clearfindco", resource.getLocation().toOSString()});
						}
					}
				}
				catch (IOException ex)
				{
					throw new InvocationTargetException(ex);
				}
			}
		}, "Find checkouts", this.PROGRESS_BUSYCURSOR);
	}

}