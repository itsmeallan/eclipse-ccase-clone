package net.sourceforge.eclipseccase.ucm;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */

/**
 * @author mikael
 * 
 */
public class Parser {

	private static final ArrayList<Activity> activities = new ArrayList<Activity>();

	public static void main(String[] args) {
		final ArrayList<String> myLines = new ArrayList<String>();
		final File file = new File("/home/eraonel/opensource_projects/net.sourceforge.eclipseccase/src/net/sourceforge/eclipseccase/ucm/cc_output.txt");
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(file);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				System.out.println(strLine);
				myLines.add(strLine);
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		String [] input = (String[]) myLines.toArray(new String[myLines.size()]);
		
		Parser.process(input);
		ArrayList<Activity> activities = Parser.listActivties();
		for (Activity activity : activities) {
			System.out.println("id " + activity.getActivitySelector() + "\n");
			System.out.println("header " + activity.getHeadline() + "\n");
			System.out.println("id " + activity.getStream() + "\n");

		}
	}

	public static void process(String[] lines) {
		Pattern pattern = Pattern.compile("activity\\s+\\\"(.*)\\\"");
		Matcher matcher = pattern.matcher("");

		int ctr = 0;
		String activityId = null;
		String user = null;
		String date = null;
		String stream = null;
		String headline = null;
		for (String line : lines) {

			matcher.reset(line); // reset the input
			if (matcher.find()) {
				System.out.print("Match " + matcher.group(1)+"\n");

				ctr++;
				if (ctr == 1) {
					activityId = matcher.group(1);

				}
				if (ctr > 1) {
					activities.add(new Activity(date, activityId, user,
							headline, stream));
					activityId = matcher.group(1);
				}
			} else {
				String[] data = DataParser.processLine(line);

				if (data[0].equalsIgnoreCase(DataParser.NO_DATA)) {
					continue;
				} else if (data[0].equalsIgnoreCase(DataParser.USER)) {
					user = data[1];
				} else if (data[0].equalsIgnoreCase(DataParser.TIME)) {
					date = data[1];
				} else if (data[0].equalsIgnoreCase(DataParser.STREAM)) {
					stream = data[1];
				} else if (data[0].equalsIgnoreCase(DataParser.HEADLINE)) {
					headline = data[1];
				}

			}
		}// end foreach
	}

	public static ArrayList<Activity> listActivties() {
		return activities;
	}

}
