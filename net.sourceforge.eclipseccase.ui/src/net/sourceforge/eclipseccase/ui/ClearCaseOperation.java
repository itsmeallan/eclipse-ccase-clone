/*
 * Copyright (c) 2004 Intershop (www.intershop.de)
 * Created on Apr 8, 2004
 */
package net.sourceforge.eclipseccase.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.ui.TeamOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * TODO Provide description for ClearCaseOperation.
 *
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class ClearCaseOperation extends TeamOperation
{
    private IWorkspaceRunnable runnable;
    private ISchedulingRule rule;
    private boolean runAsJob;
    private String jobName;
    
    /**
     * Creates a new instance.
     * @param part
     */
    public ClearCaseOperation(IWorkbenchPart part, ISchedulingRule rule, IWorkspaceRunnable runnable, boolean runAsJob, String jobName)
    {
        super(part);
        this.rule = rule;
        this.runnable = runnable;
        this.runAsJob = runAsJob;
        this.jobName = jobName;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.TeamOperation#canRunAsJob()
     */
    protected boolean canRunAsJob()
    {
        return runAsJob;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.TeamOperation#getJobName()
     */
    protected String getJobName()
    {
        if(null == jobName)
            return super.getJobName();
        
        return jobName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
            ResourcesPlugin.getWorkspace().run(runnable, rule, 0, monitor);
        }
        catch (CoreException ex)
        {
            throw new InvocationTargetException(ex, jobName + ": " + ex.getLocalizedMessage());
        }
        catch (OperationCanceledException ex)
        {
            throw new InterruptedException();
        }
    }
}
