package net.sourceforge.eclipseccase.views.HistoryViewer;


import net.sourceforge.clearcase.ElementHistory;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * 
 * @author Mattias Rundgren
 *
 */
public class HistoryViewerComparator extends ViewerComparator {
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public HistoryViewerComparator() {
		this.propertyIndex = 0;
		direction = DESCENDING;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object E1, Object E2) {
		ElementHistory e1 = (ElementHistory) E1;
		ElementHistory e2 = (ElementHistory) E2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = e1.getDate().compareTo(e2.getDate());
			break;
		case 1:
			rc = e1.getuser().compareTo(e2.getuser());
			break;
		case 2:
			rc = e1.getVersion().compareTo(e2.getVersion());
			String e1V = e1.getVersion();
			String e2V = e2.getVersion();
			int i1 = e1V.lastIndexOf("/");
			int i2 = e2V.lastIndexOf("/");
			rc = e1V.substring(0, i1).compareTo(e2V.substring(0, i2));
			if(rc==0)
			{
				rc = Integer.parseInt(e1V.substring(i1+1)) - Integer.parseInt(e2V.substring(i2+1));
			}
			break;
		case 3:
			rc = e1.getLabel().compareTo(e2.getLabel());
			break;
		case 4:
			rc = e1.getComment().compareTo(e2.getComment());
			break;
		default:
			rc = 0;
		}
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

}

