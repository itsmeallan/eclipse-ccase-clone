package net.sourceforge.eclipseccase.ui;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class CheckInAction extends TeamAction
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
				"Checkin comment",
				"Enter a checkin comment",
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
					IResource[] resources = getSelectedResources();
					monitor.beginTask("Checking in...", resources.length);
					for (int i = 0; i < resources.length; i++)
					{
						IResource resource = resources[i];
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
						provider.setComment(comment);
						provider.checkin(new IResource[] {resource},
										depth, subMonitor);
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
		}, "Checkin", this.PROGRESS_DIALOG);
	}

	protected boolean isEnabled()
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
			if (provider == null)
				return false;
			if (! provider.isCheckedOut(resource))
				return false;
		}
		return true;
	}

}