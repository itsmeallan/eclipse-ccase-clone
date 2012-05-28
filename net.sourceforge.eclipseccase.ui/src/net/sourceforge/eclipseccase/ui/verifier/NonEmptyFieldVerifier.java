/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package net.sourceforge.eclipseccase.ui.verifier;

import org.eclipse.swt.widgets.Control;


/**
 * Verifier for custom fields
 *
 * @author Sergiy Logvin
 */
public class NonEmptyFieldVerifier extends AbstractFormattedVerifier {
    protected static String ERROR_MESSAGE;
        
    public NonEmptyFieldVerifier(String fieldName) {
        super(fieldName);
        NonEmptyFieldVerifier.ERROR_MESSAGE = Messages.bind(Messages.getString("Verifier_NonEmpty"), new String[] {AbstractFormattedVerifier.FIELD_NAME});
    }
    
    protected String getErrorMessageImpl(Control input) {
        String text = this.getText(input);
        if (text.trim().length() == 0) {
            return NonEmptyFieldVerifier.ERROR_MESSAGE;
        }
        return null;
    }

    protected String getWarningMessageImpl(Control input) {    	
        return null;
    }

}
