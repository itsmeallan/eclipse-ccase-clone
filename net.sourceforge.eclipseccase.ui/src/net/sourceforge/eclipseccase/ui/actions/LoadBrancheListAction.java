package net.sourceforge.eclipseccase.ui.actions;

import java.util.ArrayList;
import java.util.List;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.resources.IResource;

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
					// TODO Add code below to loop over all linked folders (MR)
					if (project != null) {
						String []branches;
						ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(project);
						Set<File> folders = getProjectsAllFolders(p); // Where to look
						Set<String> branchesUnique = new HashSet<String>();
						for(File vob: folders)
						{
							branches = p.loadBrancheList(vob);
							for(String r : branches)
							{
								if(branchesUnique.add(r)) // Only adds if not added before
								{
//									System.out.format("Added branch %s \tfrom path %s\n", r, vob.getAbsolutePath());
								}
							}
						}						
//						File workingDir = new File(project.getLocation().toOSString());
						view.setBranches(branchesUnique.toArray(new String[0]));
						
//						if (p != null && (p.isClearCaseElement(project)))
//						{
//							branches = p.loadBrancheList(workingDir);
//							view.setBranches(branches);
//						}
					}
				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Load branch list");
	}

	private Set<File> getProjectsAllFolders(ClearCaseProvider p) throws CoreException {
		// TODO Return only one folder in each VOB.
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
}
