/*
 * Copyright (c) 2004 Intershop (www.intershop.de)
 * Created on Apr 13, 2004
 */
package net.sourceforge.eclipseccase.views;

import net.sourceforge.eclipseccase.StateCacheFactory;

import net.sourceforge.eclipseccase.StateCache;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * TODO Provide description for ClearCaseViewLabelProvider.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class ClearCaseViewLabelProvider extends WorkbenchLabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.model.WorkbenchLabelProvider#decorateText(java.lang.String
	 * , java.lang.Object)
	 */
	@Override
	protected String decorateText(String input, Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			StateCache cache = StateCacheFactory.getInstance().get(resource);
			if (cache.isCheckedOut()) {
				return resource.getFullPath().toString() + "      CHECKEDOUT";
			} else if (cache.isHijacked()) {
				return resource.getFullPath().toString() + "      HIJACKED";
			} else {
				return resource.getFullPath().toString() + "      (no CC element)";
			}
		}

		return super.decorateText(input, element);
	}
}
