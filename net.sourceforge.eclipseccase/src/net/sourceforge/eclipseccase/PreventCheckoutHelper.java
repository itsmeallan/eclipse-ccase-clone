/**
 * 
 */
package net.sourceforge.eclipseccase;



import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.PlatformUI;

/**
 * @author mikael petterson
 *
 */
public class PreventCheckoutHelper {
	
	public static boolean isPreventedFromCheckOut(ClearCaseProvider provider, IResource[] resources, boolean silent) {
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
	
	public static IResource[] isCheckedOut(ClearCaseProvider provider, IResource[] resources){
		ArrayList<IResource> toBeCheckedout = new ArrayList<IResource>(Arrays.asList(resources)); 
		for (final IResource resource : resources) {
			if(provider.isCheckedOut(resource)){
				toBeCheckedout.remove(resource);
			}
			
		}
		return (IResource[])toBeCheckedout.toArray( new IResource[toBeCheckedout.size() ]);
	}

}
