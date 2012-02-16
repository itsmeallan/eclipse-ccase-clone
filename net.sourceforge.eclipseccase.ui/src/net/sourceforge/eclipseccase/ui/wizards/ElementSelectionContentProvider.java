package net.sourceforge.eclipseccase.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IProject;

import net.sourceforge.eclipseccase.ui.ResourceComparator;

import java.util.Arrays;

import org.eclipse.core.resources.IWorkspaceRoot;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;

import org.eclipse.core.resources.IResource;

import org.eclipse.ui.model.WorkbenchContentProvider;



public class ElementSelectionContentProvider extends WorkbenchContentProvider {
	
	
	
	private IResource[] resources;
	
	
	
	
	@Override
	public Object getParent(Object element) {
		return ((IResource) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContainer)
			return true;
		else
			return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IContainer){
			return getChildResources((IContainer) parentElement);
		}
		return new Object[0];
	}
	
	
	
	private IResource[] getChildResources(IContainer parent) {
		ArrayList<IResource> children = new ArrayList<IResource>();
		for (int i = 0; i < resources.length; i++) {
			if (!(resources[i] instanceof IContainer)) {
				IContainer parentFolder = resources[i].getParent();
				if (parentFolder != null && parentFolder.equals(parent) && !children.contains(parentFolder)) {
					children.add(resources[i]);
				}
			}
		}
		IResource[] childArray = new IResource[children.size()];
		children.toArray(childArray);
		return childArray;
	}
	
	
	
	

}
