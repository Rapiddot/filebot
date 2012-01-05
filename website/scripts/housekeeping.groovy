// filebot -script "http://filebot.sf.net/scripts/housekeeping.groovy" <folder>

/*
* Watch folder for new tv shows and automatically 
* move/rename new episodes into a predefined folder structure
*/

// check for new media files once every 5 minutes
def updateFrequency = 5 * 60 * 1000

// V:/TV Shows/Stargate/Season 1/Stargate.S01E01.Pilot
def episodeFormat = "{com.sun.jna.Platform.isWindows() ? file[0] : home}/TV Shows/{n}{'/Season '+s}/{n.space('.')}.{s00e00}.{t.space('.')}"

// spawn daemon thread
Thread.startDaemon {
	while (sleep(updateFrequency) || true) {
		args.eachMediaFolder {
			rename(folder:it, db: "thetvdb", format:episodeFormat)
		}
	}
}

println "Press ENTER to abort"
console.readLine() // keep script running until aborted by user
