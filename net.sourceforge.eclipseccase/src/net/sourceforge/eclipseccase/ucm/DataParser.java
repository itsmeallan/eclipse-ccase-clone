package net.sourceforge.eclipseccase.ucm;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */

/**
 * @author mikael petterson
 * 
 */
public class DataParser {

	public static final String USER = "user";
	public static final String NO_DATA = "noData";
	public static final String TIME = "dateAndTime";
	public static final String STREAM = "streamName";
	public static final String HEADLINE = "headline";

	private static final Pattern time = Pattern.compile("(\\d{2,}.*)\\sby.*");
	private static final Matcher timeMatcher = time.matcher("");

	private static final Pattern user = Pattern.compile("owner:\\s(.*)");
	private static final Matcher userMatcher = user.matcher("");

	private static final Pattern stream = Pattern.compile("stream:\\s(.*)");
	private static final Matcher streamMatcher = stream.matcher("");
	
	private static final Pattern headline = Pattern.compile("title:\\s(.*)");
	private static final Matcher headlineMatcher = headline.matcher("");

	
	public static String[] processLine(String line) {
		if (timeMatcher.find()) {
			System.out.print("Match " + timeMatcher.group(1)+"\n");
			return new String[] { TIME, timeMatcher.group(1) };

		} else if (userMatcher.find()) {
			System.out.print("Match " + userMatcher.group(1)+"\n");
			return new String[] { USER, userMatcher.group(1) };

		} else if (streamMatcher.find()) {
			System.out.print("Match " + streamMatcher.group(1)+"\n");
			return new String[] { STREAM, streamMatcher.group(1) };

		}else if(headlineMatcher.find()){
			System.out.print("Match " + headlineMatcher.group(1)+"\n");
			return new String[] { HEADLINE, headlineMatcher.group(1) };

		}

		return new String[] { NO_DATA, "" };

	}

}
