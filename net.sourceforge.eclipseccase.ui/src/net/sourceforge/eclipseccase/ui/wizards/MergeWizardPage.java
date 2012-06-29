/*******************************************************************************
 * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.wizards;

import net.sourceforge.eclipseccase.ui.provider.MergeLabelProvider;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import java.util.HashSet;
import java.util.Set;
import net.sourceforge.eclipseccase.ClearCaseProvider;

import java.io.File;
import java.util.*;
import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.eclipseccase.*;
import net.sourceforge.eclipseccase.ui.ResourceComparator;
import net.sourceforge.eclipseccase.ui.compare.ClearCaseResourceNode;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.wizard.WizardPage;

/**
 * @author mikael petterson
 * 
 */
public class MergeWizardPage extends WizardPage {

	private static final ResourceComparator comparator = new ResourceComparator();

	private String[] branches;

	private IResource[] resources;

	private ArrayList<IResource> resourceList;

	private ListViewer listViewer;

	private ClearCaseProvider provider;

	private static final int WIDTH_HINT = 350;

	private static final int HEIGHT_HINT = 150;

	private Text commentText;

	private String[] comments = new String[0];

	private String comment = "";

	private Combo previousCommentsCombo;

	private Button recursiveButton;

	boolean recursive = false;

	boolean recursiveEnabled = true;

	@SuppressWarnings("unchecked")
	protected MergeWizardPage(String pageName, IResource[] resources, ClearCaseProvider provider) {
		super(pageName);
		setTitle("Merge");
		this.resources = resources;
		this.provider = provider;
		// sort and add to internal holder.
		// Arrays.sort(resources, comparator);
		// resourceList = new ArrayList<IResource>();
		// for (int i = 0; i < resources.length; i++) {
		// IResource resource = resources[i];
		// resourceList.add(resource);
		// }
		// // Load previous comments for combo.
		// if (null != ClearCasePlugin.getDefault()) {
		// comments = ClearCasePlugin.getDefault().getPreviousComments();
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createComboViewer(composite);
		setControl(composite);

	}

	protected void createComboViewer(Composite composite) {
		ComboViewer comboViewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboViewer.setLabelProvider(new MergeLabelProvider());
		comboViewer.setContentProvider(new ArrayContentProvider());
		loadBranches();
		comboViewer.setInput(branches);
	}

	private void loadBranches() {

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(resources[0].getProject().getName());

		if (project != null) {
			File workingDir = new File(project.getLocation().toOSString());

			if (provider != null && (provider.isClearCaseElement(project))) {
				branches = provider.loadBrancheList(workingDir);

			}
		}
	}

	

