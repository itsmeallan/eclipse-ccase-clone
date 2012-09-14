/*******************************************************************************
 * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     mikael petterson - modified to handle comboviewer.
 *     derekhunter4 - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.autocomplete.ccviewer;

import org.eclipse.jface.viewers.ComboViewer;

import net.sourceforge.eclipseccase.autocomplete.AutocompleteSelectorContentProposalProvider;

import net.sourceforge.eclipseccase.autocomplete.AutocompleteContentProposalProvider;

import java.util.Arrays;
import java.util.List;



import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Combo;

public class AutocompleteComboViewerSelector extends AutoCompleteComboViewer {
	
	private final class UpdateProposalListFocusListener implements FocusListener {
		public void focusGained(FocusEvent e) {
			// do nothing				
		}

		public void focusLost(FocusEvent e) {
			Combo theCombo = (Combo) e.getSource();
			List items = Arrays.asList(theCombo.getItems());
			if (! items.contains(theCombo.getText())) {
				theCombo.select(0);
			}
			
		}
	}

	public AutocompleteComboViewerSelector(ComboViewer aCombo) {
		super(aCombo);
		aCombo.getCombo().addFocusListener(new UpdateProposalListFocusListener());
	}
	
	protected AutocompleteContentProposalProvider getContentProposalProvider(String[] proposals) {
		return new AutocompleteSelectorContentProposalProvider(proposals, this.combo);
	}

}
