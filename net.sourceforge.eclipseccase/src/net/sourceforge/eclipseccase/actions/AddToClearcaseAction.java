package net.sourceforge.eclipseccase.actions;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.CommentDialog;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class AddToClearcaseAction extends ClearcaseAction
{

	private String lastComment = "";

	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action)
	{
		String maybeComment = "";
		int maybeDepth = IResource.DEPTH_ZERO;
		
		if (ClearcasePlugin.isAddComment())
		{
			CommentDialog dlg =
				new CommentDialog(
					shell,
					"Add to clearcase comment",
					"Enter a comment",
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
					monitor.beginTask("Adding to clearcase", resources.length);
					for (int i = 0; i < resources.length; i++)
					{
						IResource resource = resources[i];
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
						provider.setComment(comment);
						provider.add(new IResource[] {resource},
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
		}, "Adding to clearcase", TeamAction.PROGRESS_DIALOG);
		
		updateActionEnablement();
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
			ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
			if (provider == null || provider.isUnknownState(resource))
				return false;

			// Projects may be the view directory containing the VOBS, if so,
			// don't want to be able to add em, or any resource diretcly under them
			if (resource.getType() == IResource.PROJECT && ! provider.hasRemote(resource))
				return false;
			if (resource.getParent().getType() == IResource.PROJECT &&
				! provider.hasRemote(resource.getParent()))
				return false;
			if (provider.hasRemote(resource))
				return false;
		}
		return true;
	}

}