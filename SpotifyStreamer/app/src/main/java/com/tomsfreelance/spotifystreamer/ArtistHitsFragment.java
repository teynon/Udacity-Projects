package com.tomsfreelance.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tomsfreelance.spotifystreamer.Adapters.TrackResultAdapter;
import com.tomsfreelance.spotifystreamer.Tasks.TopTracksForArtistTask;
import com.tomsfreelance.spotifystreamer.Model.PlaybackArtist;
import com.tomsfreelance.spotifystreamer.Model.PlaybackTrack;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArtistHitsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArtistHitsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistHitsFragment extends Fragment {

    private Context ctx = null;
    private PlaybackArtist Artist = null;
    private ListView trackResults = null;

    private OnSelectTrackListener mListener;

    public ArtistHitsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.artist_hits, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Initialize();

        if (savedInstanceState != null) {
            ArrayList<PlaybackTrack> tracks = savedInstanceState.getParcelableArrayList("TrackList");
            setArtistTracks(tracks);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ctx = activity;
            mListener = (OnSelectTrackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (trackResults != null) {
            TrackResultAdapter adapter = (TrackResultAdapter) trackResults.getAdapter();
            if (adapter != null)
                outState.putParcelableArrayList("TrackList", adapter.GetTracks());
        }
    }



    private void Initialize() {
        if (getArguments() != null) {
            Artist = getArguments().getParcelable(getString(R.string.intentMsgArtist));

            GetArtistTopTracks();
        }

        trackResults = (ListView) getView().findViewById(R.id.listTracks);

        InitializeListeners();
    }

    private void InitializeListeners() {
        trackResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                PlaybackFragment playbackFragment = new PlaybackFragment();

                TrackResultAdapter adapter = (TrackResultAdapter) trackResults.getAdapter();
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.intentMsgTrack), adapter.getItem(position));
                args.putParcelableArrayList(getString(R.string.intentMsgTrackList), adapter.GetTracks());
                playbackFragment.setArguments(args);

                if (MainActivity.TwoPane) {
                    playbackFragment.show(fragmentManager, getString(R.string.playbackTag));
                }
                else {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();

                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    transaction.add(android.R.id.content, playbackFragment).addToBackStack(null).commit();
                }
            }
        });
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

    public void UpdateArtist(PlaybackArtist artist) {
        Artist = artist;
        GetArtistTopTracks();
    }

    public void GetArtistTopTracks() {
        // Get the tracks.
        TopTracksForArtistTask trackTask = new TopTracksForArtistTask(this, getString(R.string.topTracksCountryCode));
        trackTask.execute(Artist.ArtistID);
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
    public interface OnSelectTrackListener {
        public void OnTrackSelected(PlaybackTrack track, ArrayList<PlaybackTrack> trackList);
    }

}
