/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 8, 2004
 */

package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import net.sourceforge.eclipseccase.ui.ClearCaseOperation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * TODO Provide description for ClearcaseWorkspaceAction.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public abstract class ClearcaseWorkspaceAction extends ClearcaseAction
{
    /**
     * Executes the specified runnable in the background.
     * 
     * @param runnable
     * @param jobName
     * @param problemMessage
     */
    protected void executeInBackground(IWorkspaceRunnable runnable,
            String jobName)
    {
        IResource[] resources = getSelectedResources();
        ISchedulingRule rule = getSchedulingRule(resources);
        ClearCaseOperation operation = new ClearCaseOperation(getTargetPart(),
                rule, runnable, true, jobName);
        try
        {
            operation.run();
        }
        catch (InvocationTargetException ex)
        {
            handle(ex, jobName, jobName + ": " + ex.getLocalizedMessage());
        }
        catch (InterruptedException ex)
        {
            // canceled
        }
    }

    /**
     * Executes the specified runnable in the background.
     * 
     * @param runnable
     * @param jobName
     * @param problemMessage
     */
    protected void executeInForeground(final IWorkspaceRunnable runnable,
            int progressKind, String problemMessage)
    {
        IResource[] resources = getSelectedResources();
        ISchedulingRule rule = getSchedulingRule(resources);
        run(new WorkspaceModifyOperation(rule)
        {

            protected void execute(IProgressMonitor monitor)
                    throws CoreException, InvocationTargetException,
                    InterruptedException
            {
                runnable.run(monitor);
            }

        }, problemMessage, progressKind);
    }

    /**
     * Returns the scheduling rule.
     * 
     * @param resources
     * @return
     */
    protected ISchedulingRule getSchedulingRule(IResource[] resources)
    {
        if (null == resources || resources.length == 0) return null;
        // by default we run on the projects
        HashSet projects = new HashSet(resources.length);
        for (int i = 0; i < resources.length; i++)
        {
            projects.add(resources[i].getProject());
        }
        if (projects.size() == 1) return (IProject) projects.toArray()[0];

        return new MultiRule((IProject[]) projects
                .toArray(new IProject[projects.size()]));
    }

}