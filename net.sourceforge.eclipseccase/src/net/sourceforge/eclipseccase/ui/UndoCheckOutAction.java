package net.sourceforge.eclipseccase.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.eclipseccase.StateCache;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @author conwaym
 *
 * To change this generated comment edit the template variable "typecomment":
 * Workbench>Preferences>Java>Templates.
 */
public class UndoCheckOutAction extends TeamAction
{
	public void run(IAction action)
	{
		run(new WorkspaceModifyOperation()
		{
			public void execute(IProgressMonitor monitor)
				throws InterruptedException, InvocationTargetException
			{
				try
				{
					Hashtable table = getProviderMapping();
					Set keySet = table.keySet();
					monitor.beginTask("Undoing checkout...", keySet.size() * 1000);
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext())
					{
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						RepositoryProvider provider = (RepositoryProvider) iterator.next();
						List list = (List) table.get(provider);
						IResource[] providerResources =
							(IResource[]) list.toArray(new IResource[list.size()]);
						provider.getSimpleAccess().uncheckout(
							providerResources,
							IResource.DEPTH_ZERO,
							subMonitor);
					}
				}
				catch (TeamException e)
				{
					throw new InvocationTargetException(e);
				}
				finally
				{
					monitor.done();
				}
			}
		}, "Undoing checkout", this.PROGRESS_DIALOG);
	}

	protected boolean isEnabled() throws TeamException
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			StateCache cache = StateCache.getState(resource);
			if (!cache.hasRemote())
				return false;
			if (!cache.isCheckedOut())
				return false;
		}
		return true;
	}

}