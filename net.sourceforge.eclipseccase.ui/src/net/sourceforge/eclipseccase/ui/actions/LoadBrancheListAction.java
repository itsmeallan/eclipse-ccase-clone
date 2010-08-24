package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.views.BranchSearchView;

import org.eclipse.core.resources.IProject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;


/** 
 * Class used to retrieve branches of a clearcase view. 
 */
public class LoadBrancheListAction  extends ClearCaseWorkspaceAction {
	private IProject project = null;

	private BranchSearchView view = null;

	
	public LoadBrancheListAction(BranchSearchView view, IProject project)
	{
		this.view = view;
		this.project = project;
	}
	
	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean isEnabled() {
		boolean bRes = true;
		return bRes;
	}

	@Override
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				try {
					if (project != null) {
						String []branches;
						
						File workingDir = new File(project.getLocation().toOSString());
						
						ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(project);
						if (p != null && (p.isClearCaseElement(project)))
						{
							branches = p.loadBrancheList(workingDir);
							view.setBranches(branches);
						}
					}
				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Load branch list");
	}
}
