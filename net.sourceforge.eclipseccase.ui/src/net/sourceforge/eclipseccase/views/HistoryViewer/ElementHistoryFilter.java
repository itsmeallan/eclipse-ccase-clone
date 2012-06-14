package net.sourceforge.eclipseccase.views.HistoryViewer;




import net.sourceforge.clearcase.ElementHistory;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * 
 * @author Mattias Rundgren
 *
 */
public class ElementHistoryFilter extends ViewerFilter {

	private String searchString;

	public void setSearchText(String s) {
		// Search for substring
		this.searchString = ".*" + s + ".*";
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		ElementHistory p = (ElementHistory) element;
		if (p.getVersion().matches(searchString)) {
			return true;
		}
		if (p.getComment().matches(searchString)) {
			return true;
		}
		if (p.getuser().matches(searchString)) {
			return true;
		}
		if (p.getLabel().matches(searchString)) {
			return true;
		}
		// No match
		return false;
	}
}
