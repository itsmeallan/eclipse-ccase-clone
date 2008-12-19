/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 8, 2004
 */

package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;
import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.ui.ClearCaseOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Base class for ClearCase actions that require some workspace locking.
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
			String jobName) {
		ClearCaseOperation operation = new ClearCaseOperation(getTargetPart(),
				getSchedulingRule(), runnable, true, jobName);
		try {
			operation.run();
		} catch (InvocationTargetException ex) {
			handle(ex, jobName, jobName + ": " + ex.getMessage());
		} catch (InterruptedException ex) {
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
			int progressKind, String problemMessage) {
		StateCacheFactory.getInstance().interruptPendingRefreshes();
		run(new WorkspaceModifyOperation(getSchedulingRule()) {

			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {
				runnable.run(monitor);
			}

		}, problemMessage, progressKind);
		StateCacheFactory.getInstance().resumePendingRefreshes();
	}

    /**
	 * Returns the scheduling rule.
	 * 
	 * 
	 * @return
	 */
	protected ISchedulingRule getSchedulingRule() {
		// by default we run on the projects
		IResource[] projects = getSelectedProjects();
		if (null == projects || projects.length == 0)
			return null;
		if (projects.length == 1)
			return projects[0];
		ISchedulingRule rule = null;
		for (int i = 0; i < projects.length; i++) {
			rule = MultiRule.combine(rule, projects[i]);
		}
		return rule;
	}

}