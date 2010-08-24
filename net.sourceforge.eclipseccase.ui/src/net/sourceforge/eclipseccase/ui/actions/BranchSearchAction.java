package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;

import java.io.File;
import net.sourceforge.eclipseccase.views.BranchSearchView;
import org.eclipse.core.resources.IProject;

import java.lang.reflect.InvocationTargetException;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.views.ConfigSpecView;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

public class BranchSearchAction extends ClearCaseWorkspaceAction {
	private IProject project = null;

	private BranchSearchView view = null;

	private String branchName = null;

	public BranchSearchAction(BranchSearchView view, IProject project, String branchName)
	{
		this.view = view;
		this.project = project;
		this.branchName = branchName;
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
						String []result;
						
						File workingDir = new File(project.getLocation().toOSString());
						
						ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(project);
						if (p != null && (p.isClearCaseElement(project)))
						{
							result = p.searchFilesInBranch(branchName, workingDir, new ConsoleOperationListener(monitor));

							view.setSearchResult(result);
						}
					}
				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Find files into " + branchName + " branch");
	}
}
