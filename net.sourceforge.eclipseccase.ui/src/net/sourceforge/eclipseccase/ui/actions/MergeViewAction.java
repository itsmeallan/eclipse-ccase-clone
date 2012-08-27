package net.sourceforge.eclipseccase.ui.actions;

import org.eclipse.core.resources.IProject;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import java.util.Vector;

import org.eclipse.jface.action.IAction;

import net.sourceforge.eclipseccase.views.MergeView;
import org.eclipse.ui.PlatformUI;

import net.sourceforge.clearcase.MergeData;

public class MergeViewAction extends ClearCaseWorkspaceAction{

	private Vector<MergeData> data;

	private MergeView view = null;
	
	private ClearCaseProvider provider ;
	
		
	private IProject project;

	public MergeViewAction(Vector<MergeData> data, ClearCaseProvider provider,IProject project) {
		this.data = data;
		this.provider = provider;
		this.project = project;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {

		try {
			view = (MergeView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("net.sourceforge.eclipseccase.views.MergeView");
			view.setData(data);
			view.setProvider(provider);
			view.setProject(project);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
