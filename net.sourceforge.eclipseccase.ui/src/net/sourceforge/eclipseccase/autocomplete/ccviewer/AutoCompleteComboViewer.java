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

import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.swt.events.SelectionAdapter;

import org.eclipse.swt.events.SelectionAdapter;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.jface.viewers.*;

import net.sourceforge.eclipseccase.ui.wizards.MergeWizardPage;

import org.eclipse.jface.viewers.*;

import org.eclipse.jface.viewers.ComboViewer;

import net.sourceforge.eclipseccase.autocomplete.AutocompleteWidget;

import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Combo;

public abstract class AutoCompleteComboViewer extends AutocompleteWidget {

	private ComboViewer comboViewer;

	private final class ProposalUpdateFocusListener implements FocusListener {
		public void focusGained(FocusEvent e) {
			provider.setProposals(combo.getItems());
		}

		public void focusLost(FocusEvent e) {
			// do nothing
		}
	}

	protected Combo combo = null;

	public AutoCompleteComboViewer(ComboViewer aCombo) {
		this.comboViewer = aCombo;
		this.combo = aCombo.getCombo();

		if (combo != null) {
			this.combo.addFocusListener(new ProposalUpdateFocusListener());

			provider = getContentProposalProvider(combo.getItems());
			adapter = new ContentProposalAdapter(combo, new ComboContentAdapter(), provider, getActivationKeystroke(), getAutoactivationChars());
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			combo.addSelectionListener(new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent e) {
					System.out.println("widgetSelected!");
				}

				/**
				 * Sent when default selection occurs in the control.
				 * The default behavior is to do nothing.
				 *
				 * @param e an event containing information about the default selection
				 */
				public void widgetDefaultSelected(SelectionEvent e) {
					System.out.println("widgetDefaultSelected!");
				}
				
				
			});
		}

	}
	
	

}
