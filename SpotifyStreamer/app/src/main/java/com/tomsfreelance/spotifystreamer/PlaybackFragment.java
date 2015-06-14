package com.tomsfreelance.spotifystreamer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tomsfreelance.spotifystreamer.Enums.PlaybackBroadcastAction;
import com.tomsfreelance.spotifystreamer.Model.PlaybackTrack;
import com.tomsfreelance.spotifystreamer.Service.PlaybackService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by teynon on 5/31/2015.
 */
public class PlaybackFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    private int CurrentTrackIndex;
    private PlaybackTrack CurrentTrack;
    private ArrayList<PlaybackTrack> TrackList;
    private View DialogView;
    private TextView txtArtist;
    private TextView txtAlbum;
    private TextView txtSong;
    private ImageView btnPlay;
    private ImageView btnNext;
    private ImageView btnLast;
    private ImageView imgAlbum;
    private SeekBar seekBar;
    private TextView CurrentTime;
    private TextView RemainingTime;
    private long TrackTimeRemaining;
    private static int TrackLength = 30000;
    private BroadcastReceiver playbackMonitor;
    private Timer progressBarUpdater = new Timer();
    private int SeekPosition;
    private boolean IsPlaying = false;
    private boolean IsInitialized = false;
    private PlaybackService playbackService;
    private MediaController playbackController;
    private boolean AutoStart = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the track details.
        CurrentTrackIndex = getArguments().getInt(getString(R.string.intentMsgTrack));
        TrackList = getArguments().getParcelableArrayList(getString(R.string.intentMsgTrackList));
        PlaybackBroadcastAction action = (PlaybackBroadcastAction)getArguments().getSerializable(getString(R.string.intentMsgPlaybackAction));
        if (action == PlaybackBroadcastAction.PLAYBACK_UPDATE) {
            AutoStart = false;
        }

        if (CurrentTrackIndex >= 0)
            CurrentTrack = TrackList.get(CurrentTrackIndex);

        SeekPosition = getArguments().getInt(getString(R.string.intentMsgSeekProgress));

        TrackLength = getResources().getInteger(R.integer.playbackLength);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DialogView = inflater.inflate(R.layout.track_playback, container, false);

        IsPlaying = false;

        InitializeView();

        if (!IsInitialized) {
            Initialize();
            if (AutoStart) StartPlayback();
            IsInitialized = true;
        }
        InitializeListeners();
        updateView();

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
        setRetainInstance(true);

        subscribePlayback();
        if (SeekPosition == 0) {
            UpdateSeekPosition(0);
        }
    }

    private void InitializeView() {
        txtArtist = (TextView)DialogView.findViewById(R.id.txtPlaybackArtistName);
        txtAlbum = (TextView)DialogView.findViewById(R.id.txtPlaybackAlbumName);
        txtSong = (TextView)DialogView.findViewById(R.id.playbackSongName);
        btnPlay = (ImageView)DialogView.findViewById(R.id.btnPlay);
        btnLast = (ImageView)DialogView.findViewById(R.id.btnPrev);
        btnNext = (ImageView)DialogView.findViewById(R.id.btnNext);
        imgAlbum = (ImageView)DialogView.findViewById(R.id.imgPlaybackAlbum);
        seekBar = (SeekBar)DialogView.findViewById(R.id.playbackSeekbar);
        CurrentTime = (TextView)DialogView.findViewById(R.id.txtSeekbarCurrent);
        RemainingTime = (TextView)DialogView.findViewById(R.id.txtSeekbarRemaining);
    }

    private void updateView() {
        if (CurrentTrack != null) {
            txtArtist.setText(CurrentTrack.Artist);
            txtAlbum.setText(CurrentTrack.AlbumName);
            txtSong.setText(CurrentTrack.SongName);

            TrackTimeRemaining = TrackLength; // Each track is 30 seconds. //CurrentTrack.TrackLength;
            imgAlbum.setMinimumHeight(imgAlbum.getHeight());
            imgAlbum.setMinimumWidth(imgAlbum.getWidth());
            Picasso.with(getActivity()).load(CurrentTrack.AlbumImage).into(imgAlbum);
        }
    }

    private void sendMessage(PlaybackBroadcastAction action) {
        Intent intent = new Intent(getActivity(), PlaybackService.class);
        intent.putExtra(getString(R.string.intentMsgSeekProgress), SeekPosition);
        intent.putExtra(getString(R.string.intentMsgTrack), CurrentTrackIndex);
        intent.putExtra(getString(R.string.intentMsgTrackList), TrackList);
        intent.putExtra(getString(R.string.intentMsgPlaybackAction), action);
        getActivity().startService(intent);
    }

    private void InitializeListeners() {
        seekBar.setOnSeekBarChangeListener(this);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsPlaying) {
                    sendMessage(PlaybackBroadcastAction.PLAYBACK_PAUSE);
                    setPaused();
                }
                else {
                    sendMessage(PlaybackBroadcastAction.PLAYBACK_RESUME);
                    setPlaying();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentTrackIndex < TrackList.size() - 1)
                {
                    CurrentTrackIndex++;
                    stopProgressBarSimulator();
                    sendMessage(PlaybackBroadcastAction.PLAYBACK_START);
                }
            }
        });

        btnLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentTrackIndex > 0) {
                    stopProgressBarSimulator();
                    CurrentTrackIndex--;
                    sendMessage(PlaybackBroadcastAction.PLAYBACK_START);
                }
            }
        });
    }

    private void StartPlayback() {
        sendMessage(PlaybackBroadcastAction.PLAYBACK_START);
    }

    private void UpdateSeekPosition(int position) {
        int seekBarPosition = (int)(((double)position / (double)TrackLength) * 100); // CurrentTrack.TrackLength
        seekBar.setProgress(seekBarPosition);
        TrackTimeRemaining = TrackLength - SeekPosition; // Each track is 30 seconds. //CurrentTrack.TrackLength - SeekPosition;
        UpdateSeekTimeRemaining();
        UpdateCurrentTime();
    }

    private void startProgressBarSimulator() {
        progressBarUpdater.scheduleAtFixedRate(new ProgressSimulator(), 0, 100);
    }

    private void stopProgressBarSimulator() {
        progressBarUpdater.cancel();
        progressBarUpdater = new Timer();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Stop jerking the user around man
        stopProgressBarSimulator();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Determine the seek position based on the location of the seekbar.
        SeekPosition = (int)(((double)seekBar.getProgress() / 100) * TrackLength);
        sendMessage(PlaybackBroadcastAction.PLAYBACK_UPDATE);
        // Not sure how to monitor the "seekTo" on the media player so that I know
        // when the player is ready and playing... So I'm backtracking 400 milliseconds.
        SeekPosition -= 400;
        UpdateSeekPosition(SeekPosition);
        stopProgressBarSimulator();
        startProgressBarSimulator();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), PlaybackService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (playbackService != null) {
            getActivity().unbindService(serviceConnection);
            playbackService = null;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder)service;
            playbackService = binder.getService();

            connectMediaHandler.obtainMessage(1).sendToTarget();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playbackService = null;
        }
    };

    private static class PlaybackMediaHandler extends Handler {
        private final WeakReference<PlaybackFragment> Activity;

        public PlaybackMediaHandler(PlaybackFragment activity) {
            Activity = new WeakReference<PlaybackFragment>(activity);
        }

        public void handleMessage(Message msg) {
            PlaybackFragment activity = Activity.get();
            if (activity.isAdded()) {
                activity.TrackList = activity.playbackService.TrackList;
                activity.CurrentTrackIndex = activity.playbackService.CurrentTrackIndex;
                if (activity.TrackList != null && activity.CurrentTrackIndex >= 0 && activity.CurrentTrackIndex < activity.TrackList.size())
                    activity.CurrentTrack = activity.TrackList.get(activity.CurrentTrackIndex);

                activity.SeekPosition = activity.playbackService.getCurrentPosition();
                activity.UpdateSeekPosition(activity.SeekPosition);

                if (activity.playbackService.isPlaying()) {
                    activity.stopProgressBarSimulator();
                    activity.startProgressBarSimulator();
                    activity.setPlaying();
                } else {
                    activity.setPaused();
                }

                activity.updateView();
                /*playbackController = new MediaController(getActivity());
                playbackController.setAnchorView(getView().findViewById(R.id.playbackMediaController));
                playbackController.setMediaPlayer(playbackService);
                playbackController.setEnabled(true);
                playbackController.show();
                playbackController.requestFocus();
                playbackController.setAnchorView(getView());*/
            }
        }


    }

    private final PlaybackMediaHandler connectMediaHandler = new PlaybackMediaHandler(this);

    private class ProgressSimulator extends TimerTask {
        @Override
        public void run() {
            // Increase the timer by slightly less than the seek timer to deter
            // backwards jumping when updated by the service.
            SeekPosition += 98;
            progressSimulatorHandler.obtainMessage(1).sendToTarget();
        }
    }

    private Handler progressSimulatorHandler = new Handler() {
        public void handleMessage(Message msg) {
            UpdateSeekPosition(SeekPosition);
        }
    };

    private void UpdateCurrentTime() {
        int minutes = (int)(SeekPosition / 1000 / 60);
        int seconds = (int)((SeekPosition / 1000) % 60);
        CurrentTime.setText(Integer.toString(minutes) + ":" + String.format("%02d", seconds));
    }

    private void UpdateSeekTimeRemaining() {
        int minutes = (int)(TrackTimeRemaining / 1000 / 60);
        int seconds = (int)((TrackTimeRemaining / 1000) % 60);
        RemainingTime.setText(Integer.toString(minutes) + ":" + String.format("%02d", seconds));
    }

    private void setPlaying() {
        // getDrawable(id, theme) requires higher min sdk
        btnPlay.setImageDrawable(getResources().getDrawable(R.mipmap.btnpause));
    }

    private void setPaused() {
        btnPlay.setImageDrawable(getResources().getDrawable(R.mipmap.btnplay));
    }

    private void subscribePlayback() {
        playbackMonitor = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getActivity() != null) {
                    SeekPosition = intent.getIntExtra(getString(R.string.intentMsgSeekProgress), 0);
                    CurrentTrackIndex = intent.getIntExtra(getString(R.string.intentMsgTrack), 0);
                    CurrentTrack = (CurrentTrackIndex >= 0 && TrackList.size() > CurrentTrackIndex) ? TrackList.get(CurrentTrackIndex) : TrackList.get(0);
                    PlaybackBroadcastAction action = (PlaybackBroadcastAction) intent.getSerializableExtra(getString(R.string.intentMsgPlaybackAction));

                    UpdateSeekPosition(SeekPosition);

                    switch (action) {
                        case PLAYBACK_STARTED:
                            stopProgressBarSimulator();
                            startProgressBarSimulator();
                            setPlaying();
                            updateView();
                            break;
                        case PLAYBACK_STOPPED:
                            IsPlaying = false;
                            stopProgressBarSimulator();
                            setPaused();
                            updateView();
                            break;
                        case PLAYBACK_UPDATE:
                            if (!IsPlaying) {
                                IsPlaying = true;
                                stopProgressBarSimulator();
                                startProgressBarSimulator();
                                updateView();
                            }
                        default:
                            break;
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(playbackMonitor,
                new IntentFilter(getString(R.string.intentMsgSeek)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(getString(R.string.intentMsgTrack), CurrentTrackIndex);
        outState.putParcelableArrayList(getString(R.string.intentMsgTrackList), TrackList);
        outState.putInt(getString(R.string.intentMsgSeekProgress), SeekPosition);
    }

    @Override
     public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(playbackMonitor);
        }

        super.onDestroyView();
    }
}
