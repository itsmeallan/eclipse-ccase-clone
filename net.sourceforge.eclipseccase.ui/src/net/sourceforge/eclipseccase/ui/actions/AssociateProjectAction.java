package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class AssociateProjectAction extends ClearcaseAction
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
                    message.append("Associated project ");
                else
                    message.append("Associated projects: ");

                for (int i = 0; i < projects.length; i++)
                {
                    try
                    {
                        IProject project = projects[i];
                        RepositoryProvider.map(project, ClearcaseProvider.ID);
                        if (i > 0)
                            message.append(", ");
                        message.append(project.getName());
                        ClearcaseDecorator.refresh(project);
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
                message.append(" with clearcase");
            }
        }, "Associating with clearcase", TeamAction.PROGRESS_DIALOG);

        MessageDialog.openInformation(shell, "Clearcase Plugin", message.toString());

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
            ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
            if (provider != null)
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