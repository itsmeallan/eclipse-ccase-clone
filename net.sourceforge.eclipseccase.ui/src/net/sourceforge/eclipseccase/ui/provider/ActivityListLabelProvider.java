/*******************************************************************************
 * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     mikael - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.provider;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * @author mikael petterson
 * 
 */
public class ActivityListLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		String activity = (String) element;
		return activity;
	}

}
