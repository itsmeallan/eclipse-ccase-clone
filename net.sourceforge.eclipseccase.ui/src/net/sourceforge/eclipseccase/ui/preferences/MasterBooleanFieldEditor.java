/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Roel De Meester - extended the simple Editor 
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Upgrade of BooleanFieldEditor.
 * When this fieldeditor is unchecked/checked -> all slave Components get disabled/enabled
 * You just have to {@link #addSlave(FieldEditor) add} some slaves during control setup
 * and don't forget to activate the {@link #listen() listener} when the component gets initialized 
 */
public class MasterBooleanFieldEditor extends BooleanFieldEditor {
	Composite parent;
	ArrayList slaves = new ArrayList();

	/**
	 * @param text_prefix_decoration
	 * @param string
	 * @param fieldEditorParent
	 */
	public MasterBooleanFieldEditor(String text_prefix_decoration,
			String string, Composite fieldEditorParent) {
		super(text_prefix_decoration, string, fieldEditorParent);
		parent = fieldEditorParent;
	}

	private static void indent(Control control) {
		GridData gridData= new GridData();
		gridData.horizontalIndent= 30;
		control.setLayoutData(gridData);		
	}
	
	void addSlave(FieldEditor slave) {
		indent(slave.getLabelControl(parent));
		slaves.add(slave);
	}

	void listen() {
		for (int i = 0; i < slaves.size(); i++) {
			((FieldEditor) (slaves.get(i))).setEnabled(
					getBooleanValue(), parent);
		}

		setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				for (int i = 0; i < slaves.size(); i++) {
					((FieldEditor) (slaves.get(i))).setEnabled(
							getBooleanValue(), parent);

				}
			}
		});
	}

}