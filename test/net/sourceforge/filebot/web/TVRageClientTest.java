
package net.sourceforge.filebot.web;


import static org.junit.Assert.assertEquals;

import java.util.List;

import net.sourceforge.filebot.web.TVRageClient.TVRageSearchResult;

import org.junit.Test;


public class TVRageClientTest {
	
	private static TVRageClient tvrage = new TVRageClient();
	private static TVRageSearchResult testResult = new TVRageSearchResult("Buffy the Vampire Slayer", 2930, "http://www.tvrage.com/Buffy_The_Vampire_Slayer");
	
	
	@Test
	public void search() throws Exception {
		List<SearchResult> results = tvrage.search("Buffy");
		
		TVRageSearchResult result = (TVRageSearchResult) results.get(0);
		
		assertEquals(testResult.getName(), result.getName());
		assertEquals(testResult.getShowId(), result.getShowId());
		assertEquals(testResult.getLink(), result.getLink());
	}
	

	@Test
	public void getEpisodeList() throws Exception {
		List<Episode> list = tvrage.getEpisodeList(testResult, 7);
		
		assertEquals(22, list.size());
		
		Episode chosen = list.get(21);
		
		assertEquals("Buffy the Vampire Slayer", chosen.getShowName());
		assertEquals("Chosen", chosen.getTitle());
		assertEquals("22", chosen.getNumberOfEpisode());
		assertEquals("7", chosen.getNumberOfSeason());
	}
	

	@Test
	public void getEpisodeListAll() throws Exception {
		List<Episode> list = tvrage.getEpisodeList(testResult);
		
		assertEquals(145, list.size());
		
		Episode first = list.get(0);
		
		assertEquals("Buffy the Vampire Slayer", first.getShowName());
		assertEquals("Unaired Pilot", first.getTitle());
		assertEquals("00", first.getNumberOfEpisode());
		assertEquals("0", first.getNumberOfSeason());
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void getEpisodeListIllegalSeason() throws Exception {
		tvrage.getEpisodeList(testResult, 42);
	}
	

	@Test
	public void getEpisodeListLink() throws Exception {
		assertEquals(tvrage.getEpisodeListLink(testResult, 1).toString(), "http://www.tvrage.com/Buffy_The_Vampire_Slayer/episode_list/1");
	}
	

	@Test
	public void getEpisodeListLinkAll() throws Exception {
		assertEquals(tvrage.getEpisodeListLink(testResult).toString(), "http://www.tvrage.com/Buffy_The_Vampire_Slayer/episode_list/all");
	}
	
}
