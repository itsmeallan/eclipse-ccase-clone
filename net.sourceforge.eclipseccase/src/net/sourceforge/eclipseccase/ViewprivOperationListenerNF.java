/**
 * 
 */
package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Analyse the output lines of a "cleartool ls -view_only" and
 * "cleartool lsprivate" to gather new view-private files
 */
public class ViewprivOperationListenerNF extends ViewprivOperationListener {

	/**
	 * @param prefix
	 * @param isGatheringCO
	 * @param monitor
	 */
	public ViewprivOperationListenerNF(String prefix, IProgressMonitor monitor) {
		super(prefix, monitor);
	}

	@Override
	protected void analyseLine(String filename) {
		if (filename.endsWith("Rule: CHECKEDOUT")) {
			// ignore checkedout stuff in a ls -view_only listing for snapshot
			// views, as we gather COs differently (with the lsco command, as
			// only that lists directories too)
			return;
		}

		// we have a valid name now
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
				trace("Found new ViewPriv " + resource.getLocation());
				cache.updateAsync(true);
			} else if (cache.isClearCaseElement()) {
				trace("Found ViewPriv, but cache wrong "
						+ resource.getLocation());
				cache.updateAsync(true);
			} else if (cache.isViewprivate()) {
				// validate that this is still a private element
				cache.setVpStateVerified();
			}
		}
	}

}
