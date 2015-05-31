package com.tomsfreelance.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tomsfreelance.spotifystreamer.Adapters.ArtistResultAdapter;
import com.tomsfreelance.spotifystreamer.Adapters.TrackResultAdapter;
import com.tomsfreelance.spotifystreamer.Tasks.TopTracksForArtistTask;
import com.tomsfreelance.spotifystreamer.model.PlaybackTrack;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * NOTE: When I upgraded to Windows 8, it set my "local account" to "gabriella"
 * for some dumb reason. I just realized that it is now saying Created by gabriella
 * all over the place...
 *
 * Created by teynon on 5/30/2015.
 */
public class ArtistHitsActivity extends AppCompatActivity {

    private Context ctx = this;
    private String ArtistName = "";
    private String ArtistID = "";
    private ListView trackResults = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_hits);
        Initialize();
    }

    private void Initialize() {
        Intent intent = getIntent();
        ArtistName = intent.getStringExtra(getString(R.string.intentMsgArtistName));
        ArtistID = intent.getStringExtra(getString(R.string.intentMsgArtistID));

        trackResults = (ListView)findViewById(R.id.listTracks);

        // Reference: http://stackoverflow.com/questions/14297178/setting-action-bar-title-and-subtitle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setSubtitle(ArtistName);
        }

        // Get the tracks.
        TopTracksForArtistTask trackTask = new TopTracksForArtistTask(this, getString(R.string.topTracksCountryCode));
        trackTask.execute(ArtistID);

        InitializeListeners();
    }

    private void InitializeListeners() {
        trackResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrackResultAdapter adapter = (TrackResultAdapter) trackResults.getAdapter();

                // Load intent for selected artist.
                Intent playbackIntent = new Intent(ctx, PlaybackActivity.class);

                // Prep data to send to playback activity.

                playbackIntent.putExtra(getString(R.string.intentMsgTrack), adapter.getItem(position));
                playbackIntent.putParcelableArrayListExtra(getString(R.string.intentMsgTrackList), adapter.GetTracks());
                startActivity(playbackIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TrackResultAdapter adapter = (TrackResultAdapter) trackResults.getAdapter();
        outState.putParcelableArrayList("TrackList", adapter.GetTracks());
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);

        ArrayList<PlaybackTrack> tracks = inState.getParcelableArrayList("TrackList");
        setArtistTracks(tracks);
    }

    public void setArtistTracks(Tracks results) {
        ArrayList<PlaybackTrack> tracks = new ArrayList<PlaybackTrack>();

        for (Track t : results.tracks) {
            tracks.add(new PlaybackTrack(t));
        }

        setArtistTracks(tracks);
    }

    public void setArtistTracks(ArrayList<PlaybackTrack> tracks) {
        TrackResultAdapter resultAdapter = new TrackResultAdapter(ctx, tracks);
        trackResults.setAdapter(resultAdapter);
    }
}
