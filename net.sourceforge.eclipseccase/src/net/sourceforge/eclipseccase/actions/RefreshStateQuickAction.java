package net.sourceforge.eclipseccase.actions;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.eclipseccase.ClearcaseProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class RefreshStateQuickAction
	extends ClearcaseAction
{
	/*
	 * Method declared on IActionDelegate.
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
					monitor.beginTask("Refreshing state...", resources.length);
					for (int i = 0; i < resources.length; i++)
					{
						IResource resource = resources[i];
						IProgressMonitor subMonitor =
							new SubProgressMonitor(monitor, 1000);
						ClearcaseProvider provider =
							ClearcaseProvider.getProvider(resource);
						provider.refreshQuick(
							new IResource[] { resource },
							IResource.DEPTH_INFINITE,
							subMonitor);
						monitor.worked(1);
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
		}, "Refreshing state", TeamAction.PROGRESS_DIALOG);

		updateActionEnablement();
	}

	protected boolean isEnabled()
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			ClearcaseProvider provider =
				ClearcaseProvider.getProvider(resource);
			if (provider == null)
				return false;
		}
		return true;
	}

}