package com.tomsfreelance.spotifystreamer;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
public class PlaybackFragment extends DialogFragment {

    private PlaybackTrack CurrentTrack;
    private ArrayList<PlaybackTrack> TrackList;
    private View DialogView;
    private TextView txtArtist;
    private TextView txtAlbum;
    private TextView txtSong;
    private ImageView imgAlbum;
    private SeekBar seekBar;
    private TextView currentTime;
    private TextView remainingTime;
    private long trackTimeRemaining;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the track details.
        CurrentTrack = getArguments().getParcelable(getString(R.string.intentMsgTrack));
        TrackList = getArguments().getParcelableArrayList(getString(R.string.intentMsgTrackList));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DialogView = inflater.inflate(R.layout.track_playback, container, false);

        Initialize();

        return DialogView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(getResources().getInteger(R.integer.playbackWidth), getResources().getInteger(R.integer.playbackHeight));
        return dialog;
    }

    private void Initialize() {
        Intent intent = getActivity().getIntent();
        FragmentActivity activity = getActivity();

        setRetainInstance(true);

        txtArtist = (TextView)DialogView.findViewById(R.id.txtPlaybackArtistName);
        txtAlbum = (TextView)DialogView.findViewById(R.id.txtPlaybackAlbumName);
        txtSong = (TextView)DialogView.findViewById(R.id.playbackSongName);
        imgAlbum = (ImageView)DialogView.findViewById(R.id.imgPlaybackAlbum);
        seekBar = (SeekBar)DialogView.findViewById(R.id.playbackSeekbar);
        currentTime = (TextView)DialogView.findViewById(R.id.txtSeekbarCurrent);
        remainingTime = (TextView)DialogView.findViewById(R.id.txtSeekbarRemaining);

        txtArtist.setText(CurrentTrack.Artist);
        txtAlbum.setText(CurrentTrack.AlbumName);
        txtSong.setText(CurrentTrack.SongName);

        trackTimeRemaining = CurrentTrack.TrackLength;
        Picasso.with(getActivity()).load(CurrentTrack.AlbumImage).into(imgAlbum);

        UpdateSeekTimeRemaining();


    }

    private void UpdateSeekTimeRemaining() {
        int minutes = (int)(trackTimeRemaining / 1000 / 60);
        int seconds = (int)((trackTimeRemaining / 1000) % 60);
        remainingTime.setText(Integer.toString(minutes) + ":" + String.format("%02d", seconds));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("CurrentTrack", CurrentTrack);
        outState.putParcelableArrayList("TrackList", TrackList);
    }

    @Override
     public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}
