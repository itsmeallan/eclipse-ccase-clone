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
package net.sourceforge.eclipseccase.ui.dialogs;

import net.sourceforge.eclipseccase.Activity;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * @author mikael petterson
 *
 */
public class ActivityListLabelProvider extends LabelProvider {

	public String getText(Object element){
		Activity activity = (Activity)element;
		return activity.getHeadline();
	}

}
