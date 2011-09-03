package net.sourceforge.eclipseccase.views;

import java.util.*;
import net.sourceforge.clearcase.ElementHistory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableItem;

public class HistoryColumnSorter implements SelectionListener {

	public static enum HistoryColumn {
		date, user, version, label, comment;
	}

	private static class HistoryColumnComparator implements Comparator<ElementHistory> {

		private final HistoryColumn sortColumn;

		public HistoryColumnComparator(final HistoryColumn _sortColumn) {
			sortColumn = _sortColumn;
		}

		private String getSortValue(ElementHistory el) {
			switch (sortColumn) {
			case comment:
				return el.getComment();
			case date:
				return el.getDate();
			case label:
				return el.getLabel();
			case user:
				return el.getuser();
			case version:
				return el.getVersion();
			default:
				throw new IllegalStateException();
			}
		}

		public int compare(ElementHistory o1, ElementHistory o2) {
			String sortVal1 = getSortValue(o1), sortVal2 = getSortValue(o2);
			if (sortVal1 != null && sortVal2 != null && sortVal1.compareTo(sortVal2) > 0) {
				return 1;
			}
			return -1;
		}
	};

	private final HistoryView historyView;

	private final Comparator<ElementHistory> userComparator;

	public HistoryColumnSorter(final HistoryView _historyView, final HistoryColumn _column) {
		historyView = _historyView;
		userComparator = new HistoryColumnComparator(_column);
	}

	public void widgetSelected(SelectionEvent e) {
		historyView.getHistoryTable().removeAll();
		java.util.List<ElementHistory> sorted = new ArrayList<ElementHistory>();
		sorted.addAll(historyView.getElemHistory());
		Collections.sort(sorted, userComparator);
		for (ElementHistory elem : sorted) {
			String[] row = new String[5];
			row[0] = elem.getDate();
			row[1] = elem.getuser();
			row[2] = elem.getVersion();
			row[3] = elem.getLabel();
			row[4] = elem.getComment();
			TableItem rowItem = new TableItem(historyView.getHistoryTable(), SWT.NONE);
			rowItem.setText(row);
		}
		historyView.getHistoryTable().update();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

}
