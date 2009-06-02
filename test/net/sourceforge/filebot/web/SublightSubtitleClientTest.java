
package net.sourceforge.filebot.web;


import static org.junit.Assert.*;

import java.util.List;

import net.sublight.webservice.Subtitle;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class SublightSubtitleClientTest {
	
	private static SublightSubtitleClient client = new SublightSubtitleClient("Test;0.0");
	

	@BeforeClass
	public static void login() {
		// login manually
		client.login();
	}
	

	@Test
	public void search() {
		List<SearchResult> list = client.search("babylon 5");
		
		MovieDescriptor sample = (MovieDescriptor) list.get(0);
		
		// check sample entry
		assertEquals("Babylon 5", sample.getName());
		assertEquals(105946, sample.getImdbId());
		
		// check size
		assertEquals(8, list.size());
	}
	

	@Test
	public void getSubtitleListEnglish() {
		List<SubtitleDescriptor> list = client.getSubtitleList(new MovieDescriptor("Heroes", 2006, 813715), "English");
		
		SubtitleDescriptor sample = list.get(0);
		
		assertTrue(sample.getName().startsWith("Heroes"));
		assertEquals("English", sample.getLanguageName());
		
		// check size
		assertTrue(list.size() > 45);
	}
	

	@Test
	public void getSubtitleListAllLanguages() {
		List<SubtitleDescriptor> list = client.getSubtitleList(new MovieDescriptor("Babylon 5", 1994, 105946), null);
		
		SubtitleDescriptor sample = list.get(0);
		
		assertEquals("Babylon.5.S01E01.Midnight.on.the.Firing.Line.AC3.DVDRip.DivX-AMC", sample.getName());
		assertEquals("Slovenian", sample.getLanguageName());
		
		// check size
		assertTrue(list.size() > 45);
	}
	

	@Test
	public void getSubtitleListVideoHash() {
		List<Subtitle> list = client.getSubtitleList("000a20000045eacfebd3c2c83bfb4ea1598b14e9be7db38316fd", null, null, "English");
		
		Subtitle sample = list.get(0);
		
		assertEquals("Terminator: The Sarah Connor Chronicles", sample.getTitle());
		assertEquals(2, sample.getSeason(), 0);
		assertEquals(22, sample.getEpisode(), 0);
		assertEquals("Terminator.The.Sarah.Connor.Chronicles.S02E22.HDTV.XviD-2HD", sample.getRelease());
		assertTrue(sample.isIsLinked());
	}
	

	@AfterClass
	public static void logout() {
		// logout manually
		client.logout();
	}
	
}
