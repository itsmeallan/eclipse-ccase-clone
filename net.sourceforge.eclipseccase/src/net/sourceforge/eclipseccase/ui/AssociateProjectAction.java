package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;

public class AssociateProjectAction extends TeamAction
{

	/** (non-Javadoc)
	 * Method declared on IDropActionDelegate
	 */
	public void run(IAction action)
	{
		IProject[] projects = getSelectedProjects();
		for (int i = 0; i < projects.length; i++)
		{
			try
			{
				final IProject project = projects[i];
				String projectPath = project.getLocation().toOSString();
				if (! ClearcasePlugin.getEngine().getViewName(projectPath).status)
					throw new TeamException("The Project directory must exist within a clearcase view");
				RepositoryProvider.map(project, ClearcaseProvider.ID);
				Team.removeNatureFromProject(project, ClearcaseProvider.ID, new NullProgressMonitor());
				final ClearcaseProvider provider = ClearcaseProvider.getProvider((IResource) project);
				Thread t = new Thread()
				{
					public void run()
					{
						try
						{
							provider.refresh(new IResource[] {project}, IResource.DEPTH_INFINITE, null);
						}
						catch (TeamException e)
						{
							ClearcasePlugin.log(IStatus.ERROR, "Problems refreshing clearcase state for project", e);
						}
					}
				};
				t.start();
				MessageDialog.openInformation(
					shell,
					"Clearcase Plugin",
					"Associated project '" + projects[i].getName() + "' with clearcase");
			}
			catch (TeamException e)
			{
				ErrorDialog.openError(
					shell,
					"Clearcase Error",
					"Could not associate project '" + projects[i].getName() + "' with clearcase",
					e.getStatus());
			}
		}
	}

	protected boolean isEnabled() throws TeamException
	{
		IProject[] projects = getSelectedProjects();
		if (projects.length == 0)
			return false;
		for (int i = 0; i < projects.length; i++)
		{
			IResource resource = projects[i];
			ClearcaseProvider provider =
				ClearcaseProvider.getProvider(resource);
			if (provider != null)
				return false;
		}
		return true;
	}

}