package com.tomsfreelance.spotifystreamer.Tasks;

import android.os.AsyncTask;

import com.tomsfreelance.spotifystreamer.ArtistSearchFragment;
import com.tomsfreelance.spotifystreamer.MainActivity;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by gabriella on 5/30/2015.
 */
public class SearchArtistsTask extends AsyncTask<String, Void, ArtistsPager> {

    public ArtistSearchFragment Parent = null;
    private SpotifyApi Spotify_API = null;
    private SpotifyService Spotify_Service = null;

    public SearchArtistsTask(ArtistSearchFragment parent) {
        Parent = parent;
    }

    protected ArtistsPager doInBackground(String... searchString) {
        Spotify_API = new SpotifyApi();
        Spotify_Service = Spotify_API.getService();
        return Spotify_Service.searchArtists(searchString[0]);
    }

    protected void onPostExecute(ArtistsPager result) {
        Parent.setArtistSearchResults(result);
    }
}
