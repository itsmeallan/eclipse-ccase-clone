/*
 * Copyright (c) 2004 Intershop (www.intershop.de)
 * Created on Apr 13, 2004
 */
package net.sourceforge.eclipseccase.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * TODO Provide description for ClearcaseViewLabelProvider.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class ClearcaseViewLabelProvider extends WorkbenchLabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.model.WorkbenchLabelProvider#decorateText(java.lang.String
	 * , java.lang.Object)
	 */
	@Override
	protected String decorateText(String input, Object element) {
		if (element instanceof IResource)
			return ((IResource) element).getFullPath().toString();

		return super.decorateText(input, element);
	}
}