	protected void createTabItemForComments(TabFolder tabFolder) {

		TabItem tab1 = new TabItem(tabFolder, SWT.NONE);
		tab1.setText("Comments");
		Group group = new Group(tabFolder, SWT.BORDER);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		final Label commentLabel = new Label(group, SWT.NULL);
		commentLabel.setText("Edit the &comment:");
		commentText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		if (ClearCasePreferences.isCommentCheckin()) {
			commentText.setEnabled(true);
			commentText.setFocus();
		} else {
			commentText.setEnabled(false);
		}
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = WIDTH_HINT;
		data.heightHint = HEIGHT_HINT;
		commentText.setLayoutData(data);

		String extCoComment = getLastExtCoComment(resources);
		if (!extCoComment.equalsIgnoreCase("")) {
			this.setComment(extCoComment);

		}
		commentText.selectAll();
		// FIXME: Tabbing needed?
		commentText.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN && (e.stateMask & SWT.CTRL) != 0) {
					e.doit = false;
					// CommentDialogArea.this.signalCtrlEnter();
				}
			}
		});

		commentText.setText(comment);
		commentText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				comment = commentText.getText();
			}
		});

		// Combo for comments
		final Label prevCommentLabel = new Label(group, SWT.NULL);
		prevCommentLabel.setText("Choose a &previously entered comment:");

		previousCommentsCombo = new Combo(group, SWT.READ_ONLY);
		GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
		data2.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		previousCommentsCombo.setLayoutData(data2);
		initializeValues();
		previousCommentsCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = previousCommentsCombo.getSelectionIndex();
				if (index != -1) {
					commentText.setText(comments[index]);
				}
			}
		});
		// recursive checkbox.
		recursiveButton = new Button(group, SWT.CHECK);
		recursiveButton.setText("Recurse");
		recursiveButton.setEnabled(recursiveEnabled);
		recursiveButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				recursive = recursiveButton.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		tab1.setControl(group);
	}

	private void initializeValues() {
		// populate the previous comment list
		for (int i = 0; i < comments.length; i++) {
			previousCommentsCombo.add(flattenText(comments[i]));
		}
		// We don't want to have an initial selection
		// (see bug 32078: http://bugs.eclipse.org/bugs/show_bug.cgi?id=32078)
		previousCommentsCombo.setText(""); //$NON-NLS-1$
	}

	/**
	 * Flatten the text in the multiline comment
	 * 
	 * @param string
	 * @return String
	 */
	private String flattenText(String string) {
		StringBuffer buffer = new StringBuffer(string.length() + 20);
		boolean skipAdjacentLineSeparator = true;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == '\r' || c == '\n') {
				if (!skipAdjacentLineSeparator) {
					buffer.append("/");
				}
				skipAdjacentLineSeparator = true;
			} else {
				buffer.append(c);
				skipAdjacentLineSeparator = false;
			}
		}
		return buffer.toString();
	}

	/**
	 * Method retrieves the check-out comment for the last modified resource
	 * outside the eclipse workspace.
	 * 
	 * @param resources
	 * @return comment
	 */
	private String getLastExtCoComment(IResource[] resources) {
		long lastModificationTime = 0L;
		IResource lastModifiedResource = null;
		StringBuffer comment = new StringBuffer();
		String lastComment = "";
		for (IResource iResource : resources) {
			String path = iResource.getLocation().toOSString();
			File file = new File(path);
			long modificationTime = file.lastModified();
			if (modificationTime == 0L) {
				ClearCasePlugin.log(IStatus.WARNING, "Could not access resource," + iResource.getName(), null);
			}
			if (modificationTime > lastModificationTime) {
				lastModificationTime = modificationTime;
				lastModifiedResource = iResource;
			}

		}

		// get comment for last modified resource.
		ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(lastModifiedResource);
		if (provider != null) {
			String element = lastModifiedResource.getLocation().toOSString();
			String[] output = provider.describe(element, ClearCase.FORMAT, "%c");
			if (output.length > 0) {
				for (int i = 0; i < output.length; i++) {
					comment.append(output[i] + "\n");
				}

			}
			lastComment = comment.toString();
		}
		return lastComment;
	}

	// FIXME: Seems like we cannot use this to find elements.
	private class ResourceLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			// Image file =
			// PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			return null;
		}

		@Override
		public String getText(Object element) {
			IResource resource = (IResource) element;
			// return resource.getFullPath().toString();
			return resource.getLocation().toOSString();
		}
	}

	protected void createListViewContextMenu() {
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(listViewer.getControl());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				addToListViewContextMenu(menuMgr);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		listViewer.getList().setMenu(menu);
	}

	protected void addToListViewContextMenu(IMenuManager menuMgr) {

		if (!listViewer.getSelection().isEmpty()) {
			Action removeAction = new Action("remove") { //$NON-NLS-1$
				@Override
				public void run() {
					removeFromView();
				}
			};
			menuMgr.add(removeAction);

			Action compareAction = new Action("compare") {
				@Override
				public void run() {
					IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
					IResource resource = (IResource) selection.getFirstElement();
					compareWithPredecessor(resource);
				}
			};

			menuMgr.add(compareAction);
		}

	}

	public IResource[] getResourceList() {
		return resourceList.toArray(new IResource[resourceList.size()]);

	}

	/**
	 * Returns the recursiveEnabled.
	 * 
	 * @return returns the recursiveEnabled
	 */
	public boolean isRecursiveEnabled() {
		return recursiveEnabled;
	}

	/**
	 * Gets the recursive.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isRecursive() {
		return recursive;
	}

	private void removeFromView() {
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		Iterator<IResource> iterator = selection.iterator();
		while (iterator.hasNext()) {
			IResource resource = iterator.next();
			remove(resource);
		}

		resources = new IResource[resourceList.size()];
		resourceList.toArray(resources);
		refresh();
		if (resourceList.isEmpty()) {
			setPageComplete(false);
		}
	}

	private void remove(IResource resource) {
		ArrayList<IResource> removedResources = new ArrayList<IResource>();
		Iterator<IResource> iter = resourceList.iterator();
		while (iter.hasNext()) {
			IResource checkResource = iter.next();
			if (checkResource.getFullPath().toString().equals(resource.getFullPath().toString())) {
				removedResources.add(checkResource);
			}
		}
		iter = removedResources.iterator();
		while (iter.hasNext()) {
			resourceList.remove(iter.next());
		}
	}

	private void compareWithPredecessor(IResource resource) {
		CompareDialog cmpDialog = new CompareDialog(getShell(), null);
		String previousVersion = provider.getPredecessorVersion(resource);
		cmpDialog.compare(new DiffNode(null, Differencer.CHANGE, null, new ClearCaseResourceNode(resource, previousVersion, provider), new ResourceNode(resource)));
		cmpDialog.open();

	}

	private void refresh() {
		listViewer.refresh();

	}

	/**
	 * Returns the comment.
	 * 
	 * @return String
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Returns the comment.
	 * 
	 * @return String
	 */
	public String getComment() {
		if (comment != null && comment.length() > 0) {
			finished();
		}
		return comment;
	}

	private void finished() {
		// if there is a comment, remember it
		if (comment.length() > 0) {
			ClearCasePlugin.getDefault().addComment(comment);
		}
	}

}
