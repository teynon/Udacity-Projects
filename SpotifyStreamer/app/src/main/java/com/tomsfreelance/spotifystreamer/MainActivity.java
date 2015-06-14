package com.tomsfreelance.spotifystreamer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tomsfreelance.spotifystreamer.Enums.PlaybackBroadcastAction;
import com.tomsfreelance.spotifystreamer.Model.PlaybackArtist;
import com.tomsfreelance.spotifystreamer.Model.PlaybackTrack;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;


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

    public static boolean TwoPane = false;
    private Menu ActionMenu;
    private boolean ShowNowPlaying = false;
    private PlaybackFragment playbackFragment = null;

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(getString(R.string.intentMsgPlaybackAction))) {
            PlaybackBroadcastAction action = (PlaybackBroadcastAction)intent.getSerializableExtra(getString(R.string.intentMsgPlaybackAction));

            if (action == PlaybackBroadcastAction.PLAYBACK_UPDATE)
                OpenPlayer(-1, null, PlaybackBroadcastAction.PLAYBACK_UPDATE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        ActionMenu.findItem(R.id.action_resumePlayback).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
                if (fragment != null)
                    return false;

                OpenPlayer(-1, null, PlaybackBroadcastAction.PLAYBACK_UPDATE);
                return true;
            }
        });

        if (ShowNowPlaying) {
            ShowNowPlayingAction();
        }
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
    public void onArtistSelected(PlaybackArtist artist) {
        ArtistHitsFragment hitsFragment = null;
        if (TwoPane) {
            hitsFragment = (ArtistHitsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_results);
            hitsFragment.UpdateArtist(artist);
        }
        else {
            hitsFragment = new ArtistHitsFragment();
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.intentMsgArtist), artist);
            hitsFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_search, hitsFragment);
            transaction.addToBackStack(null);

            transaction.commit();
        }

        // Reference: http://stackoverflow.com/questions/14297178/setting-action-bar-title-and-subtitle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setSubtitle(artist.ArtistName);
        }
    }

    @Override
    public void OnTrackSelected(int track, ArrayList<PlaybackTrack> trackList) {
        OpenPlayer(track, trackList, PlaybackBroadcastAction.PLAYBACK_START);
    }

    public void OpenPlayer(int track, ArrayList<PlaybackTrack> trackList, PlaybackBroadcastAction action) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.intentMsgPlaybackAction), action);
        args.putInt(getString(R.string.intentMsgTrack), track);
        args.putParcelableArrayList(getString(R.string.intentMsgTrackList), trackList);

        if (playbackFragment != null) {
            playbackFragment.dismiss();
            playbackFragment = null;
        }

        playbackFragment = new PlaybackFragment();
        playbackFragment.setArguments(args);

        if (MainActivity.TwoPane) {
            playbackFragment.show(fragmentManager, getString(R.string.playbackTag));
        }
        else {
            if (!fragmentManager.popBackStackImmediate(getString(R.string.playbackBackstackName), 0) &&
                    fragmentManager.findFragmentByTag(getString(R.string.playbackTag)) == null)
            {

                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                transaction.replace(android.R.id.content, playbackFragment, getString(R.string.playbackTag)).addToBackStack(getString(R.string.playbackBackstackName)).commit();
            }
        }

        ShowNowPlayingAction();
    }

    public void ShowNowPlayingAction() {
        if (ActionMenu != null) {
            MenuItem resume = ActionMenu.findItem(R.id.action_resumePlayback);
            if (resume != null) resume.setVisible(true);
        }
        ShowNowPlaying = true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ShowNowPlaying = savedInstanceState.getBoolean(getString(R.string.intentMsgNowPlaying));
        if (ActionMenu != null && ShowNowPlaying) ShowNowPlayingAction();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(getString(R.string.intentMsgNowPlaying), ShowNowPlaying);
    }
}
