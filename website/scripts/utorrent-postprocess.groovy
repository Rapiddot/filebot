// filebot -script "fn:utorrent-postprocess" --output "X:/media" --action copy --conflict override -non-strict -trust-script -Xxbmc=localhost "-Xut_dir=%D" "-Xut_file=%F" "-Xut_label=%L" "-Xut_state=%S" "-Xut_kind=%K"
def input = []

// print input parameters
_args.parameters.each{ k, v -> println "Parameter: $k = $v" }

if (ut_kind == "multi") {
	input += new File(ut_dir).getFiles() // multi-file torrent
} else {
	input += new File(ut_dir, ut_file) // single-file torrent
}

// extract archives if necessary
input += extract(file:input, output:".", conflict:"override")

// process only media files
input = input.findAll{ it.isVideo() || it.isSubtitle() }

// ignore clutter files
input = input.findAll{ !(it.path =~ /\b(?i:sample|trailer|extras|deleted.scenes|music.video|scrapbook)\b/) }

// print input fileset
input.each{ println "Input: $it" }

// xbmc artwork/nfo utility
include("fn:lib/xbmc")

// group episodes/movies and rename according to XBMC standards
def groups = input.groupBy{
	def tvs = detectSeriesName(it)
	def mov = detectMovie(it, false)
	println "$it.name [series: $tvs, movie: $mov]"
	
	// DECIDE EPISODE VS MOVIE (IF NOT CLEAR)
	if (tvs && mov) {
		if (it.name =~ "(?i:$tvs - .+)" || parseEpisodeNumber(it.name) || parseDate(it.name)) {
			println "Exclude Movie: $mov"
			mov = null
		} else if (detectMovie(it, true)) {
			println "Exclude Series: $tvs"
			tvs = null
		}
	}
	return [tvs:tvs, mov:mov]
}

groups.each{ group, files ->
	// fetch subtitles
	def subs = getMissingSubtitles(file:files, output:"srt", encoding:"utf-8")
	if (subs) files += subs
	
	// EPISODE MODE
	if (group.tvs && !group.mov) {
		def dest = rename(file:files, format:'TV Shows/{n}/{episode.special ? "Special" : "Season "+s}/{n} - {episode.special ? "S00E"+special.pad(2) : s00e00} - {t}', db:'TheTVDB')
		if (dest != null || _args.conflict == 'fail') { // allow script to crash via --conflict fail
			dest.mapByFolder().keySet().each{ dir ->
				println "Fetching artwork for $dir from TheTVDB"
				def query = group.tvs
				def sxe = dest.findResult{ parseEpisodeNumber(it) }
				def options = TheTVDB.search(query)
				if (options.isEmpty()) {
					println "TV Series not found: $query"
					return
				}
				options = options.sortBySimilarity(query, { it.name })
				fetchSeriesArtworkAndNfo(dir.dir, dir, options[0], sxe && sxe.season > 0 ? sxe.season : 1)
			}
		}
	}
	
	// MOVIE MODE
	if (group.mov && !group.tvs) {
		def dest = rename(file:files, format:'Movies/{n} ({y})/{n} ({y}){" CD$pi"}', db:'TheMovieDB')
		if (dest != null || _args.conflict == 'fail') { // allow script to crash via --conflict fail
			dest.mapByFolder().keySet().each{ dir ->
				println "Fetching artwork for $dir from TheMovieDB"
				fetchMovieArtworkAndNfo(dir, group.mov)
			}
		}
	}
}



// make XBMC scan for new content
xbmc.split(/[\s,|]+/).each{
	println "Notify XBMC: $it"
	invokeScanVideoLibrary(it)
}
