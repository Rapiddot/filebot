
package net.sourceforge.filebot.archive;


import static org.apache.commons.io.FilenameUtils.*;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sun.jna.Platform;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;


public class Archive implements Closeable {
	
	static {
		// initialize 7z-JBinding native libs
		try {
			if (Platform.isWindows()) {
				System.loadLibrary("lib7z-gcc");
			}
			
			System.loadLibrary("lib7z-JBinding");
			SevenZip.initLoadedLibraries();
		} catch (Throwable e) {
			Logger.getLogger(Archive.class.getName()).warning("Failed to load 7z-JBinding");
		}
	}
	
	private ISevenZipInArchive inArchive;
	private Closeable openVolume;
	
	
	public Archive(File file) throws SevenZipException, IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		
		ArchiveOpenVolumeCallback openVolumeCallback = new ArchiveOpenVolumeCallback();
		IInStream inStream = openVolumeCallback.getStream(file.getAbsolutePath());
		
		inArchive = SevenZip.openInArchive(null, inStream, openVolumeCallback);
		openVolume = openVolumeCallback;
	}
	
	
	public int itemCount() throws SevenZipException {
		return inArchive.getNumberOfItems();
	}
	
	
	public Map<PropID, Object> getItem(int index) throws SevenZipException {
		Map<PropID, Object> item = new EnumMap<PropID, Object>(PropID.class);
		
		for (PropID prop : PropID.values()) {
			Object value = inArchive.getProperty(index, prop);
			if (value != null) {
				item.put(prop, value);
			}
		}
		
		return item;
	}
	
	
	public List<File> listFiles() throws SevenZipException {
		List<File> paths = new ArrayList<File>();
		
		for (int i = 0; i < inArchive.getNumberOfItems(); i++) {
			boolean isFolder = (Boolean) inArchive.getProperty(i, PropID.IS_FOLDER);
			if (!isFolder) {
				String path = (String) inArchive.getProperty(i, PropID.PATH);
				paths.add(new File(path));
			}
		}
		
		return paths;
	}
	
	
	public void extract(ExtractOutProvider outputMapper) throws SevenZipException {
		inArchive.extract(null, false, new ExtractCallback(inArchive, outputMapper));
	}
	
	
	@Override
	public void close() throws IOException {
		try {
			inArchive.close();
		} catch (SevenZipException e) {
			throw new IOException(e);
		} finally {
			openVolume.close();
		}
	}
	
	
	public static final FileFilter VOLUME_ONE_FILTER = new FileFilter() {
		
		private Pattern exclude = Pattern.compile("[.]r[0-9]+$|[.]part[0-9]*[2-9][.]rar$|[.][0-9]*[2-9]$", Pattern.CASE_INSENSITIVE);
		
		
		@Override
		public boolean accept(File path) {
			if (exclude.matcher(path.getName()).find()) {
				return false;
			}
			
			String ext = getExtension(path.getName());
			for (ArchiveFormat it : ArchiveFormat.values()) {
				if (it.getMethodName().equalsIgnoreCase(ext)) {
					return true;
				}
			}
			
			return false;
		}
	};
	
}
