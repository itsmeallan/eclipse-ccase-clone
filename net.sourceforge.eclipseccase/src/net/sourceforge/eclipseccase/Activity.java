/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *    mikael petterson - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *******************************************************************************/
package net.sourceforge.eclipseccase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mikael petterson
 *
 */
public class Activity {
	
	private String date;//cc date format is: 06-Jun-00.17:16:12
	private String activitySelector;
	private String user;
	private String headline;
	private boolean current;
	private Date myDate ;
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public Activity(String date,String activitySelector, String user, String headline){
		this.date = date;
		this.activitySelector = activitySelector;
		this.user = user;
		this.headline = headline;
			
	}
	

    public Date getDate(){
    	String myModDateString = date.replace('.', ' ');//replace dot with a space.
    
    	try {
    		myDate = (Date)formatter.parse(myModDateString);
		} catch (ParseException pe) {
			System.out.println("Could not parse date: "+date +"got"+pe.getMessage());
		}
    	return myDate;
    	
    }
    
	

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	
	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}


	public String getActivitySelector() {
		return activitySelector;
	}


	public void setActivitySelector(String activitySelector) {
		this.activitySelector = activitySelector;
	}
	
	
	

}
