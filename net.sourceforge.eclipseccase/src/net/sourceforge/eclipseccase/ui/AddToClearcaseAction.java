package net.sourceforge.eclipseccase.ui;

import java.lang.reflect.InvocationTargetException;
import java.security.Policy;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.StateCache;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class AddToClearcaseAction extends TeamAction
{

	private String lastComment = "";

	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action)
	{
		CheckinDialog dlg =
			new CheckinDialog(
				shell,
				"Add to clearcase comment",
				"Enter a comment",
				lastComment,
				null);
		if (dlg.open() == dlg.CANCEL)
			return;
		final String comment = dlg.getValue();
		final int depth =
			dlg.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
		lastComment = comment;
		run(new WorkspaceModifyOperation()
		{
			public void execute(IProgressMonitor monitor)
				throws InterruptedException, InvocationTargetException
			{
				try
				{
					Hashtable table = getProviderMapping();
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000);
					monitor.setTaskName("Adding to clearcase");
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext())
					{
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						RepositoryProvider provider = (RepositoryProvider) iterator.next();
						List list = (List) table.get(provider);
						IResource[] providerResources =
							(IResource[]) list.toArray(new IResource[list.size()]);
						if (provider instanceof ClearcaseProvider)
						{
							ClearcaseProvider ccprovider = (ClearcaseProvider) provider.getSimpleAccess();
							ccprovider.setComment(comment);
							ccprovider.add(providerResources, depth, subMonitor);
						}
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
		}, "Adding to clearcase", this.PROGRESS_DIALOG);
	}
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
			StateCache cache = StateCache.getState(resource);
			if (cache.hasRemote())
				return false;
		}
		return true;
	}
}