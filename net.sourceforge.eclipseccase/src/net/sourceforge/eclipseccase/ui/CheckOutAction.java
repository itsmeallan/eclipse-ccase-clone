package net.sourceforge.eclipseccase.ui;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class CheckOutAction extends TeamAction
{
	private String lastComment = "";

	public void run(IAction action)
	{
		String maybeComment = "";
		int maybeDepth = IResource.DEPTH_ZERO;
		
		if (ClearcasePlugin.isCheckoutComment())
		{
			CommentDialog dlg =
				new CommentDialog(
					shell,
					"Checkout comment",
					"Enter a checkout comment",
					lastComment,
					null);
			if (dlg.open() == CommentDialog.CANCEL)
				return;
			maybeComment = dlg.getValue();
			maybeDepth =
				dlg.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
		}

		final String comment = maybeComment;
		final int depth = maybeDepth;
		lastComment = comment;
		run(new WorkspaceModifyOperation()
		{
			public void execute(IProgressMonitor monitor)
				throws InterruptedException, InvocationTargetException
			{
				try
				{
					IResource[] resources = getSelectedResources();
					monitor.beginTask("Checking out...", resources.length);
					for (int i = 0; i < resources.length; i++)
					{
						IResource resource = resources[i];
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
						provider.setComment(comment);
						provider.checkout(new IResource[] {resource},
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
		}, "Checking out", TeamAction.PROGRESS_DIALOG);
	}

	protected boolean isEnabled() throws TeamException
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
			if (provider == null || provider.isUnknownState(resource))
				return false;
			if (! provider.hasRemote(resource))
				return false;
			if (provider.isCheckedOut(resource))
				return false;
		}
		return true;
	}

}