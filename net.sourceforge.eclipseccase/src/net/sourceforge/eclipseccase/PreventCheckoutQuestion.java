/**
 * 
 */
package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.PlatformUI;

/**
 * @author mikael petterson
 *
 */
public class PreventCheckoutQuestion implements Runnable {

	private IResource resource;

	private int result;

	private boolean remember;

	public PreventCheckoutQuestion(IResource resource) {
		this.resource = resource;
	}

	public void run() {
		MessageDialogWithToggle checkoutQuestion = new MessageDialogWithToggle(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("ClearCaseModificationHandler.infoDialog.title"), null, Messages.getString("ClearCaseModificationHandler.infoDialog.message.part1")+resource.getName()+" "+Messages.getString("ClearCaseModificationHandler.infoDialog.message.part2"), MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0, "Skip this dialog in the future!", false);
		checkoutQuestion.open();
		result = checkoutQuestion.getReturnCode();
		remember = checkoutQuestion.getToggleState();
	}

	public int getResult() {
		return result;
	}

	public boolean isRemember() {
		return remember;
	}

}
