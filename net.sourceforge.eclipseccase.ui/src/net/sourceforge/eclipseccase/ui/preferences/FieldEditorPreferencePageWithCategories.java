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
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.preferences;

import java.util.HashMap;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A preference page using field editors and tabs.
 */
public abstract class FieldEditorPreferencePageWithCategories extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IPropertyChangeListener {

	/**
	 * A special tab folder layout for borders around tab folders
	 */
	private final class TabFolderLayout extends Layout {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets
		 * .Composite, int, int, boolean)
		 */
		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);

			Control[] children = composite.getChildren();
			int count = children.length;
			int maxWidth = 0, maxHeight = 0;
			for (int i = 0; i < count; i++) {
				Control child = children[i];
				Point pt = child.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				maxWidth = Math.max(maxWidth, pt.x);
				maxHeight = Math.max(maxHeight, pt.y);
			}

			if (wHint != SWT.DEFAULT) {
				maxWidth = wHint;
			}
			if (hHint != SWT.DEFAULT) {
				maxHeight = hHint;
			}

			return new Point(maxWidth, maxHeight);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite
		 * , boolean)
		 */
		@Override
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle rect = composite.getClientArea();

			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				children[i].setBounds(rect);
			}
		}
	}

	/** map with field parents by category */
	private HashMap fieldParentsByCategory;

	/**
	 * Returns the field editor parent for the specified category.
	 * 
	 * @param category
	 * @return the field editor parent (maybe <code>null</code>)
	 */
	protected Composite getFieldEditorParent(String category) {
		return (Composite) fieldParentsByCategory.get(category);
	}

	/**
	 * Creates a new instance.
	 */
	public FieldEditorPreferencePageWithCategories() {
		super(GRID);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param title
	 */
	public FieldEditorPreferencePageWithCategories(String title) {
		super(title, GRID);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param title
	 * @param image
	 */
	public FieldEditorPreferencePageWithCategories(String title, ImageDescriptor image) {
		super(title, image, GRID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		// the main composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// create a tab folder for the page
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		GridData gd = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gd);

		// get tabs
		String[] categories = getCategories();
		fieldParentsByCategory = new HashMap(categories.length);

		// create tab item for every category
		for (int i = 0; i < categories.length; i++) {
			String category = categories[i];
			Composite categoryComposite = createCategoryComposite(category, tabFolder);
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(category);
			tabItem.setControl(categoryComposite);
		}

		// call super to create field editors and area for uncategorized field
		// editors
		Composite uncategorizedArea = new Composite(parent, SWT.NONE);
		uncategorizedArea.setLayout(new GridLayout(1, false));
		uncategorizedArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		super.createContents(uncategorizedArea);

		// create additional content on the uncategorized area
		createInformationAreaContent(uncategorizedArea);

		// adjust grid layout of categories
		for (int i = 0; i < categories.length; i++) {
			Composite cartegoryParent = getFieldEditorParent(categories[i]);
			((GridLayout) cartegoryParent.getLayout()).numColumns = ((GridLayout) getFieldEditorParent().getLayout()).numColumns;
			// update scroll area
			// ScrolledComposite scrolled =
			// (ScrolledComposite)cartegoryParent.getParent().getParent();
			// scrolled.layout();
			// scrolled.setMinWidth(cartegoryParent.getParent().getSize().x);
			// scrolled.setMinHeight(cartegoryParent.getParent().getSize().y);
		}

		return composite;
	}

	/**
	 * This method allows creation of an information area below the tab folder.
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 * 
	 * @param parent
	 */
	protected Control createInformationAreaContent(Composite parent) {
		// nothing
		return null;
	}

	/**
	 * Creates the composite for the specified category.
	 * 
	 * @param category
	 * @param parent
	 * @return the category composite
	 */
	private Composite createCategoryComposite(String category, Composite parent) {
		final ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);

		GridData newPageData = new GridData(GridData.FILL_BOTH);
		scrolled.setLayoutData(newPageData);

		final Composite categoryComposite = new Composite(scrolled, SWT.NULL);

		scrolled.setContent(categoryComposite);
		scrolled.setExpandVertical(true);
		scrolled.setExpandHorizontal(true);
		GridData controlData = new GridData(GridData.FILL_BOTH);
		categoryComposite.setLayoutData(controlData);
		categoryComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Point preferredSize = categoryComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				scrolled.setMinSize(preferredSize);
			}
		});

		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 1;
		categoryComposite.setLayout(layout);

		String description = getDescription(category);
		if (null != description) {
			Label label = new Label(categoryComposite, SWT.NONE);
			label.setText(description);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		Composite fieldParent = new Composite(categoryComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		fieldParent.setLayout(layout);
		fieldParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fieldParent.setFont(parent.getFont());
		fieldParentsByCategory.put(category, fieldParent);

		return scrolled;
	}

	/**
	 * Returns the description for the specified category.
	 * 
	 * @param category
	 * @return the description for the specified category
	 */
	protected abstract String getDescription(String category);

	/**
	 * Returns the categories.
	 * <p>
	 * There will be a tab for each category.
	 * </p>
	 * 
	 * @return the categories to create tabs for
	 */
	protected abstract String[] getCategories();
}