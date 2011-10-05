package net.sourceforge.eclipseccase.ucm;

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


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.sourceforge.eclipseccase.ClearCaseProvider;

/**
 * @author mikael petterson
 * 
 */
public class Activity {

	private static boolean test = false;
	private static ArrayList<Activity> activities = new ArrayList<Activity>();
	private String date;
	private String activitySelector;
	private String user;
	private String headline;
	private String stream;
	private boolean current;
	private Date myDate;

	public Activity(String date, String activitySelector, String user,
			String headline,String stream) {
		this.date = date;
		this.activitySelector = activitySelector;
		this.user = user;
		this.headline = headline;
		this.stream = stream;
		

	}
	
	public static Activity [] refreshActivities(String view, ClearCaseProvider provider){
		if (view == "" && provider == null) {
			Activity.setTest(true);
			activities.add(new Activity("06-Jun-00.17:16:12", "test", "mike", "test comment","mystream@/pvob1"));
			activities.add(new Activity("04-Jun-00.17:10:00", "test2", "mike", "another test comment","yourstream@/pvob1"));
			activities.add( new Activity("2011-06-14T16:16:04+03:00", "bmn011_quick_bug_fix", "bmn011", "bmn011_quick_bug_fix","teststream@/pvob1"));
			ArrayList<Activity> activityList = Activity.getActivities();
			return activityList.toArray(new Activity[activityList.size()]);

		}
		
		activities = provider.listActivities(view);
		//create new activities
		ArrayList<Activity> activityList = Activity.getActivities();
		return activityList.toArray(new Activity[activityList.size()]);
		
		
	}

	public Date getDate() {

		try {

			// ISO8601 time format.
			if (date.indexOf("T") != -1) {
				// ISO8601 time format like: 2011-05-22T16:02:37+03:00
				myDate = (Date) parseIso(date);

			} else {
				// Other format: 06-Jun-00.17:16:12
				DateFormat formatter = new SimpleDateFormat(
						"dd-MMM-yy HH:mm:ss");
				String myModDateString = date.replace('.', ' ');// replace dot
																// with
				myDate = (Date) formatter.parse(myModDateString);

			}
		} catch (ParseException pe) {
			System.out.println("Could not parse date: " + date + "got"
					+ pe.getMessage());
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

	public static void main(String[] args) {
		// 2011-06-14T16:16:04+03:00 bmn011_quick_bug_fix bmn011
		// "bmn011_quick_bug_fix"
		// ISO 8601 Format
		Activity a = new Activity("2011-06-14T16:16:04+03:00",
				"bmn011_quick_bug_fix", "bmn011", "bmn011_quick_bug_fix","bugfixstream@/pvob2");
		Date date = a.getDate();
		System.out.println("Date is " + date.getTime());

	}

	/**
	 * The formats are as follows. Exactly the components shown here must be
	 * present, with exactly this punctuation. Note that the "T" appears
	 * literally in the string, to indicate the beginning of the time element,
	 * as specified in ISO 8601.
	 * 
	 * Year: YYYY (eg 1997) Year and month: YYYY-MM (eg 1997-07) Complete date:
	 * YYYY-MM-DD (eg 1997-07-16) Complete date plus hours and minutes:
	 * YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00) Complete date plus hours,
	 * minutes and seconds: YYYY-MM-DDThh:mm:ssTZD (eg
	 * 1997-07-16T19:20:30+01:00) Complete date plus hours, minutes, seconds and
	 * a decimal fraction of a second YYYY-MM-DDThh:mm:ss.sTZD (eg
	 * 1997-07-16T19:20:30.45+01:00)
	 * 
	 * where:
	 * 
	 * YYYY = four-digit year MM = two-digit month (01=January, etc.) DD =
	 * two-digit day of month (01 through 31) hh = two digits of hour (00
	 * through 23) (am/pm NOT allowed) mm = two digits of minute (00 through 59)
	 * ss = two digits of second (00 through 59) s = one or more digits
	 * representing a decimal fraction of a second TZD = time zone designator (Z
	 * or +hh:mm or -hh:mm)
	 * 
	 */
	public static Date parseIso(String input) throws java.text.ParseException {

		// NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
		// things a bit. Before we go on we have to repair this.
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

		// this is zero time so we need to add that TZ indicator for
		if (input.endsWith("Z")) {
			input = input.substring(0, input.length() - 1) + "GMT-00:00";
		} else {
			int inset = 6;

			String s0 = input.substring(0, input.length() - inset);
			String s1 = input.substring(input.length() - inset, input.length());

			input = s0 + "GMT" + s1;
		}

		return df.parse(input);

	}

	public static ArrayList<Activity> getActivities() {
		return activities;
	}

	public static void setActivities(ArrayList<Activity> activities) {
		Activity.activities = activities;
	}

	public String getStream() {
		return stream;
	}

	public void setStream(String stream) {
		this.stream = stream;
	}

	public static void setTest(boolean value) {
		test = value;
	}

	public static boolean isTest() {
		return test;
	}

}

