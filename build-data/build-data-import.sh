#!/bin/sh

fetch()
{
    FILE="$1"
    LINK="$2"
    TIME="$3"

    echo "Fetch $FILE"
    if [ ! -f "$FILE" ] || test "`find $FILE -mtime $TIME`"; then
        curl -L -o "$FILE" -z "$FILE" "$LINK"
    fi
}

fetch anidb.txt.gz 'http://anidb.net/api/anime-titles.dat.gz' +5
fetch tvdb.zip 'http://thetvdb.com/api/58B4AA94C59AD656/updates/updates_all.zip' +5
fetch omdb.zip 'http://beforethecode.com/projects/omdb/download.aspx?e=reinhard.pointner%40gmail.com&tsv=movies' +30
fetch osdb.txt 'http://www.opensubtitles.org/addons/export_movie.php' +30

gunzip -k -f anidb.txt.gz
unzip -o tvdb.zip
unzip -o omdb.zip

echo 'DONE'
