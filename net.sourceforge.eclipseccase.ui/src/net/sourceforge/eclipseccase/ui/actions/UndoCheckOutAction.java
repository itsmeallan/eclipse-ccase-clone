package net.sourceforge.eclipseccase.ui.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;

/**
 * @author conwaym
 *
 * To change this generated comment edit the template variable "typecomment":
 * Workbench>Preferences>Java>Templates.
 */
public class UndoCheckOutAction extends ClearcaseWorkspaceAction
{
	public void run(IAction action)
	{
        IWorkspaceRunnable runnable = new IWorkspaceRunnable()
        {
            public void run(IProgressMonitor monitor) throws CoreException
            {
				try
				{
					IResource[] resources = getSelectedResources();
					monitor.beginTask("Undoing checkout...", resources.length * 1000);
					
					// Sort resources with directories last so that the modification of a
					// directory doesn't abort the modification of files within it.
					List resList = Arrays.asList(resources);
					Collections.sort(resList, new DirectoryLastComparator());
					
					for (int i = 0; i < resources.length; i++)
					{
						IResource resource = resources[i];
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
						provider.uncheckout(new IResource[] {resource},
										IResource.DEPTH_ZERO, subMonitor);
					}
				}
				finally
				{
					monitor.done();
				}
			}
        };
        executeInBackground(runnable, "Undoing checkout");
	}

	protected boolean isEnabled() throws TeamException
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
            if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.hasRemote(resource))
                return false;
			if (!provider.isCheckedOut(resource))
				return false;
		}
		return true;
	}

}