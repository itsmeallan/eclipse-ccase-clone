/*******************************************************************************
 * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     mikael petterson - 
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.preferences;

import net.sourceforge.eclipseccase.ClearCasePreferences;
import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
import net.sourceforge.eclipseccase.ui.composite.DiffViewerFileAssociationsComposite;
import net.sourceforge.eclipseccase.ui.preferences.DiffViewerSettings.ResourceSpecificParameters;
import net.sourceforge.eclipseccase.ui.utility.FileHelper;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;

/**
 * @author mikael petterson
 * 
 */
public class ExtDiffViewerPreferencePage extends AbstractClearCasePreferencesPage {

	protected DiffViewerFileAssociationsComposite fileAssociationsComposite;

	protected DiffViewerSettings diffSettings;
	
	

	@Override
	public void init(IWorkbench workbench) {
		this.setDescription(PreferenceMessages.getString("ExtDiffViewerPreferencePage.Description")); //$NON-NLS-1$
	}

	
	protected Control createContentsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		composite.setLayout(layout);
		fileAssociationsComposite = new DiffViewerFileAssociationsComposite(composite,this);

		return composite;
	}

	@Override
	protected void initializeControls() {
		fileAssociationsComposite.initializeControls(this.diffSettings);
	}

	// Get hardcoded values.
	@Override
	protected void loadDefaultValues(IPreferenceStore store) {
		diffSettings = DiffViewerSettings.getDefaultDiffViewerSettings();
	}

	@Override
	protected void loadValues(IPreferenceStore store) {
		diffSettings = ExtDiffViewerPreferencePage.loadDiffViewerSettings(store);
	}

	@Override
	protected void saveValues(IPreferenceStore store) {
		ExtDiffViewerPreferencePage.saveDiffViewerSettings(this.diffSettings, store);
	}

	public static DiffViewerSettings loadDiffViewerSettings() {

		return ExtDiffViewerPreferencePage.loadDiffViewerSettings();
	}

	public static DiffViewerSettings loadDiffViewerSettings(IPreferenceStore store) {
		DiffViewerSettings diffSettings = new DiffViewerSettings();

		String encodedString = ClearCasePreferences.getDiffViewer();

		String[] stringArray = FileHelper.decodeStringToArray(encodedString);
		if (stringArray.length > 0 && stringArray.length % ResourceSpecificParameters.FIELDS_COUNT == 0) {
			int paramsCount = stringArray.length / ResourceSpecificParameters.FIELDS_COUNT;
			for (int i = 0; i < paramsCount; i++) {
				String[] strings = new String[ResourceSpecificParameters.FIELDS_COUNT];
				for (int j = 0; j < ResourceSpecificParameters.FIELDS_COUNT; j++) {
					strings[j] = stringArray[i * ResourceSpecificParameters.FIELDS_COUNT + j];
				}
				ResourceSpecificParameters param = ResourceSpecificParameters.createFromStrings(strings);
				if (param != null) {
					diffSettings.addResourceSpecificParameters(param);
				}
			}
		}

		return diffSettings;
	}

	public static void saveDiffViewerSettings(DiffViewerSettings diffSettings, IPreferenceStore store) {
		ExtDiffViewerPreferencePage.saveDiffViewerSettings(diffSettings, store, false);
	}

	public static void saveDiffViewerSettings(DiffViewerSettings diffSettings, IPreferenceStore store, boolean isDefault) {
		ResourceSpecificParameters[] resourceParams = diffSettings.getResourceSpecificParameters();
		if (resourceParams.length > 0) {
			String[] stringArray = new String[ResourceSpecificParameters.FIELDS_COUNT * resourceParams.length];
			for (int i = 0; i < resourceParams.length; i++) {
				ResourceSpecificParameters resourceParam = resourceParams[i];
				String[] strings = resourceParam.getAsStrings();
				System.arraycopy(strings, 0, stringArray, ResourceSpecificParameters.FIELDS_COUNT * i, strings.length);
			}
			String encodedString = FileHelper.encodeArrayToString(stringArray);
			ClearCasePreferences.setDiffViewer(IClearCasePreferenceConstants.DIFF_VIEWER_RESOURCES_SPECIFIC_PARAMETERS, encodedString, isDefault);
		} else {
			ClearCasePreferences.setDiffViewer(IClearCasePreferenceConstants.DIFF_VIEWER_RESOURCES_SPECIFIC_PARAMETERS, "", isDefault); //$NON-NLS-1$
		}
	}

//	@Override
//	protected Control createContents(Composite parent) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
