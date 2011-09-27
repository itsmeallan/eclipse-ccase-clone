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

import net.sourceforge.eclipseccase.Activity;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.dialogs.ActivityDialog;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * This class sets/creates a ucm activity used for checkout or add operation.
 * 
 * @author eraonel
 * 
 */
public class UcmActivity {

	static class ActivityQuestion implements Runnable {
		private int returncode;

		public int getReturncode() {
			return returncode;
		}

		public void run() {
			Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
			MessageDialog activityQuestion = new MessageDialog(activeShell, "Select Activity", null, "Do you want to create a new or change activity?", MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
			returncode = activityQuestion.open();
		}
	}

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
			// check if current view has an activity associated.
			ActivityQuestion question = new ActivityQuestion();
			// Want to change//create activity?
			PlatformUI.getWorkbench().getDisplay().syncExec(question);

			/* Yes=0 No=1 Cancel=2 */
			if (!provider.activityAssociated(ClearCaseProvider.getViewName(resource)) || question.getReturncode() == 0) {
				ActivityDialog dlg = new ActivityDialog(shell, provider, resource);
				if (dlg.open() == Window.OK) {
					Activity activity = dlg.getSelectedActivity();
					if (activity != null) {
						String activitySelector = activity.getActivitySelector();
						MessageDialog.openInformation(shell, "Info", "activitySelector :"+activitySelector+" View :"+ClearCaseProvider.getViewName(resource));
						provider.setActivity(activitySelector, ClearCaseProvider.getViewName(resource));
						return true;
					}

				}
			}

		}
		// No checkout.
		return false;
	}

}
