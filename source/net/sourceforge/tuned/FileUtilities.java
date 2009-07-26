
package net.sourceforge.tuned;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class FileUtilities {
	
	public static final long KILO = 1024;
	public static final long MEGA = KILO * 1024;
	public static final long GIGA = MEGA * 1024;
	

	public static String formatSize(long size) {
		if (size >= MEGA)
			return String.format("%,d MB", size / MEGA);
		else if (size >= KILO)
			return String.format("%,d KB", size / KILO);
		else
			return String.format("%,d Byte", size);
	}
	

	/**
	 * Pattern used for matching file extensions.
	 * 
	 * e.g. "file.txt" -> match "txt", ".hidden" -> no match
	 */
	private static final Pattern extension = Pattern.compile("(?<=.[.])\\p{Alnum}+$");
	

	public static String getExtension(File file) {
		if (file.isDirectory())
			return null;
		
		return getExtension(file.getName());
	}
	

	public static String getExtension(String name) {
		Matcher matcher = extension.matcher(name);
		
		if (matcher.find()) {
			// extension without leading '.'
			return matcher.group();
		}
		
		// no extension
		return null;
	}
	

	public static boolean hasExtension(File file, String... extensions) {
		if (file.isDirectory())
			return false;
		
		return hasExtension(file.getName(), extensions);
	}
	

	public static boolean hasExtension(String filename, String... extensions) {
		String extension = getExtension(filename);
		
		if (extension != null) {
			for (String entry : extensions) {
				if (extension.equalsIgnoreCase(entry))
					return true;
			}
		}
		
		return false;
	}
	

	public static String getNameWithoutExtension(String name) {
		Matcher matcher = extension.matcher(name);
		
		if (matcher.find()) {
			return name.substring(0, matcher.start() - 1);
		}
		
		// no extension, return given name
		return name;
	}
	

	public static String getName(File file) {
		if (file.isDirectory())
			return getFolderName(file);
		
		return getNameWithoutExtension(file.getName());
	}
	

	public static String getFolderName(File file) {
		String name = file.getName();
		
		if (!name.isEmpty())
			return name;
		
		// file might be a drive (only has a path, but no name)
		return file.toString();
	}
	

	public static boolean containsOnly(Iterable<File> files, FileFilter filter) {
		for (File file : files) {
			if (!filter.accept(file))
				return false;
		}
		
		return true;
	}
	

	public static List<File> filter(Iterable<File> files, FileFilter... filters) {
		List<File> accepted = new ArrayList<File>();
		
		for (File file : files) {
			for (FileFilter filter : filters) {
				if (filter.accept(file)) {
					accepted.add(file);
					break;
				}
			}
		}
		
		return accepted;
	}
	

	public static final FileFilter FOLDERS = new FileFilter() {
		
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};
	
	public static final FileFilter FILES = new FileFilter() {
		
		@Override
		public boolean accept(File file) {
			return file.isFile();
		}
	};
	

	public static class ExtensionFileFilter implements FileFilter {
		
		private final String[] extensions;
		

		public ExtensionFileFilter(String... extensions) {
			this.extensions = extensions;
		}
		

		public ExtensionFileFilter(Collection<String> extensions) {
			this.extensions = extensions.toArray(new String[0]);
		}
		

		@Override
		public boolean accept(File file) {
			return hasExtension(file, extensions);
		}
		

		public boolean accept(String name) {
			return hasExtension(name, extensions);
		}
		

		public boolean acceptExtension(String extension) {
			for (String other : extensions) {
				if (other.equalsIgnoreCase(extension))
					return true;
			}
			
			return false;
		}
		

		public List<String> extensions() {
			return Arrays.asList(extensions);
		}
	}
	

	/**
	 * Dummy constructor to prevent instantiation.
	 */
	private FileUtilities() {
		throw new UnsupportedOperationException();
	}
	
}
