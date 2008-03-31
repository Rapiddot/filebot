
package net.sourceforge.filebot.web;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.tuned.DownloadTask;


/**
 * Describes a subtitle on OpenSubtitles.
 * 
 * @see OpenSubtitlesClient
 */
public class OpenSubtitleDescriptor implements SubtitleDescriptor {
	
	private final Map<String, String> properties;
	
	
	public static enum Properties {
		IDSubMovieFile,
		MovieHash,
		MovieByteSize,
		MovieTimeMS,
		MovieFrames,
		IDSubtitleFile,
		SubFileName,
		SubActualCD,
		SubSize,
		SubHash,
		IDSubtitle,
		UserID,
		SubLanguageID,
		SubFormat,
		SubSumCD,
		SubAuthorComment,
		SubAddDate,
		SubBad,
		SubRating,
		SubDownloadsCnt,
		MovieReleaseName,
		IDMovie,
		IDMovieImdb,
		MovieName,
		MovieNameEng,
		MovieYear,
		MovieImdbRating,
		UserNickName,
		ISO639,
		LanguageName,
		SubDownloadLink,
		ZipDownloadLink,
	}
	
	
	public OpenSubtitleDescriptor(Map<String, String> properties) {
		this.properties = properties;
	}
	

	public String getProperty(Properties property) {
		return properties.get(property.name());
	}
	

	@Override
	public String getName() {
		return getProperty(Properties.SubFileName);
	}
	

	public String getLanguageName() {
		return getProperty(Properties.LanguageName);
	}
	

	public long getSize() {
		return Long.parseLong(getProperty(Properties.SubSize));
	}
	

	public URL getDownloadLink() {
		String link = getProperty(Properties.ZipDownloadLink);
		
		try {
			return new URL(link);
		} catch (MalformedURLException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Invalid download link: " + link);
			return null;
		}
	}
	

	@Override
	public DownloadTask createDownloadTask() {
		return new DownloadTask(getDownloadLink());
	}
	

	@Override
	public String getArchiveType() {
		return "zip";
	}
	

	@Override
	public String toString() {
		return String.format("%s [%s]", getName(), getLanguageName());
	}
	
}
