package net.sourceforge.eclipseccase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

	private static final File file = new File(
			"/home/mikael/workspace/test/src/cc_output.txt");

	public static void main(String[] args) {
		Parser.process();
		ArrayList<Activity> activities = Parser.listActivties();
		for (Activity activity : activities) {
			System.out.println("id " + activity.getActivitySelector() + "\n");
			System.out.println("header " + activity.getHeadline() + "\n");
			System.out.println("id " + activity.getStream() + "\n");

		}

	}

	public static void process() {
		Pattern pattern = Pattern.compile("activity\\s+\\\"(.*)\\\"");
		Matcher matcher = pattern.matcher("");

		LineNumberReader lineReader = null;
		try {
			lineReader = new LineNumberReader(new FileReader(file));
			String line = null;
			int ctr = 0;
			String activityId = null;
			String user = null;
			String date = null;
			String stream = null;
			String headline = null;
			while ((line = lineReader.readLine()) != null) {
				matcher.reset(line); // reset the input
				if (matcher.find()) {
					System.out.print("Match " + matcher.group(1));

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
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (lineReader != null)
					lineReader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static ArrayList<Activity> listActivties() {
		return activities;
	}

}
