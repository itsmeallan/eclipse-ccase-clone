/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *     Tobias Sodergren - added preferences for job priority
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.preferences;

import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The main preference page for the Eclipse ClearCase integration.
 */
public class ClearCasePreferencePage extends FieldEditorPreferencePageWithCategories implements IWorkbenchPreferencePage, IClearCasePreferenceConstants {

	private static final String GENERAL = PreferenceMessages.getString("Preferences.Category.General"); //$NON-NLS-1$

	private static final String SOURCE_MANAGEMENT = PreferenceMessages.getString("Preferences.Category.Source"); //$NON-NLS-1$

	private static final String COMMENTS = PreferenceMessages.getString("Preferences.Category.Comments"); //$NON-NLS-1$

	private static final String[] CATEGORIES = new String[] { GENERAL, SOURCE_MANAGEMENT, COMMENTS };

	static final String[][] ALWAYS_NEVER_PROMPT = new String[][] { { PreferenceMessages.getString("Always"), ALWAYS }, //$NON-NLS-1$
			{ PreferenceMessages.getString("Never"), NEVER }, //$NON-NLS-1$
			{ PreferenceMessages.getString("Prompt"), PROMPT } }; //$NON-NLS-1$

	static final String[][] ALWAYS_IF_POSSIBLE_NEVER = new String[][] { { PreferenceMessages.getString("Always"), ALWAYS }, //$NON-NLS-1$
			{ PreferenceMessages.getString("IfPossible"), IF_POSSIBLE }, //$NON-NLS-1$
			{ PreferenceMessages.getString("Never"), NEVER } }; //$NON-NLS-1$

	static final String[][] PRIORITIES = new String[][] { { PreferenceMessages.getString("HighPriority"), Integer.toString(Job.LONG) }, //$NON-NLS-1$ 
			{ PreferenceMessages.getString("DefaultPriority"), Integer.toString(Job.DECORATE) } }; //$NON-NLS-1$

