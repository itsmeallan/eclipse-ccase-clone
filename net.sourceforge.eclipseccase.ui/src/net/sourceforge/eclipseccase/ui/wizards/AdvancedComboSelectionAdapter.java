/*******************************************************************************
 * Copyright (c) 2006 BestSolution Systemhaus GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of:
 * 1. The Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 2. LGPL v2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 3. MPL v1.1 which accompanies this distribution, and is available at
 * http://www.mozilla.org/MPL/MPL-1.1.html
 *
 * Contributors:
 *     Tom Schind <tom.schindl@bestsolution.at> - Initial API and implementation
 *******************************************************************************/

package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;



public class AdvancedComboSelectionAdapter implements KeyListener, FocusListener {
	private KeyInputFilter filter;
	private ComboViewer viewer;

	public AdvancedComboSelectionAdapter(ComboViewer viewer) {
		this.filter = new KeyInputFilter();
		this.viewer = viewer;
		this.viewer.addFilter(filter);
		this.viewer.getCombo().addKeyListener(this);
		this.viewer.getCombo().addFocusListener(this);
	}

	public void keyPressed(KeyEvent e) {
		e.doit = false;
		
		if (e.keyCode != SWT.ARROW_UP && e.keyCode != SWT.ARROW_DOWN && e.keyCode != SWT.ESC && e.keyCode != SWT.DEL && e.keyCode != SWT.CR
				&& e.keyCode != SWT.LF && e.keyCode != SWT.BS) {
			filter.actualString.append((char) e.keyCode);
		} else if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS ) {
			if (filter.actualString.length() > 0) {
				filter.actualString = new StringBuffer(filter.actualString.substring(0, filter.actualString.length() - 1));
			}
		} else if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
			e.doit = true;
		} else {
			filter.actualString = new StringBuffer();
		}

		if (!e.doit) {
			viewer.refresh();
			if (viewer.getSelection().isEmpty()) {
				Object value = viewer.getElementAt(0);
				if (value != null) {
					viewer.setSelection(new StructuredSelection(value));
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {

	}

	public void focusGained(FocusEvent e) {
		filter.actualString = new StringBuffer();
	}

	public void focusLost(FocusEvent e) {
		filter.actualString = new StringBuffer();
		viewer.refresh();
	}
	
	protected class KeyInputFilter extends ViewerFilter {
		protected StringBuffer actualString = new StringBuffer();
		
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (actualString.equals("")) {
				return true;
			} else {
				ILabelProvider provider = (ILabelProvider)((ComboViewer)viewer).getLabelProvider();
				String labelValue = provider.getText(element).toLowerCase();
				return labelValue.startsWith(actualString.toString().toLowerCase());
			}
		}
	}
}

