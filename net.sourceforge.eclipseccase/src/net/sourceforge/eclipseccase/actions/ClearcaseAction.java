package net.sourceforge.eclipseccase.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.eclipseccase.ClearcasePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

abstract public class ClearcaseAction extends TeamAction  implements IWorkbenchWindowActionDelegate
{
	private IWorkbenchWindow window;
	private static Map actions = new HashMap();
	
	public ClearcaseAction()
	{
		super();
	}

	public void init(IWorkbenchWindow window)
	{
		this.window = window;
	}

	public void dispose()
	{
		deregisterAction();
		super.dispose();
	}

	// Lets us use the same actions for context menus/actionSet+keybindings - i.e. if we
	// are in an active editor, that is the active selection instead of whats selected in
	// the tree view
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
	
	private void registerAction(IAction action)
	{
		if (action!= null)
		{
			synchronized(actions)
			{
				IAction old = (IAction) actions.get(this);
				if (old != null && old != action)
				{
					ClearcasePlugin.log(IStatus.WARNING, "Mismatched actions in ClearcaseAction", null);
				}
				actions.put(this, action);
			}
		}
	}
	
	private void deregisterAction()
	{
		synchronized(actions)
		{
			actions.remove(this);
		}		
	}
	
	// We may want to register a cache StateChangeListener so that if the state changes,
	// and we have not changed our selection, the action enablement gets updated to reflect
	// the new state.  May be too much overhead for too little benefit
	protected void updateActionEnablement()
	{
		synchronized(actions)
		{
			for (Iterator iter = actions.entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry element = (Map.Entry) iter.next();
				ClearcaseAction ccAction = (ClearcaseAction) element.getKey();
				IAction action = (IAction) element.getValue();
				ccAction.setActionEnablement(action);
			}
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection)
	{
		super.selectionChanged(action, selection);
		registerAction(action);
	}

}
