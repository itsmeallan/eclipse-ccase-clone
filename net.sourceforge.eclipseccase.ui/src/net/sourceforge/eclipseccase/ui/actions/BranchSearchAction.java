package net.sourceforge.eclipseccase.ui.actions;

import java.io.IOException;

import java.util.HashSet;

import java.util.Set;

import org.eclipse.core.resources.IFolder;

import java.util.ArrayList;

import java.util.List;

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
						
						ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(project);
						if (p != null)
						{
							Set<File> folders = getProjectsAllFolders(p);
							List<String> theResult = new ArrayList<String>();
							for(File vob: folders)
							{
								result = p.searchFilesInBranch(branchName, 
										vob, 
										new ConsoleOperationListener(monitor));
								for(String r : result)
								{
									theResult.add(r);
								}
							}

							view.setSearchResult(
									theResult.toArray(new String[0]));
						}
					}
				} finally {
					monitor.done();
				}
			}

			private Set<File> getProjectsAllFolders(ClearCaseProvider p) throws CoreException {
				// Find all folders
				Set<File> folders = new HashSet<File>();
				IResource [] projMembers = project.members();
				if (p.isClearCaseElement(project))
				{
					folders.add(project.getLocation().toFile());
				}
				for(IResource projMem : projMembers)
				{
					if((projMem.getType() == IResource.FOLDER) &&
							p.isClearCaseElement(projMem))
					{
						if(projMem.isLinked())
						{
							folders.add(projMem.getLocation().toFile());
						}
						else
						{
							String symbolicLink = p.getSymbolicLinkTarget(projMem);
							String location = projMem.getLocation().toOSString();
							if(symbolicLink.length() > 0)
							{
								location = projMem.getParent().getLocation().toOSString();
								File path = new File(location,symbolicLink);
								folders.add(path);
							}
						}
					}
				}
				return folders;
			}
		};

		executeInBackground(runnable, "Find files into " + branchName + " branch");
	}
}
