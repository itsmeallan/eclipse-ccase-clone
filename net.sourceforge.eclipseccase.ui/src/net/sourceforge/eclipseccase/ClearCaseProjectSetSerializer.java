package net.sourceforge.eclipseccase;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * A project set serializer for clear case projects.
 * @see IProjectSetSerializer
 */
public class ClearCaseProjectSetSerializer implements IProjectSetSerializer
{
    private static final String VERSION = "1.0"; //$NON-NLS-1$

    /* (non-Javadoc)
     * @see org.eclipse.team.core.IProjectSetSerializer#asReference(org.eclipse.core.resources.IProject[], java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
     */
    public String[] asReference(
        IProject[] providerProjects,
        Object context,
        IProgressMonitor monitor)
        throws TeamException
    {
        ArrayList references = new ArrayList(providerProjects.length);
        for (int i = 0; i < providerProjects.length; i++)
        {
            IProject project = providerProjects[i];
            ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(project);
            if (provider != null)
            {
                StringBuffer reference = new StringBuffer();
                reference.append(VERSION);
                reference.append(':');
                reference.append(project.getName());
                reference.append(':');
                reference.append(provider.getVobName(project));
                reference.append(':');
                reference.append(provider.getVobRelativPath(project));
                references.add(reference.toString());
            }
        }

        return (String[]) references.toArray(new String[references.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.IProjectSetSerializer#addToWorkspace(java.lang.String[], java.lang.String, java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IProject[] addToWorkspace(
        String[] referenceStrings,
        String filename,
        Object context,
        IProgressMonitor monitor)
        throws TeamException
    {
        final int size = referenceStrings.length;
        final IProject[] projects = new IProject[size];
        final String[] vobs = new String[size];
        final String[] vobRelativePathes = new String[size];
        final Shell shell = context instanceof Shell ? (Shell) context : null;

        for (int i = 0; i < size; i++)
        {
            StringTokenizer tokenizer = new StringTokenizer(referenceStrings[i], ":"); //$NON-NLS-1$
            String version = tokenizer.nextToken();
            if (!VERSION.equals(version))
            {
                // Bail out, this is a newer version
                if (null != shell)
                {
                    shell.getDisplay().syncExec(new Runnable()
                    {
                        public void run()
                        {
                            MessageDialog.openError(
                                shell,
                                "Import Error",
                                "The project set was created with an older version of the Clear Case plugin and can not be used any more.");
                        }
                    });
                }
                return null;
            }
            projects[i] =
                ResourcesPlugin.getWorkspace().getRoot().getProject(tokenizer.nextToken());
            vobs[i] = tokenizer.nextToken();
            vobRelativePathes[i] = tokenizer.nextToken();
        }

        if (null != shell)
        {
            // Check if any projects will be overwritten, and warn the user.
            boolean yesToAll = false;
            int action;
            final int[] num = new int[] { size };
            for (int i = 0; i < size; i++)
            {
                IProject project = projects[i];
                if (project.exists())
                {
                    action = confirmOverwrite(project, yesToAll, shell);
                    yesToAll = action == 2;

                    // message dialog
                    switch (action)
                    {
                        // no
                        case 1 :
                            // Remove it from the set
                            vobs[i] = null;
                            vobRelativePathes[i] = null;
                            projects[i] = null;
                            num[0]--;
                            break;
                            // yes to all
                        case 2 :
                            // yes
                        case 0 :
                            break;
                            // cancel
                        case 3 :
                        default :
                            return null;
                    }
                }
            }

            // ask for the vob root
            final String vobRoot =
                openDirectoryDialog(
                    shell,
                    "ClearCase View Directory",
                    "Please select the folder containing the vobs with the projects to import (your View directory/drive on Windows and the 'vobs' directory on UNIX.");
            if (null != vobRoot)
            {
                WorkspaceModifyOperation op = new WorkspaceModifyOperation()
                {
                    public void execute(IProgressMonitor progressMonitor)
                        throws InterruptedException, InvocationTargetException
                    {
                        progressMonitor.beginTask("", 1000 * num[0]); //$NON-NLS-1$
                        try
                        {
                            for (int i = 0; i < size; i++)
                            {
                                if (null != projects[i]
                                    && null != vobs[i]
                                    && null != vobRelativePathes[i])
                                {
                                    createProject(
                                        projects[i],
                                        vobRoot,
                                        vobs[i],
                                        vobRelativePathes[i],
                                        new SubProgressMonitor(progressMonitor, 1000));
                                }
                            }
                        }
                        catch (TeamException e)
                        {
                            throw new InvocationTargetException(e);
                        }
                        finally
                        {
                            progressMonitor.done();
                        }
                    }
                };
                try
                {
                    op.run(monitor);
                }
                catch (InterruptedException e)
                {
                    // canceled
                }
                catch (InvocationTargetException e)
                {
                    Throwable t = e.getTargetException();
                    if (t instanceof TeamException)
                    {
                        throw (TeamException) t;
                    }
                }
                List result = new ArrayList();
                for (int i = 0; i < projects.length; i++)
                {
                    if (projects[i] != null)
                        result.add(projects[i]);
                }
                return (IProject[]) result.toArray(new IProject[result.size()]);
            }
        }
        return null;
    }

    /**
     * Creates a project in the workspace.
     * 
     * <p><b>Must be executed inside a workspace operation.</b></p>
     * 
     * @param project
     * @param vobRoot
     * @param vob
     * @param vobRelativePath
     * @param monitor
     * @throws TeamException
     */
    static void createProject(
        IProject project,
        String vobRoot,
        String vob,
        String vobRelativePath,
        IProgressMonitor monitor)
        throws TeamException, InterruptedException
    {
        try
        {
            monitor.beginTask(null, 1000);

            // determine the project path
            IPath newProjectLocation = new Path(vobRoot).append(vob).append(vobRelativePath);

            // Prepare the target projects to receive resources
            scrubProject(project, new SubProgressMonitor(monitor, 100));

            // create project description
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProjectDescription description = null;
            try
            {
                description =
                    workspace.loadProjectDescription(newProjectLocation.append(IProjectDescription.DESCRIPTION_FILE_NAME));
            }
            catch (CoreException exception)
            {
                description = workspace.newProjectDescription(project.getName());
            }

            //If it is under the root use the default location
            if (workspace.getRoot().getLocation().isPrefixOf(newProjectLocation))
                description.setLocation(null);
            else
                description.setLocation(newProjectLocation);

            // Bring the project into the workspace
            project.create(description, new SubProgressMonitor(monitor, 400));
            if (monitor.isCanceled())
                throw new InterruptedException();
            project.open(new SubProgressMonitor(monitor, 400));

            // bind to clear clase plugin
            RepositoryProvider.map(project, ClearcaseProvider.ID);
            StateCacheFactory.getInstance().remove(project);
            StateCacheFactory.getInstance().fireStateChanged(project);
            monitor.worked(100);
        }
        catch (CoreException e)
        {
            throw new TeamException(e.getStatus());
        }
        finally
        {
            monitor.done();
        }
    }

    /**
     * delete current existing projects but do not delete their content
     */
    private static void scrubProject(IProject project, IProgressMonitor monitor)
        throws TeamException
    {
        monitor.beginTask("Scrubbing project", 100);
        try
        {
            if (project != null && project.exists())
            {
                if (!project.isOpen())
                {
                    project.open(new SubProgressMonitor(monitor, 10));
                }

                monitor.subTask("Scrubbing local project");

                if (RepositoryProvider.getProvider(project) != null)
                    RepositoryProvider.unmap(project);

                project.delete(false, true, new SubProgressMonitor(monitor, 80));
            }
            else if (project != null)
            {
                // Make sure there is no directory in the local file system.
                File location =
                    new File(project.getParent().getLocation().toFile(), project.getName());
                if (location.exists())
                {
                    deepDelete(location);
                }
            }
        }
        catch (TeamException e)
        {
            throw e;
        }
        catch (CoreException e)
        {
            throw new TeamException(e.getStatus());
        }
        finally
        {
            monitor.done();
        }
    }

    private int confirmOverwrite(IProject project, boolean yesToAll, Shell shell)
    {
        if (yesToAll)
            return 2;
        if (!project.exists())
            return 0;
        final MessageDialog dialog =
            new MessageDialog(
                shell,
                "Confirm Overwrite",
                null,
                MessageFormat.format(
                    "The project {0} already exists. Do you wish to overwrite it?",
                    new String[] { project.getName()}),
                MessageDialog.QUESTION,
                new String[] {
                    IDialogConstants.YES_LABEL,
                    IDialogConstants.NO_LABEL,
                    IDialogConstants.YES_TO_ALL_LABEL,
                    IDialogConstants.CANCEL_LABEL },
                0);
        final int[] result = new int[1];
        shell.getDisplay().syncExec(new Runnable()
        {
            public void run()
            {
                result[0] = dialog.open();
            }
        });
        return result[0];
    }

    private String openDirectoryDialog(final Shell shell, final String title, final String message)
    {
        final String[] result = new String[1];
        shell.getDisplay().syncExec(new Runnable()
        {
            public void run()
            {
                DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.OPEN);
                directoryDialog.setText(title);
                directoryDialog.setMessage(message);
                result[0] = directoryDialog.open();
            }
        });
        return result[0];
    }

    private static void deepDelete(File resource)
    {
        if (resource.isDirectory())
        {
            File[] fileList = resource.listFiles();
            for (int i = 0; i < fileList.length; i++)
            {
                deepDelete(fileList[i]);
            }
        }
        resource.delete();
    }

}
