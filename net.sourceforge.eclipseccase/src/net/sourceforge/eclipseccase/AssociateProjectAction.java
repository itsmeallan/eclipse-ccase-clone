package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class AssociateProjectAction implements IObjectActionDelegate
{

	private IWorkbenchPart part;
	private IStructuredSelection selection;

	public AssociateProjectAction()
	{
		super();
	}

	/** (non-Javadoc)
	 * Method declared on IDropActionDelegate
	 */
	public void run(IAction action)
	{

		try
		{
			if (RepositoryProvider.getProvider(getSelectedProject()) == null)
			{
				RepositoryProvider.addNatureToProject(
					getSelectedProject(),
					ClearcaseProvider.ID,
					null);
				//SimpleProvider p = (SimpleProvider) RepositoryProvider.getProvider(project);
				//p.configureProvider(config);
				MessageDialog.openInformation(
					part.getSite().getShell(),
					"Clearcase Plugin",
					"Associated project '" + getSelectedProject().getName() + "' with clearcase");
			}
		}
		catch (TeamException e)
		{
			ErrorDialog.openError(
				part.getSite().getShell(),
				"Clearcase Error",
				"Could not associate project '"
					+ getSelectedProject().getName()
					+ "' with clearcase",
				e.getStatus());
		}

	}

	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection instanceof IStructuredSelection)
		{
			this.selection = (IStructuredSelection) selection;
			//			if (action != null)
			//			{
			//				boolean enable = RepositoryProvider.getProvider(getSelectedProject()) == null;
			//				action.setEnabled(enable);
			//			}
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		this.part = targetPart;
	}

	protected IProject getSelectedProject()
	{
		return (IProject) selection.getFirstElement();
	}

}