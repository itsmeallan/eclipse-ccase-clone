/*******************************************************************************
 * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.dialogs.ActivityDialog;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * This class sets/creates a ucm activity used for checkout or add operation.
 * 
 * @author eraonel
 * 
 */
public class UcmActivity {

	/**
	 * 
	 * @param provider
	 * @param resources
	 * @param shell
	 * @return
	 */
	public static boolean checkoutWithActivity(ClearCaseProvider provider, IResource[] resources, Shell shell) {

		IResource resource = resources[0];
		if (resource != null) {
			String view = ClearCaseProvider.getViewName(resource);
			ActivityDialog dlg = new ActivityDialog(shell, provider, resource);
			if (dlg.open() == Window.OK) {
				String activity = dlg.getSelectedActivity();
				if (activity != null) {
					System.out.println("Selected activity is :" + activity);
					provider.setActivity(activity, view);
					return true;
				}

			} else
				// Answer was N or Cancel.
				return false;

			return true;
		}
		// resource null don't check-out.
		return false;
	}

}
