package com.tomsfreelance.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tomsfreelance.spotifystreamer.Adapters.ArtistResultAdapter;
import com.tomsfreelance.spotifystreamer.Model.PlaybackArtist;
import com.tomsfreelance.spotifystreamer.Model.PlaybackTrack;
import com.tomsfreelance.spotifystreamer.Tasks.SearchArtistsTask;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArtistSearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArtistSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistSearchFragment extends Fragment {
    private EditText txtArtistSearch = null;
    private Context ctx = null;
    private Toast quickNotice = null;
    private ListView artistResults = null;
    private SearchArtistsTask searchTask = null;
    private ArtistSearchFragment self = this;

    private OnSelectArtistListener mListener;

    public static ArtistSearchFragment newInstance() {
        ArtistSearchFragment fragment = new ArtistSearchFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public ArtistSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.artist_search, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ctx = activity;
            mListener = (OnSelectArtistListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        InitializeListeners();

        if (savedInstanceState != null) {
            ArrayList<PlaybackArtist> artists = savedInstanceState.getParcelableArrayList(getString(R.string.intentMsgArtistList));
            setArtistResults(artists);

            typingDelayHandler.removeCallbacks(typingDelayRunnable);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (artistResults != null) {
            ArtistResultAdapter adapter = (ArtistResultAdapter) artistResults.getAdapter();
            if (adapter != null)
                outState.putParcelableArrayList(getString(R.string.intentMsgArtistList), adapter.GetArtists());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



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
                setArtistResults(new ArrayList<PlaybackArtist>());
            }
        }
    };

    public void setArtistSearchResults(ArtistsPager results) {
        ArrayList<PlaybackArtist> artists = new ArrayList<PlaybackArtist>();

        for (Artist t : results.artists.items) {
            artists.add(new PlaybackArtist(t));
        }

        setArtistSearchResults(artists, false);
    }

    public void setArtistSearchResults(ArrayList<PlaybackArtist> results, boolean forceSet) {

        if (forceSet || results.size() > 0) {
            setArtistResults(results);
        }
        else {
            setArtistResults(new ArrayList<PlaybackArtist>());

            if (quickNotice != null) quickNotice.cancel();

            quickNotice = Toast.makeText(ctx, R.string.msgNoResults, Toast.LENGTH_SHORT);
            quickNotice.show();
        }
    }

    private void setArtistResults(ArrayList<PlaybackArtist> artists) {
        ArtistResultAdapter resultAdapter = new ArtistResultAdapter(ctx, artists);
        artistResults.setAdapter(resultAdapter);
    }

    private void InitializeListeners() {
        artistResults = (ListView)getView().findViewById(R.id.listArtistResults);
        txtArtistSearch = (EditText)getView().findViewById(R.id.txtSearchArtist);

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

        artistResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistResultAdapter adapter = (ArtistResultAdapter) artistResults.getAdapter();

                PlaybackArtist artist = adapter.getItem(position);

                mListener.onArtistSelected(artist);
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSelectArtistListener {
        public void onArtistSelected(PlaybackArtist artist);
    }

}