	/**
	 * Creates a new instance.
	 */
	public ClearCasePreferencePage() {
		setDescription(PreferenceMessages.getString("Preferences.Description")); //$NON-NLS-1$

		// Set the preference store for the preference page.
		setPreferenceStore(new ClearCasePreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {

		// general settings
		addField(new StringFieldEditor(CLEARCASE_PRIMARY_GROUP, PreferenceMessages.getString("Preferences.General.ClearCasePrimaryGroup"), //$NON-NLS-1$
				getFieldEditorParent(GENERAL)));

		addField(new BooleanFieldEditor(WIP_REFRESH_CHILDREN_PREVENT, PreferenceMessages.getString("Preferences.General.RefreshChildren"), //$NON-NLS-1$
				getFieldEditorParent(GENERAL)));

		//		addField(new BooleanFieldEditor(IGNORE_NEW, PreferenceMessages.getString("Preferences.General.IgnoreNew"), //$NON-NLS-1$
		// getFieldEditorParent(GENERAL)));

		addField(new BooleanFieldEditor(HIDE_REFRESH_STATE_ACTIVITY, PreferenceMessages.getString("Preferences.General.HideRefreshStateActivity"), //$NON-NLS-1$
				getFieldEditorParent(GENERAL)));

		//		addField(new BooleanFieldEditor(RECURSIVE, PreferenceMessages.getString("Preferences.General.Recursive"), //$NON-NLS-1$
		// getFieldEditorParent(GENERAL)));

		//		addField(new BooleanFieldEditor(PRESERVE_TIMES, PreferenceMessages.getString("Preferences.General.PreserveTimes"), //$NON-NLS-1$
		// getFieldEditorParent(GENERAL)));

		//		addField(new BooleanFieldEditor(TEST_LINKED_PARENT_IN_CLEARCASE, PreferenceMessages.getString("Preferences.General.TestLinkedParentInClearCase"), //$NON-NLS-1$
		// getFieldEditorParent(GENERAL)));
		
		// TODO Achim: misused for testing
		// useCleartool.setEnabled(ClearCasePlugin.isWindows(),
		// getFieldEditorParent(GENERAL));
		// addField(useCleartool);

		//		addField(new RadioGroupFieldEditor(SAVE_DIRTY_EDITORS, PreferenceMessages.getString("Preferences.General.SaveDirtyEditors"), 1, //$NON-NLS-1$
		// ALWAYS_NEVER_PROMPT, getFieldEditorParent(GENERAL), true));

		addField(new BooleanFieldEditor(USE_CLEARDLG, PreferenceMessages.getString("Preferences.Source.ClearDlg"), //$NON-NLS-1$
				getFieldEditorParent(GENERAL)));

		addField(new BooleanFieldEditor(FULL_REFRESH, PreferenceMessages.getString("Preferences.Source.FullRefreshOnAssoc"), //$NON-NLS-1$
				getFieldEditorParent(GENERAL)));
		
		addField(new RadioGroupFieldEditor(JOB_QUEUE_PRIORITY, PreferenceMessages.getString("Preferences.General.JobQueuePriority"), 1, //$NON-NLS-1$
				PRIORITIES, getFieldEditorParent(GENERAL), true));

		// RadioGroupFieldEditor clearcaseLayer = new
		// RadioGroupFieldEditor(CLEARCASE_API,
		// "Interface for ClearCase operations",1,
		// new String[][]{
		// {"Native - CAL (ClearCase Automation Library)", CLEARCASE_NATIVE},
		// {"Native - cleartool executable", CLEARCASE_CLEARTOOL},
		// {"Compatible - ClearDlg executable", CLEARCASE_CLEARDLG}
		// }
		// ,getFieldEditorParent(GENERAL),true);
		// addField(clearcaseLayer);

		// source management

		// addField(new BooleanFieldEditor(CHECKOUT_AUTO, PreferenceMessages
		// .getString("Preferences.Source.CheckoutAuto"), //$NON-NLS-1$
		// getFieldEditorParent(SOURCE_MANAGEMENT)));

		addField(new RadioGroupFieldEditor(CHECKOUT_AUTO, PreferenceMessages.getString("Preferences.Source.CheckoutAuto"), //$NON-NLS-1$
				3, ALWAYS_NEVER_PROMPT, getFieldEditorParent(SOURCE_MANAGEMENT), true));

		addField(new BooleanFieldEditor(AUTO_PARENT_CHECKIN_AFTER_MOVE, PreferenceMessages.getString("Preferences.Source.AutoParentCheckinAfterMove"), getFieldEditorParent(SOURCE_MANAGEMENT)));

		addField(new BooleanFieldEditor(CHECKIN_IDENTICAL, PreferenceMessages.getString("Preferences.Source.CheckinIdentical"), //$NON-NLS-1$
				getFieldEditorParent(SOURCE_MANAGEMENT)));

		addField(new BooleanFieldEditor(KEEP_CHANGES_AFTER_UNCHECKOUT, PreferenceMessages.getString("Preferences.Source.KeepChangesAfterUncheckout"), //$NON-NLS-1$
				getFieldEditorParent(SOURCE_MANAGEMENT)));

		//		addField(new BooleanFieldEditor(ADD_WITH_CHECKIN, PreferenceMessages.getString("Preferences.Source.AddWithCheckin"), //$NON-NLS-1$
		// getFieldEditorParent(SOURCE_MANAGEMENT)));

		addField(new BooleanFieldEditor(CHECKOUT_LATEST, PreferenceMessages.getString("Preferences.Source.CheckoutLatest"), //$NON-NLS-1$
				getFieldEditorParent(SOURCE_MANAGEMENT)));

		addField(new RadioGroupFieldEditor(IClearCasePreferenceConstants.CHECKOUT_RESERVED, PreferenceMessages.getString("Preferences.Source.CheckoutReserved"), 3, //$NON-NLS-1$ 
				ALWAYS_IF_POSSIBLE_NEVER, getFieldEditorParent(SOURCE_MANAGEMENT), true));

		// comments

		addField(new BooleanFieldEditor(COMMENT_ADD, PreferenceMessages.getString("Preferences.Comments.CommentAdd"), //$NON-NLS-1$
				getFieldEditorParent(COMMENTS)));

		addField(new BooleanFieldEditor(COMMENT_CHECKIN, PreferenceMessages.getString("Preferences.Comments.CommentCheckin"), //$NON-NLS-1$
				getFieldEditorParent(COMMENTS)));

		addField(new BooleanFieldEditor(COMMENT_CHECKOUT, PreferenceMessages.getString("Preferences.Comments.CommentCheckout"), //$NON-NLS-1$
				getFieldEditorParent(COMMENTS)));

		addField(new BooleanFieldEditor(COMMENT_CHECKOUT_NEVER_ON_AUTO, PreferenceMessages.getString("Preferences.Comments.CommentCheckoutNeverOnAuto"), //$NON-NLS-1$ 
				getFieldEditorParent(COMMENTS)));

		//		addField(new BooleanFieldEditor(COMMENT_ADD_NEVER_ON_AUTO, PreferenceMessages.getString("Preferences.Comments.CommentAddNeverOnAuto"), //$NON-NLS-1$ 
		// getFieldEditorParent(COMMENTS)));

		//		addField(new BooleanFieldEditor(COMMENT_ESCAPE, PreferenceMessages.getString("Preferences.Comments.CommentEscapeXml"), //$NON-NLS-1$
		// getFieldEditorParent(COMMENTS)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (super.performOk()) {
			ClearCasePlugin.getInstance().resetClearCase();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.eclipseccase.ui.preferences.TabFieldEditorPreferencePage
	 * #getCategories()
	 */
	@Override
	protected String[] getCategories() {
		return CATEGORIES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sourceforge.eclipseccase.ui.preferences.
	 * FieldEditorPreferencePageWithCategories#getDescription(java.lang.String)
	 */
	@Override
	protected String getDescription(String category) {
//		if (GENERAL.equals(category))
//			return PreferenceMessages.getString("Preferences.Description.Category.General"); //$NON-NLS-1$
//		if (SOURCE_MANAGEMENT.equals(category))
//			return PreferenceMessages.getString("Preferences.Description.Category.Source"); //$NON-NLS-1$
//		if (COMMENTS.equals(category))
//			return PreferenceMessages.getString("Preferences.Description.Category.Comments"); //$NON-NLS-1$
		return null;
	}

}