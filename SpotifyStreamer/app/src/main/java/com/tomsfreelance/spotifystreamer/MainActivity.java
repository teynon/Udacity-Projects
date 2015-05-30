package com.tomsfreelance.spotifystreamer;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


public class MainActivity extends ActionBarActivity {

    private EditText txtArtistSearch = null;
    private Context ctx = this;
    private Toast quickNotice = null;
    private ListView artistResults = null;
    private SearchArtistsTask searchTask = null;
    private MainActivity self = this;

    // Handlers for the typing delay.
    protected Handler typingDelayHandler = new Handler();
    protected Runnable typingDelayRunnable = new Runnable() {
        @Override
        public void run() {
            // Time to search.
            if (searchTask != null) searchTask.cancel(true);
            searchTask = new SearchArtistsTask(self);
            String searchText = txtArtistSearch.getText().toString();
            if (!searchText.trim().equals(""))
                searchTask.execute(searchText.trim());
            else {
                setArtistResults(new ArrayList<Artist>());
            }
        }
    };

    public void setArtistSearchResults(ArtistsPager results) {
        setArtistSearchResults(results, false);
    }

    public void setArtistSearchResults(ArtistsPager results, boolean forceSet) {
        if (forceSet || results.artists.total > 0) {
            setArtistResults(results.artists.items);
        }
        else {
            if (quickNotice != null) quickNotice.cancel();

            quickNotice = Toast.makeText(ctx, R.string.msgNoResults, Toast.LENGTH_SHORT);
            quickNotice.show();
        }
    }

    private void setArtistResults(List<Artist> artists) {
        ArtistResultAdapter resultAdapter = new ArtistResultAdapter(ctx, artists);
        artistResults.setAdapter(resultAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        artistResults = (ListView)findViewById(R.id.listArtistResults);
        InitializeListeners();
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

    private void InitializeListeners() {
        txtArtistSearch = (EditText)findViewById(R.id.txtSearchArtist);

        txtArtistSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Set a short typing delay to prevent spamming if the user is typing fast.

                // Clear any existing delays
                typingDelayHandler.removeCallbacks(typingDelayRunnable);


                // Send a new delayed search request.
                typingDelayHandler.postDelayed(typingDelayRunnable, getResources().getInteger(R.integer.searchTypingDelay));
            }
        });
    }
}
