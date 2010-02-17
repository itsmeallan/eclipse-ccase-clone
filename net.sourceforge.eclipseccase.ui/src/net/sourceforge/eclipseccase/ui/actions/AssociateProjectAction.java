package net.sourceforge.eclipseccase.ui.actions;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipseccase.StateCache;

import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import org.eclipse.core.runtime.SubProgressMonitor;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

public class AssociateProjectAction extends ClearcaseWorkspaceAction {

	/**
	 * (non-Javadoc) Method declared on IDropActionDelegate
	 */
	public void execute(IAction action) {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IProject[] projects = getSelectedProjects();
					// each project gets 200 ticks
					monitor.setTaskName("Associating projects with ClearCase");
					monitor.beginTask("", 200 * projects.length);

					StateCacheFactory.getInstance().operationBegin();

					for (int i = 0; i < projects.length; i++) {
						IProject project = projects[i];
						RepositoryProvider.map(project, ClearcaseProvider.ID);
						StateCacheFactory.getInstance().remove(project);
						StateCacheFactory.getInstance().fireStateChanged(project);

						// first, get list of resources
						final List<IResource> resources = new ArrayList<IResource>();
						try {
							project.accept(new IResourceVisitor() {
								public boolean visit(IResource resource) {
									resources.add(resource);
									return true;
								}
							});
						} catch (CoreException e) {
						}
						// now we know how much to do, create a
						// SubProgressMonitor
						SubProgressMonitor submonitor = new SubProgressMonitor(monitor, 200);
						monitor.subTask("Scanning project " + project.getName());
						// 10 for activeDecorator.refresh()
						submonitor.beginTask(project.getName(), resources.size() + 10);
						for (IResource res : resources) {
							ClearcaseProvider p = ClearcaseProvider.getClearcaseProvider(res);
							if (p != null) {
								p.ensureInitialized(res);
							}
							submonitor.worked(1);
						}

						// To get correct state for project.
						// refresh the decorator
						IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
						if (manager.getEnabled(ClearcaseDecorator.ID)) {
							ClearcaseDecorator activeDecorator = (ClearcaseDecorator) manager.getBaseLabelProvider(ClearcaseDecorator.ID);
							if (activeDecorator != null) {
								activeDecorator.refresh(project);
							}
						}
						submonitor.done();
					}
				} finally {
					StateCacheFactory.getInstance().operationEnd();
					monitor.done();
				}
			}
		};

		executeInForeground(runnable, PROGRESS_DIALOG, "Associating with ClearCase");
	}

	public boolean isEnabled() {
		IProject[] projects = getSelectedProjects();
		if (projects.length == 0)
			return false;
		for (int i = 0; i < projects.length; i++) {
			IResource resource = projects[i];
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
			if (provider != null)
				return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.internal.ui.actions.TeamAction#getSelectedProjects()
	 */
	protected IProject[] getSelectedProjects() {
		return super.getSelectedProjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sourceforge.eclipseccase.ui.actions.ClearcaseWorkspaceAction#
	 * getSchedulingRule()
	 */
	protected ISchedulingRule getSchedulingRule() {
		// we run on the workspace root
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}