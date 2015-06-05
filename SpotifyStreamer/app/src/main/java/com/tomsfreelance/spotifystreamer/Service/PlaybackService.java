package com.tomsfreelance.spotifystreamer.Service;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.tomsfreelance.spotifystreamer.Enums.PlaybackBroadcastAction;
import com.tomsfreelance.spotifystreamer.R;
import com.tomsfreelance.spotifystreamer.Model.PlaybackTrack;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by teynon on 6/3/2015.
 */
public class PlaybackService extends Service implements
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

    private MediaPlayer player;
    private LocalBroadcastManager broadcaster;
    private Timer seekTimer = new Timer();

    private int CurrentTrackIndex;
    private PlaybackTrack CurrentTrack;
    private ArrayList<PlaybackTrack> TrackList;
    private boolean Completed = false;

    public PlaybackService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            int SeekPosition = intent.getIntExtra(getString(R.string.intentMsgSeekProgress), 0);
            CurrentTrackIndex = intent.getIntExtra(getString(R.string.intentMsgTrack), 0);
            TrackList = intent.getParcelableArrayListExtra(getString(R.string.intentMsgTrackList));
            CurrentTrack = (CurrentTrackIndex >= 0 && TrackList.size() > CurrentTrackIndex) ? TrackList.get(CurrentTrackIndex) : TrackList.get(0);
            PlaybackBroadcastAction action = (PlaybackBroadcastAction)intent.getSerializableExtra(getString(R.string.intentMsgPlaybackAction));

            switch (action) {
                case PLAYBACK_START:
                    startTrack();
                    break;
                case PLAYBACK_PAUSE:
                    stopPlaying();
                    break;
                case PLAYBACK_STARTED:
                    startPlaying();
                    break;
                case PLAYBACK_UPDATE:
                    // Update seek position.
                    //stopPlaying();
                    stopSeekMonitor();
                    UpdateSeekPosition(SeekPosition);
                    break;
                default:
                    break;
            }
        }

        return START_STICKY;
    }

    private void startTrack() {
        player.reset();
        if (!player.isPlaying()) {
            try {
                player.setDataSource(CurrentTrack.StreamURL);
                player.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*@Override
    protected void onHandleIntent(Intent intent) {

    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnPreparedListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnInfoListener(this);
        player.reset();

        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    private void UpdateSeekPosition(int position) {
        player.seekTo(position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }

            player.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playbackBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Completed = true;
        stopPlaying();

        if (CurrentTrackIndex + 1 < TrackList.size()) {
            CurrentTrack = TrackList.get(++CurrentTrackIndex);
            startTrack();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        startPlaying();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        startPlaying();
    }

    private void startPlaying() {
        startPlaying(player.getCurrentPosition());
    }

    private void startPlaying(int position) {
        if (!player.isPlaying()) {
            player.start();
        }

        startSeekMonitor();
        SendBroadcast(PlaybackBroadcastAction.PLAYBACK_STARTED, position);
    }

    private void stopPlaying() {
        if (player.isPlaying()) {
            player.stop();
        }

        stopSeekMonitor();
        SendBroadcast(PlaybackBroadcastAction.PLAYBACK_STOPPED);
    }

    private void SendBroadcast(PlaybackBroadcastAction action) {
        SendBroadcast(action, player.getCurrentPosition());
    }
    private void SendBroadcast(PlaybackBroadcastAction action, int position) {

        Intent seekUpdateIntent = new Intent(getString(R.string.intentMsgSeek));

        // Because we should all seek progress... Yey for bad puns.
        seekUpdateIntent.putExtra(getString(R.string.intentMsgSeekProgress),
                (Completed) ? getResources().getInteger(R.integer.playbackLength) : position);
        seekUpdateIntent.putExtra(getString(R.string.intentMsgTrack), CurrentTrackIndex);
        seekUpdateIntent.putExtra(getString(R.string.intentMsgPlaybackAction), action);

        if (Completed || position == 30000) {
            Completed = false;
        }

        broadcaster.sendBroadcast(seekUpdateIntent);
        Completed = false;
    }

    private void startSeekMonitor() {
        // We can update the seek less often and fake the funk in the UI application.
        seekTimer.scheduleAtFixedRate(new SeekMonitor(), 0, 5000);
    }

    private void stopSeekMonitor() {
        seekTimer.cancel();
        seekTimer = new Timer();
    }

    private class SeekMonitor extends TimerTask {
        public void run() {
            if (player.isPlaying())
                SendBroadcast(PlaybackBroadcastAction.PLAYBACK_UPDATE);
        }
    }


    private final IBinder playbackBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }
}
