/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Mikael Petterson - initial  implementation
 *******************************************************************************/
package net.sourceforge.eclipseccase;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * DialogMessageRunnable class was created so we could remove cluttered code 
 * in ClearCaseProvider and have a generic MessageDialog.
 * We also needed to get a value from the answer.
 * 
 * @author mike
 * 
 */
public class DialogMessageRunnable implements Runnable {

	private String operationType;
	private String msg;
	private int returnCode = 1;//Default no.

	public DialogMessageRunnable(String operationType, String msg) {
		this.operationType = operationType;
		this.msg = msg;
	}

	

	public void run() {
		Shell activeShell = PlatformUI.getWorkbench().getDisplay()
				.getActiveShell();
		MessageDialog checkoutQuestion = new MessageDialog(activeShell,
				operationType, null, msg, MessageDialog.QUESTION, new String[] {
						"Yes", "No", "Cancel" }, 0);
		returnCode = checkoutQuestion.open();

	}

	// A getter method to provide the result of the thread.
	public int getResult() {
		return returnCode;
	}

}
