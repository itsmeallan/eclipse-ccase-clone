package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.ISharedImages;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.resources.IContainer;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

public class ElementSelectionLabelProvider extends LabelProvider {

	// private WorkbenchLabelProvider wlp = new WorkbenchLabelProvider();
	private ArrayList<IResource> resourceList;

	@Override
	public Image getImage(Object element) {

		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;

		if (element instanceof IContainer)
			imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		// Image image = null;
		// if (resourceList.contains(element)) {
		// image = super.getImage(element);
		// }
		//
		// return image;

	}

	@Override
	public String getText(Object element) {
		String text = null;
		IResource resource = (IResource) element;
		
			
		return resource.getFullPath().makeRelative().toString();
		
	}

}
