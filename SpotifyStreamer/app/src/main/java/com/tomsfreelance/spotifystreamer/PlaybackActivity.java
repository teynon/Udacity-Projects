package com.tomsfreelance.spotifystreamer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tomsfreelance.spotifystreamer.Tasks.TopTracksForArtistTask;
import com.tomsfreelance.spotifystreamer.model.PlaybackTrack;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by teynon on 5/31/2015.
 */
public class PlaybackActivity extends AppCompatActivity {

    private PlaybackTrack CurrentTrack;
    private ArrayList<PlaybackTrack> TrackList;
    private TextView txtArtist;
    private TextView txtAlbum;
    private TextView txtSong;
    private ImageView imgAlbum;
    private SeekBar seekBar;
    private TextView currentTime;
    private TextView remainingTime;
    private long trackTimeRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_playback);
        Initialize();
    }

    private void Initialize() {
        Intent intent = getIntent();

        // Get the track details.
        CurrentTrack = intent.getParcelableExtra(getString(R.string.intentMsgTrack));
        TrackList = intent.getParcelableArrayListExtra(getString(R.string.intentMsgTrackList));

        txtArtist = (TextView)findViewById(R.id.txtPlaybackArtistName);
        txtAlbum = (TextView)findViewById(R.id.txtPlaybackAlbumName);
        txtSong = (TextView)findViewById(R.id.playbackSongName);
        imgAlbum = (ImageView)findViewById(R.id.imgPlaybackAlbum);
        seekBar = (SeekBar)findViewById(R.id.playbackSeekbar);
        currentTime = (TextView)findViewById(R.id.txtSeekbarCurrent);
        remainingTime = (TextView)findViewById(R.id.txtSeekbarRemaining);

        txtArtist.setText(CurrentTrack.Artist);
        txtAlbum.setText(CurrentTrack.AlbumName);
        txtSong.setText(CurrentTrack.SongName);

        trackTimeRemaining = CurrentTrack.TrackLength;
        Picasso.with(this).load(CurrentTrack.AlbumImage).into(imgAlbum);

        UpdateSeekTimeRemaining();

        // TODO - (Stage 2 I think?) Get the track details and start streaming.
    }

    private void UpdateSeekTimeRemaining() {
        int minutes = (int)(trackTimeRemaining / 1000 / 60);
        int seconds = (int)((trackTimeRemaining / 1000) % 60);
        remainingTime.setText(Integer.toString(minutes) + ":" + String.format("%02d", seconds));
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

        outState.putParcelable("CurrentTrack", CurrentTrack);
        outState.putParcelableArrayList("TrackList", TrackList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);

        CurrentTrack = inState.getParcelable("CurrentTrack");
        TrackList = inState.getParcelableArrayList("TrackList");
    }
}
