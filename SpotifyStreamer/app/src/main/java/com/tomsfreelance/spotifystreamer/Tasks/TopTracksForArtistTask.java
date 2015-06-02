package com.tomsfreelance.spotifystreamer.Tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.tomsfreelance.spotifystreamer.ArtistHitsActivity;
import com.tomsfreelance.spotifystreamer.ArtistHitsFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by gabriella on 5/30/2015.
 */
public class TopTracksForArtistTask extends AsyncTask<String, Void, Tracks> {

    public ArtistHitsFragment Parent = null;
    private SpotifyApi Spotify_API = null;
    private SpotifyService Spotify_Service = null;
    public String CountryCode = "us";

    public TopTracksForArtistTask(ArtistHitsFragment parent, String countryCode) {
        Parent = parent;
        CountryCode = countryCode;
    }

    protected Tracks doInBackground(String... searchString) {
        Spotify_API = new SpotifyApi();
        Spotify_Service = Spotify_API.getService();

        // According to https://developer.spotify.com/web-api/get-artists-top-tracks/
        // "country" is required.
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("country", CountryCode);

        return Spotify_Service.getArtistTopTrack(searchString[0], map);
    }

    protected void onPostExecute(Tracks result) {
        Parent.setArtistTracks(result);
    }
}
