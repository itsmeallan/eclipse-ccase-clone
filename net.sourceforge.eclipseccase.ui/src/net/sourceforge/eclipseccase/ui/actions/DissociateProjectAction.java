package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

public class DissociateProjectAction extends ClearcaseWorkspaceAction {

    /**
     * (non-Javadoc) Method declared on IDropActionDelegate
     */
    public void run(IAction action) {
        final StringBuffer message = new StringBuffer();

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    IProject[] projects = getSelectedProjects();
                    monitor.beginTask("Dissociating from ClearCase",
                            10 * projects.length);

                    if (projects.length == 1)
                        message.append("Dissociated project ");
                    else
                        message.append("Dissociated projects: ");

                    StateCacheFactory.getInstance().operationBegin();

                    for (int i = 0; i < projects.length; i++) {
                        IProject project = projects[i];
                        RepositoryProvider.unmap(project);
                        StateCacheFactory.getInstance().remove(project);
                        StateCacheFactory.getInstance().fireStateChanged(
                                project);
                        if (i > 0) message.append(", ");
                        message.append(project.getName());
                        monitor.worked(5);

                        // refresh the decorator
                        IDecoratorManager manager = PlatformUI.getWorkbench()
                                .getDecoratorManager();
                        if (manager.getEnabled(ClearcaseDecorator.ID)) {
                            ClearcaseDecorator activeDecorator = (ClearcaseDecorator) manager
                                    .getBaseLabelProvider(ClearcaseDecorator.ID);
                            if (activeDecorator != null) {
                                activeDecorator.refresh(project);
                            }
                        }
                        monitor.worked(5);
                    }
                    message.append(" from ClearCase");
                } finally {
                    StateCacheFactory.getInstance().operationEnd();
                    monitor.done();
                }
            }
        };

        executeInForeground(runnable, PROGRESS_DIALOG,
                "Dissociating from ClearCase");

        MessageDialog.openInformation(shell, "Clearcase Plugin", message
                .toString());
    }

    protected boolean isEnabled() throws TeamException {
        IProject[] projects = getSelectedProjects();
        if (projects.length == 0) return false;
        for (int i = 0; i < projects.length; i++) {
            IResource resource = projects[i];
            ClearcaseProvider provider = ClearcaseProvider
                    .getClearcaseProvider(resource);
            if (provider == null) return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.team.internal.ui.actions.TeamAction#getSelectedProjects()
     */
    protected IProject[] getSelectedProjects() {
        return super.getSelectedProjects();
    }

}