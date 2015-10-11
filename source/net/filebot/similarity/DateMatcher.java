package net.filebot.similarity;

import static net.filebot.util.FileUtilities.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.filebot.web.SimpleDate;

public class DateMatcher {

	private final DatePattern[] patterns;

	public DateMatcher() {
		patterns = new DatePattern[4];

		// match yyyy-mm-dd patterns like 2010-10-24, 2009/6/1, etc
		patterns[0] = new NumericDatePattern("(?<!\\p{Alnum})(\\d{4})[^\\p{Alnum}](\\d{1,2})[^\\p{Alnum}](\\d{1,2})(?!\\p{Alnum})", new int[] { 1, 2, 3 });

		// match dd-mm-yyyy patterns like 1.1.2010, 01/06/2010, etc
		patterns[1] = new NumericDatePattern("(?<!\\p{Alnum})(\\d{1,2})[^\\p{Alnum}](\\d{1,2})[^\\p{Alnum}](\\d{4})(?!\\p{Alnum})", new int[] { 3, 2, 1 });

		// match yyyy.MMMMM.dd patterns like 2015.October.05 etc
		patterns[2] = new DateFormatPattern("(?<!\\p{Alnum})(\\d{4})[^\\p{Alnum}](?i:January|February|March|April|May|June|July|August|September|October|November|December)[^\\p{Alnum}](\\d{1,2})(?!\\p{Alnum})", "yyyy MMMMM dd");

		// match yyyy.MMM.dd patterns like 2015.Oct.06 etc
		patterns[3] = new DateFormatPattern("(?<!\\p{Alnum})(\\d{4})[^\\p{Alnum}](?i:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[^\\p{Alnum}](\\d{1,2})(?!\\p{Alnum})", "yyyy MMM dd");
	}

	public DateMatcher(DatePattern... patterns) {
		this.patterns = patterns;
	}

	public SimpleDate match(CharSequence seq) {
		for (DatePattern pattern : patterns) {
			SimpleDate match = pattern.match(seq);

			if (match != null) {
				return match;
			}
		}

		return null;
	}

	public int find(CharSequence seq, int fromIndex) {
		for (DatePattern pattern : patterns) {
			int pos = pattern.find(seq, fromIndex);

			if (pos >= 0) {
				return pos;
			}
		}

		return -1;
	}

	public SimpleDate match(File file) {
		for (String name : tokenizeTail(file)) {
			for (DatePattern pattern : patterns) {
				SimpleDate match = pattern.match(name);

				if (match != null) {
					return match;
				}
			}
		}
		return null;
	}

	protected List<String> tokenizeTail(File file) {
		List<String> tail = new ArrayList<String>(2);
		for (File f : listPathTail(file, 2, true)) {
			tail.add(getName(f));
		}
		return tail;
	}

	private static interface DatePattern {

		public SimpleDate match(CharSequence seq);

		public int find(CharSequence seq, int fromIndex);

	}

	private static class NumericDatePattern implements DatePattern {

		protected final Pattern pattern;
		protected final int[] order;

		public NumericDatePattern(String pattern, int[] order) {
			this.pattern = Pattern.compile(pattern);
			this.order = order;
		}

		protected SimpleDate process(MatchResult match) {
			return new SimpleDate(Integer.parseInt(match.group(order[0])), Integer.parseInt(match.group(order[1])), Integer.parseInt(match.group(order[2])));
		}

		public SimpleDate match(CharSequence seq) {
			Matcher matcher = pattern.matcher(seq);

			if (matcher.find()) {
				return process(matcher);
			}

			return null;
		}

		public int find(CharSequence seq, int fromIndex) {
			Matcher matcher = pattern.matcher(seq).region(fromIndex, seq.length());

			if (matcher.find()) {
				return matcher.start();
			}

			return -1;
		}

	}

	private static class DateFormatPattern implements DatePattern {

		protected final Pattern space = Pattern.compile("[^\\p{Alnum}]+");

		protected final Pattern pattern;
		protected final String dateFormat;

		public DateFormatPattern(String pattern, String dateFormat) {
			this.pattern = Pattern.compile(pattern);
			this.dateFormat = dateFormat;
		}

		protected SimpleDate process(MatchResult match) {
			return SimpleDate.parse(space.matcher(match.group()).replaceAll(" "), dateFormat);
		}

		public SimpleDate match(CharSequence seq) {
			Matcher matcher = pattern.matcher(seq);

			if (matcher.find()) {
				return process(matcher);
			}

			return null;
		}

		public int find(CharSequence seq, int fromIndex) {
			Matcher matcher = pattern.matcher(seq).region(fromIndex, seq.length());

			if (matcher.find()) {
				if (process(matcher) != null) {
					return matcher.start();
				}
			}

			return -1;
		}

	}

}
