package net.sourceforge.eclipseccase.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.StateCache;
import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.StateChangeListener;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class CheckoutsView extends ViewPart implements StateChangeListener
{
	private TableViewer viewer;
	private Action refreshAction;
	private Collection checkouts =
		Collections.synchronizedSortedSet(new TreeSet(new Comparator()
	{
		public int compare(Object o1, Object o2)
		{
			return ((IResource) o1).getFullPath().toString().compareTo(
				((IResource) o2).getFullPath().toString());
		}
	}));

	private static final CoreException INTERRUPTED_EXCEPTION =
		new CoreException(new Status(IStatus.OK, "unknown", 1, "", null));

	private final class DoubleClickListener implements IDoubleClickListener
	{
		public void doubleClick(DoubleClickEvent event)
		{
			if (event.getSelection() instanceof IStructuredSelection)
			{
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				for (Iterator iter = selection.iterator(); iter.hasNext();)
				{
					IResource element = (IResource) iter.next();
					if (element.getType() == IResource.FILE)
					{
						try
						{
							PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.openEditor((IFile) element);
						}
						catch (PartInitException e)
						{
							ClearcasePlugin.log(
								IStatus.ERROR,
								"Could not create editor for " + element,
								e);
						}
					}
			
				}
			}
		}
	}

	private final class ViewContentProvider implements IStructuredContentProvider
	{
		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{
		}
		public void dispose()
		{
		}
		public Object[] getElements(Object parent)
		{
			return (IResource[]) checkouts.toArray(
				new IResource[checkouts.size()]);
		}
	}

	private final class ViewLabelProvider
		extends LabelProvider
		implements ITableLabelProvider
	{
		public String getColumnText(Object obj, int index)
		{
			return ((IResource) obj).getFullPath().toString();
		}
		public Image getColumnImage(Object obj, int index)
		{
			return getImage(obj);
		}
		public Image getImage(Object obj)
		{
			Image image = null;
			IResource resource = (IResource) obj;
			switch(resource.getType())
			{
				case IResource.FILE :
					image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
					break;
				case IResource.FOLDER :
					image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
					break;
				case IResource.PROJECT :
					image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_PROJECT);
					break;					
				default :
					image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
					break;					
			}
			return image;
		}
	}
	
	/**
	 * The constructor.
	 */
	public CheckoutsView()
	{
		StateCacheFactory.getInstance().addStateChangeListerer(this);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
		StateCacheFactory.getInstance().removeStateChangeListerer(this);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent)
	{
		viewer =
			new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(ResourcesPlugin.getWorkspace());
		viewer.addDoubleClickListener(new DoubleClickListener());
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		try
		{
			findCheckouts(new NullProgressMonitor(), false);
		}
		catch (InterruptedException e)
		{
		}
	}

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				// Other plug-ins can contribute there actions here
				manager.add(new Separator("Additions"));
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(refreshAction);
	}

	private void makeActions()
	{
		refreshAction = new Action()
		{
			public void run()
			{
				try
				{
					IRunnableWithProgress op = new IRunnableWithProgress()
					{
						public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException
						{
							monitor.beginTask("Finding checkouts", 10);
							IProgressMonitor subMonitor =
								new InfiniteSubProgressMonitor(monitor, 10);
							subMonitor.beginTask("", 5000);
							findCheckouts(subMonitor, true);
							subMonitor.done();
							monitor.done();
						}
					};
					
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(
						true,
						true,
						op);
				}
				catch (InvocationTargetException e)
				{
					showError(e.getTargetException().toString());
				}
				catch (InterruptedException e)
				{
					int i = 1;
				}
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refreshes the list of checked out files");
		refreshAction.setImageDescriptor(
			TeamImages.getImageDescriptor(UIConstants.IMG_REFRESH));
	}

	private void showMessage(String message)
	{
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Checkouts View",
			message);
	}

	private void showError(String message)
	{
		MessageDialog.openError(
			viewer.getControl().getShell(),
			"Checkouts View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	private void findCheckouts(final IProgressMonitor monitor, final boolean refreshState)
		throws InterruptedException
	{
		checkouts.clear();
		try
		{
			ClearcasePlugin
				.getWorkspace()
				.getRoot()
				.accept(new IResourceVisitor()
			{
				public boolean visit(IResource resource) throws CoreException
				{
					if (monitor.isCanceled())
						throw INTERRUPTED_EXCEPTION;
					monitor.worked(1);

					if (resource.getType() == IResource.ROOT)
						return true;

					if (StateCacheFactory
						.getInstance()
						.isUnitialized(resource))
						return false;

					StateCache cache =
						StateCacheFactory.getInstance().get(resource);
					if (refreshState)
						cache.update(true);
					if (cache.hasRemote())
					{
						updateCheckout(cache);
					}
					return true;
				}
			});
		}
		catch (CoreException e)
		{
			if (e == INTERRUPTED_EXCEPTION)
				throw new InterruptedException("Find checkouts cancelled");
			showError("Unable to find checkouts: " + e.toString());
		}
		finally
		{
			asyncRefresh();
		}
	}

	private void asyncRefresh()
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				viewer.refresh();
			}
		});
	}

	/**
	 * @see net.sourceforge.eclipseccase.StateChangeListener#stateChanged(net.sourceforge.eclipseccase.StateCache)
	 */
	public void stateChanged(StateCache stateCache)
	{
		if (updateCheckout(stateCache))
		{
			asyncRefresh();
		}
	}

	private boolean updateCheckout(StateCache stateCache)
	{
		boolean actionPerformed = false;
		IResource resource = stateCache.getResource();
		boolean contains = checkouts.contains(resource);
		if (stateCache.isCheckedOut())
		{
			if (!contains)
			{
				checkouts.add(resource);
				actionPerformed = true;
			}
		}
		else
		{
			if (contains)
			{
				checkouts.remove(resource);
				actionPerformed = true;
			}
		}
		return actionPerformed;
	}
}