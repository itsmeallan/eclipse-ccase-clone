/**
 * 
 */
package net.sourceforge.eclipseccase;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

/**
 * @author mikael petterson
 * 
 */
public class PreventCheckoutHelper {

	private static int coUnresAnswer = 100;// Default value for

	// "No qestion for checkout has been asked".

	public static boolean isPreventedFromCheckOut(ClearCaseProvider provider,
			IResource[] resources, boolean silent) {
		for (final IResource resource : resources) {

			if (provider.isPreventCheckout(resource) && !silent) {
				PreventCheckoutQuestion question = new PreventCheckoutQuestion(
						resource);
				PlatformUI.getWorkbench().getDisplay().syncExec(question);
				if (question.isRemember()) {
					ClearCasePreferences.setSilentPrevent();
				}
				return true;
			} else if (provider.isPreventCheckout(resource) && silent) {
				// show no message.
				return true;
			}
		}

		return false;
	}

	public static IResource[] isCheckedOut(ClearCaseProvider provider,
			IResource[] resources) {
		ArrayList<IResource> toBeCheckedout = new ArrayList<IResource>(
				Arrays.asList(resources));
		for (final IResource resource : resources) {
			if (provider.isCheckedOut(resource)) {
				toBeCheckedout.remove(resource);
			}

		}
		return toBeCheckedout
				.toArray(new IResource[toBeCheckedout.size()]);
	}

	public static boolean isPromtedCoTypeOk() {
		UserDialog dialog = null;
		if (ClearCasePreferences.isAskCoType()) {
			dialog = new UserDialog(null, "Checkout Unreserved", null,
					"Checkout unreserved?", MessageDialog.QUESTION,
					new String[] { "Yes", "No" }, 0);
			PlatformUI.getWorkbench().getDisplay().syncExec(dialog);

			coUnresAnswer = dialog.getResult();
			if (ClearCasePreferences.isUseMasterForAdd() && coUnresAnswer == 1) {
				dialog = new UserDialog(
						null,
						"Checkout Warning",
						null,
						"Reserved checkout cannot be used with -nmaster option.\n Change in Preferences.",
						MessageDialog.WARNING, new String[] { "Ok" }, 0);
				PlatformUI.getWorkbench().getDisplay().syncExec(dialog);

				return false;
			}

			return true;
		}

		// since we don't have prompt option it is ok to co.
		return true;
	}

	public static int getcoUnresAnswer() {
		return coUnresAnswer;
	}

	public static void setcoUnresAnswer(int coUnresAnswer) {
		PreventCheckoutHelper.coUnresAnswer = coUnresAnswer;
	}

}
