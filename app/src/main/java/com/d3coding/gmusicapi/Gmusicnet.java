package com.d3coding.gmusicapi;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.api.TrackApi;
import com.github.felixgail.gplaymusic.model.Track;
import com.github.felixgail.gplaymusic.model.snippets.ArtRef;
import com.github.felixgail.gplaymusic.util.TokenProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import svarzee.gps.gpsoauth.AuthToken;
import svarzee.gps.gpsoauth.Gpsoauth;

public class Gmusicnet extends AsyncTask<String, Void, Void> {

    public class Chunck {

        public String id, title, artist, composer, album, albumArtist;
        public int year, trackNumber;
        public String genre, albumArtUrl;
        public Long estimatedSize, time;
        public String albumId, artistId, comment;
        public int totalTrackCount;

        Chunck(String id, String title, String artist, String composer, String album, String albumArtist, int year, int trackNumber,
               String genre, String albumArtUrl, Long estimatedSize, Long time, String albumId, String artistId, String comment, int totalTrackCount) {

            this.id = id;
            this.title = title;
            this.artist = artist;
            this.composer = composer;
            this.album = album;
            this.albumArtist = albumArtist;
            this.year = year;
            this.trackNumber = trackNumber;
            this.genre = genre;
            this.albumArtUrl = albumArtUrl;
            this.estimatedSize = estimatedSize;
            this.time = time;
            this.albumId = albumId;
            this.artistId = artistId;
            this.comment = comment;
            this.totalTrackCount = totalTrackCount;

        }
    }

    List<Chunck> chunckList;

    private Context context;

    public Gmusicnet(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {

            System.out.println("#010");
            AuthToken authToken;

            if (strings.length > 0)
                authToken = TokenProvider.provideToken(strings[0]);
            else
                throw new Exception("Argumnent");

            System.out.println("#001");
            GPlayMusic api = new GPlayMusic.Builder().setAuthToken(authToken).build();
            System.out.println("#002");
            TrackApi trackApi = api.getTrackApi();
            System.out.println("#003");
            List<Track> listTrack = trackApi.getLibraryTracks();
            System.out.println("#004");
            System.out.println("# Size: " + listTrack.size());

            chunckList = new ArrayList<>();

            for (int x = 0; x < listTrack.size(); ++x) {
                String id, title, artist, composer, album, albumArtist, genre, albumArtUrl, albumId, artistId, comment;
                int year, trackNumber, totalTrackCount;
                Long estimatedSize;
                // id
                id = listTrack.get(x).getID();
                // title
                title = listTrack.get(x).getTitle();
                // artist
                artist = listTrack.get(x).getArtist();
                // composer
                composer = listTrack.get(x).getComposer();
                // album
                album = listTrack.get(x).getAlbum();
                // albumArtist
                albumArtist = listTrack.get(x).getAlbumArtist();
                // year
                year = listTrack.get(x).getYear().getAsInt();
                // trackNumber
                trackNumber = listTrack.get(x).getTrackNumber();
                // genre
                Optional<String> optionalGenre = listTrack.get(x).getGenre();
                if (optionalGenre.isPresent())
                    genre = optionalGenre.get();
                else
                    genre = "";
                // albumArtUrl
                Optional<List<ArtRef>> optionalArtRefs = listTrack.get(x).getAlbumArtRef();
                if (optionalArtRefs.isPresent())
                    albumArtUrl = optionalArtRefs.get().get(0).getUrl();
                else
                    albumArtUrl = "";
                // estimatedSize
                // estimatedSize =
                try {
                    estimatedSize = listTrack.get(x).getEstimatedSize();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.out.println("Code: ###" + id + "###");
                    System.out.println("Code: ###" + artist + "###");
                    System.out.println("Code: ###" + album + "###");
                    System.out.println("Code: ###" + title + "###");
                    estimatedSize = 0L;
                }
                // albumId
                albumId = listTrack.get(x).getAlbumId();
                // artistId
                Optional<List<String>> optionalArtistId = listTrack.get(x).getArtistId();
                if (optionalArtistId.isPresent()) {
                    artistId = optionalArtistId.get().get(0);
                    for (int y = 1; y < optionalArtistId.get().size(); ++y)
                        artistId += optionalArtistId.get().get(y);
                } else
                    artistId = "";
                // comment
                Optional<String> optionalComment = listTrack.get(x).getComment();
                if (optionalComment.isPresent())
                    comment = optionalComment.get();
                else
                    comment = "";
                // totalTrackCount
                OptionalInt optionalTotalTrackCount = listTrack.get(x).getTotalTrackCount();
                if (optionalTotalTrackCount.isPresent())
                    totalTrackCount = optionalTotalTrackCount.getAsInt();
                else
                    totalTrackCount = 0;

                // TODO
                chunckList.add(new Chunck(id, title, artist, composer, album, albumArtist, year, trackNumber,
                        genre, albumArtUrl, estimatedSize, 100L, albumId, artistId, comment, totalTrackCount));
            }

            System.out.println("#005");

            Gmusicdb gmusicdb = new Gmusicdb(context);
            for (int x = 0; x < chunckList.size(); ++x)
                gmusicdb.insertIfNotExists(chunckList.get(x));

            System.out.println("#006");
        } catch (IOException e) {
            System.out.println("#100");
            e.printStackTrace();
        } catch (Gpsoauth.TokenRequestFailed e) {
            System.out.println("#666");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}