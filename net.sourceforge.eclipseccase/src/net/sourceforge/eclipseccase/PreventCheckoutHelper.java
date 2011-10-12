/**
 * 
 */
package net.sourceforge.eclipseccase;



import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author mikael petterson
 *
 */
public class PreventCheckoutHelper {
	
	public static boolean isPreventedFromCheckOut(Shell shell, ClearCaseProvider provider, IResource[] resources,boolean silent) {
		for (final IResource resource : resources) {

			if (provider.isPreventCheckout(resource) && !silent) {
				PreventCheckoutQuestion question = new PreventCheckoutQuestion(resource);
				PlatformUI.getWorkbench().getDisplay().syncExec(question);
				if (question.isRemember()) {
						ClearCasePreferences.setSilentPrevent();
				}
				return true;
			}else if(provider.isPreventCheckout(resource) && silent){
				//show no message.
				return true;
			}
		}
				
		return false;
	}
	
	

}
