/*******************************************************************************
 * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     mikael petterson - made modification to handle selected proposal.
 *     derekhunter4 - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.autocomplete.ccviewer;

import net.sourceforge.eclipseccase.autocomplete.AutocompleteWidget;
import net.sourceforge.eclipseccase.ui.wizards.MergeWizardPage;
import org.eclipse.jface.fieldassist.*;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Combo;

public abstract class AutoCompleteComboViewer extends AutocompleteWidget {

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
		this.combo = aCombo.getCombo();

		if (combo != null) {
			this.combo.addFocusListener(new ProposalUpdateFocusListener());

			provider = getContentProposalProvider(combo.getItems());
			adapter = new ContentProposalAdapter(combo, new ComboContentAdapter(), provider, getActivationKeystroke(), getAutoactivationChars());
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			//This is called when using autocomplete.
			adapter.addContentProposalListener(new IContentProposalListener() {

				public void proposalAccepted(IContentProposal proposal) {
					MergeWizardPage.setSelectedBranch(proposal.getContent());

				}
			});

		}

	}

}
