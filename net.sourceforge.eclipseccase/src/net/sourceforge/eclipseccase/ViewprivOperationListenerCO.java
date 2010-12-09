/**
 * 
 */
package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author BA3759
 * 
 */
public class ViewprivOperationListenerCO extends ViewprivOperationListener {

	/**
	 * @param prefix
	 * @param isGatheringCO
	 * @param monitor
	 */
	public ViewprivOperationListenerCO(String prefix, IProgressMonitor monitor) {
		super(prefix, monitor);
	}

	@Override
	protected void analyseLine(String filename) {
		// System.out.println("+++ "+ filename);
		IResource[] resources = findResources(filename);

		// What about found resources that are not visible in
		// workspace yet? Two possibilities:
		// 1) If it would be visible after a manual refresh, we get a
		// valid IResource here, but resource.isAccessible() is false.
		// That is handled in cache.doUpdate() later
		// 2) If the workspace does not have a possible access path to the
		// resource, resources is empty, the for loop is not executed
		for (IResource resource : resources) {
			StateCache cache = StateCacheFactory.getInstance().getWithNoUpdate(
					resource);

			if (cache.isUninitialized()) {
				trace("Found new CO(1) " + resource.getLocation());
				cache.updateAsync(true);
			} else if (cache.isClearCaseElement()) {
				if (cache.isCheckedOut()) {
					// validate that this is still a checkedout element
					cache.setVpStateVerified();
				} else {
					trace("Found new CO(2) " + resource.getLocation());
					cache.updateAsync(true);
				}
			} else {
				// cached state is not (yet) a CC element
				trace("Found new CO(3) " + resource.getLocation());
				cache.updateAsync(true);
			}
		}
	}

}
