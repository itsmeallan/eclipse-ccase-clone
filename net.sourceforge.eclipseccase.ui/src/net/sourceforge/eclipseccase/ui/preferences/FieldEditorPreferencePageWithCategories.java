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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A preference page using field editors and tabs.
 */
public abstract class FieldEditorPreferencePageWithCategories extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, IPropertyChangeListener {

    /**
     * A special tab folder layout for borders around tab folders
     */
    private final class TabFolderLayout extends Layout {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
         *      int, int, boolean)
         */
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
                    return new Point(wHint, hHint);

            Control[] children = composite.getChildren();
            int count = children.length;
            int maxWidth = 0, maxHeight = 0;
            for (int i = 0; i < count; i++) {
                Control child = children[i];
                Point pt = child.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                        flushCache);
                maxWidth = Math.max(maxWidth, pt.x);
                maxHeight = Math.max(maxHeight, pt.y);
            }

            if (wHint != SWT.DEFAULT) maxWidth = wHint;
            if (hHint != SWT.DEFAULT) maxHeight = hHint;

            return new Point(maxWidth, maxHeight);

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
         *      boolean)
         */
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
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        
        // the main composite
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // create a tab folder for the page
        TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
        tabFolder.setLayout(new TabFolderLayout());
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        // get tabs
        String[] categories = getCategories();
        fieldParentsByCategory = new HashMap(categories.length);

        // create tab item for every category
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            Composite categoryComposite = createCategoryComposite(category,
                    tabFolder);
            TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
            tabItem.setText(category);
            tabItem.setControl(categoryComposite);
        }
        
        // call super to create field editors and area for uncategorized field editors^
        Composite uncategorizedArea = new Composite(parent, SWT.NONE);
        uncategorizedArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        super.createContents(uncategorizedArea);

        return composite;
    }

    /**
     * Creates the composite for the specified category.
     * 
     * @param category
     * @param parent
     * @return the category composite
     */
    private Composite createCategoryComposite(String category, Composite parent) {
        Composite categoryComposite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        categoryComposite.setLayout(layout);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        categoryComposite.setLayoutData(data);

        String description = getDescription(category);
        if (null != description) {
            Label label = new Label(categoryComposite, SWT.NONE);
            label.setText(description);
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        Composite fieldParent = new Composite(categoryComposite, SWT.NONE);
        fieldParent.setLayoutData(new GridData(GridData.FILL_BOTH));
        fieldParentsByCategory.put(category, fieldParent);
        return categoryComposite;
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