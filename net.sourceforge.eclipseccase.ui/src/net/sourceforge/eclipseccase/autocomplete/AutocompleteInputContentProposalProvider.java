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
package net.sourceforge.eclipseccase.autocomplete;

public class AutocompleteInputContentProposalProvider extends AutocompleteContentProposalProvider {

	/**
	 * Construct a ContentProposalProvider whose content proposals are
	 * the specified array of Objects.  This ContentProposalProvider will
	 * SUGGEST a completion for the input but will not force the input
	 * to be one of the proposals
	 * 
	 * @param proposals
	 *            the array of Strings to be returned whenever proposals are
	 *            requested.
	 */
	public AutocompleteInputContentProposalProvider(String[] proposals) {
		super(proposals);
	}

}
