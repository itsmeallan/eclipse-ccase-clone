package net.sourceforge.eclipseccase.ui;

import java.lang.reflect.InvocationTargetException;
import java.security.Policy;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @author conwaym
 *
 * To change this generated comment edit the template variable "typecomment":
 * Workbench>Preferences>Java>Templates.
 */
public class UndoCheckOutAction extends org.eclipse.team.ui.actions.UndoCheckOutAction
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

}