package net.sourceforge.eclipseccase.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

abstract public class ClearcaseAction extends TeamAction  implements IWorkbenchWindowActionDelegate
{
	private IWorkbenchWindow window;

	public ClearcaseAction()
	{
		super();
	}

	public void init(IWorkbenchWindow window)
	{
		this.window = window;
	}

	protected IResource[] getSelectedResources()
	{
		IResource[] result = null;
		IWorkbenchPart part = null;
		if (getWindow() != null)
			part = getWindow().getActivePage().getActivePart();
		if (part != null && part instanceof IEditorPart)
		{
			IEditorPart editor = (IEditorPart) part;
			IEditorInput input = editor.getEditorInput();
			IResource edited = (IFile) input.getAdapter(IFile.class);
			if (edited != null)
			{
				result = new IResource[] { edited };
			}
		}
		else
		{
			result = super.getSelectedResources();
		}

		if (result == null)
			result = new IResource[0];

		return result;
	}

	protected IWorkbenchWindow getWindow()
	{
		return window;
	}

}
