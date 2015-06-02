package com.tomsfreelance.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tomsfreelance.spotifystreamer.Adapters.ArtistResultAdapter;
import com.tomsfreelance.spotifystreamer.Tasks.SearchArtistsTask;
import com.tomsfreelance.spotifystreamer.model.PlaybackArtist;
import com.tomsfreelance.spotifystreamer.model.PlaybackTrack;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * NOTE: When I upgraded to Windows 8, it set my "local account" to "gabriella"
 * for some dumb reason. I just realized that it is now saying Created by gabriella
 * all over the place...
 * (http://www.eightforums.com/tutorials/8782-user-account-name-change-windows-8-a.html)
 * - per the link above: I don't want to delete my account and risk losing files.
 *   I updated the template to use a hardcoded "teynon" instead of ${user}
 *
 * Created by teynon on 5/30/2015.
 */
public class MainActivity extends AppCompatActivity
                        implements ArtistSearchFragment.OnSelectArtistListener,
                                   ArtistHitsFragment.OnSelectTrackListener {

    private boolean TwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Log.i("Density", String.valueOf(metrics.densityDpi));

        if (findViewById(R.id.fragment_results) != null) {
                TwoPane = true;

                if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_results, new ArtistHitsFragment())
                        .commit();
            }
        }
        else {
            TwoPane = false;
        }
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
    public void onArtistSelected(Artist artist) {
        ArtistHitsFragment hitsFragment = null;
        if (TwoPane) {
            hitsFragment = (ArtistHitsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_results);
            hitsFragment.UpdateArtist(new PlaybackArtist(artist));
        }
        else {
            hitsFragment = new ArtistHitsFragment();
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.intentMsgArtist), new PlaybackArtist(artist));
            hitsFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_search, hitsFragment);
            transaction.addToBackStack(null);

            transaction.commit();
        }

        // Reference: http://stackoverflow.com/questions/14297178/setting-action-bar-title-and-subtitle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setSubtitle(artist.name);
        }
    }

    @Override
    public void OnTrackSelected(PlaybackTrack track, ArrayList<PlaybackTrack> trackList) {

    }
}
