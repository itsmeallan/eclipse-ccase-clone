package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.StateCache;
import net.sourceforge.eclipseccase.StateCacheFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class DissociateProjectAction extends ClearcaseAction
{

	/** (non-Javadoc)
	 * Method declared on IDropActionDelegate
	 */
	public void run(IAction action)
	{
		final StringBuffer message = new StringBuffer();
		run(new WorkspaceModifyOperation()
		{
			public void execute(IProgressMonitor monitor)
				throws InterruptedException, InvocationTargetException
			{
				IProject[] projects = getSelectedProjects();
				monitor.beginTask("Adding to clearcase", projects.length);

				if (projects.length == 1)
					message.append("Dissociated project ");
				else
					message.append("Dissociated projects: ");

                List associatedProjects = new ArrayList(projects.length);
                try
                {
    				for (int i = 0; i < projects.length; i++)
    				{
    					try
    					{
    						IProject project = projects[i];
                            associatedProjects.add(StateCacheFactory.getInstance().get(project));
    						RepositoryProvider.unmap(project);
                            StateCacheFactory.getInstance().remove(project);
    						if (i > 0)
    							message.append(", ");
    						message.append(project.getName());
    						monitor.worked(1);
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
    				message.append(" from clearcase");
                }
                finally
                {
                    for (Iterator changedStates = associatedProjects.iterator(); changedStates
                            .hasNext();)
                    {
                        StateCache state = (StateCache) changedStates.next();
                        StateCacheFactory.getInstance().fireStateChanged(state);
                    }
                    
                }
			}
		}, "Dissociating from clearcase", TeamAction.PROGRESS_DIALOG);

		MessageDialog.openInformation(
			shell,
			"Clearcase Plugin",
			message.toString());

		updateActionEnablement();
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
			if (provider == null)
				return false;
		}
		return true;
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.actions.TeamAction#getSelectedProjects()
     */
    protected IProject[] getSelectedProjects()
    {
        return super.getSelectedProjects();
    }

}
