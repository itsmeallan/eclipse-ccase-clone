package net.sourceforge.eclipseccase;

/**
 * This class connects the project with a certain view type, snapshot or
 * dynamic. We use this class to cache information so it will not be neccessary
 * to make a clearcase call.
 * 
 * @author mike
 * 
 */
public class View implements IView {
	private String viewType = null;
	private String projectName = null;

	public View(String projectName, String viewType) {
		this.projectName = projectName;
		this.viewType = viewType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.IView#getViewType()
	 */
	public String getViewType() {
		return viewType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.IView#getProjectName()
	 */
	public String getProjectName() {
		return projectName;
	}

}
