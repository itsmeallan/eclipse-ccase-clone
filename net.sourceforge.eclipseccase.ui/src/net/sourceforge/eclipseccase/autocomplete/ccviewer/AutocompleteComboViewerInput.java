/*******************************************************************************
 * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     derekhunter4 - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.autocomplete.ccviewer;

import org.eclipse.jface.viewers.ComboViewer;

import net.sourceforge.eclipseccase.autocomplete.AutocompleteInputContentProposalProvider;

import net.sourceforge.eclipseccase.autocomplete.AutocompleteContentProposalProvider;

import org.eclipse.swt.widgets.Combo;

public class AutocompleteComboViewerInput extends AutoCompleteComboViewer {
	
	
	public AutocompleteComboViewerInput(ComboViewer combo) {
		super(combo);
	}

	protected AutocompleteContentProposalProvider getContentProposalProvider(String[] proposals) {
		return new AutocompleteInputContentProposalProvider(proposals);
	}
	
	
	
	

}
