package net.sourceforge.eclipseccase.ucm;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class is used to parse extensive output from 'cleartool lsactivity -l
 * <viewname>'. It separates each activity and sends the data following it to
 * the ActivityDataParser.
 * 
 * @author mikael petterson
 * 
 */
public class ActivityParser {

	private static final ArrayList<Activity> activities = new ArrayList<Activity>();

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
				ctr++;
				if (ctr == 1) {
					activityId = matcher.group(1);

				}
				if (ctr > 1) {
					StringBuffer sb = new StringBuffer();
					sb.append("Date "+date).append("actvityId "+activityId).append("user "+user).append("headline "+headline).append("stream "+stream);
					System.out.println("Creating activity "+sb.toString());
					activities.add(new Activity(date, activityId, user,
							headline, stream));
					activityId = matcher.group(1);
				}
			} else {
				String[] data = ActivityDataParser.processLine(line);

				if (data[0].equalsIgnoreCase(ActivityDataParser.NO_DATA)) {
					continue;
				} else if (data[0].equalsIgnoreCase(ActivityDataParser.USER)) {
					user = data[1];
				} else if (data[0].equalsIgnoreCase(ActivityDataParser.TIME)) {
					date = data[1];
				} else if (data[0].equalsIgnoreCase(ActivityDataParser.STREAM)) {
					stream = data[1];
				} else if (data[0]
						.equalsIgnoreCase(ActivityDataParser.HEADLINE)) {
					headline = data[1];
				}

			}
		}// end foreach
	}

	public static ArrayList<Activity> listActivties() {
		return activities;
	}

	// This is for testing only.
	public static void main(String[] args) {
		final ArrayList<String> myLines = new ArrayList<String>();
		String workingDir = System.getProperty("user.dir");
		final File file = new File(workingDir
				+ "/src/net/sourceforge/eclipseccase/ucm/cc_output.txt");
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
				// System.out.println(strLine);
				myLines.add(strLine);
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		String[] input = myLines.toArray(new String[myLines.size()]);

		ActivityParser.process(input);
		ArrayList<Activity> activities = ActivityParser.listActivties();
		for (Activity activity : activities) {
			// date, activityId, user,
			// headline, stream)
			System.out.println("activityId :" + activity.getActivitySelector()
					+ "\n");
			System.out.println("headline   :" + activity.getHeadline());
			System.out.println("stream     :" + activity.getStream());
			System.out.println("user       :" + activity.getUser());
			System.out.println("date       :" + activity.getDate());
			System.out.println("\n");
		}
	}

}
