package com.tomsfreelance.spotifystreamer.Service;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.tomsfreelance.spotifystreamer.R;
import com.tomsfreelance.spotifystreamer.Model.PlaybackTrack;

import java.util.ArrayList;

/**
 * Created by teynon on 6/3/2015.
 */
public class PlaybackService extends IntentService implements
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

    private MediaPlayer player;

    private PlaybackTrack CurrentTrack;
    private ArrayList<PlaybackTrack> TrackList;

    public PlaybackService() {
        super("PlaybackService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        CurrentTrack = intent.getParcelableExtra(getString(R.string.intentMsgTrack));
        TrackList = intent.getParcelableArrayListExtra(getString(R.string.intentMsgTrackList));

        player.reset();
        if (!player.isPlaying()) {
            try {
                player.setDataSource(CurrentTrack.StreamURL);
                player.prepareAsync();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

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
        return super.onBind(intent);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        StopPlaying();
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
        StartPlaying();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    private void StartPlaying() {
        if (!player.isPlaying()) {
            player.start();
        }
    }

    private void StopPlaying() {
        if (player.isPlaying()) {
            player.stop();
        }
    }
}
