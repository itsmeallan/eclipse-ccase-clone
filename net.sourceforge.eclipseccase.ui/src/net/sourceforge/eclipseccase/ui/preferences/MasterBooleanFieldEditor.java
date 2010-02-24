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
import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.Composite;

/**
 * Upgrade of BooleanFieldEditor.
 * <p>
 * When this fieldeditor is unchecked/checked all slave Components get
 * disabled/enabled You just have to {@link #addSlave(FieldEditor) add}some
 * slaves during control setup and don't forget to activate the
 * {@link #listen() listener}when the component gets initialized
 * </p>
 */
public class MasterBooleanFieldEditor extends BooleanFieldEditor {

	/** the parent */
	final Composite parent;

	/** the slaves */
	ArrayList slaves = new ArrayList();

	/** the enabled state */
	boolean enabled = true;

	/**
	 * @param text_prefix_decoration
	 * @param string
	 * @param fieldEditorParent
	 */
	public MasterBooleanFieldEditor(String text_prefix_decoration, String string, Composite fieldEditorParent) {
		super(text_prefix_decoration, string, fieldEditorParent);
		parent = fieldEditorParent;
	}

	/**
	 * Adds a slave editor to control.
	 * 
	 * @param slave
	 */
	public void addSlave(FieldEditor slave) {
		slaves.add(slave);
		slave.setEnabled(getBooleanValue(), parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.BooleanFieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {

		// call super
		super.doLoad();

		updateSlaves();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.BooleanFieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {

		// call super
		super.doLoadDefault();

		updateSlaves();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditor#fireValueChanged(java.lang.String
	 * , java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void fireValueChanged(String property, Object oldValue, Object newValue) {
		if (VALUE.equals(property)) {
			updateSlaves();
		}
		super.fireValueChanged(property, oldValue, newValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.BooleanFieldEditor#setEnabled(boolean,
	 * org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent) {

		// call super
		super.setEnabled(enabled, parent);

		this.enabled = enabled;
		updateSlaves();
	}

	private void updateSlaves() {
		boolean enable = enabled && getBooleanValue();
		for (int i = 0; i < slaves.size(); i++) {

			Object e = slaves.get(i);
			FieldEditor fe = (FieldEditor) e;
			String name = fe.getPreferenceName();
			String imageccelelmentback = ClearCaseUIPreferences.IMAGE_CLEARCASE_ELEMENTS_BACKGROUND;
			if (name.equals(imageccelelmentback) && enable) {
				FileFieldEditor fed = ((FileFieldEditor) fe);
				fed.setEmptyStringAllowed(false);

			} else if (name.equals(imageccelelmentback) && !enable) {
				FileFieldEditor fed = ((FileFieldEditor) fe);
				fed.setEmptyStringAllowed(true);
			}
			fe.setEnabled(enable, parent);
		}
	}
}