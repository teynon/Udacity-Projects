package com.tomsfreelance.spotifystreamer.Service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.MediaController;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tomsfreelance.spotifystreamer.Enums.PlaybackBroadcastAction;
import com.tomsfreelance.spotifystreamer.MainActivity;
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
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, MediaController.MediaPlayerControl {

    public MediaPlayer player;
    private LocalBroadcastManager broadcaster;
    private Timer seekTimer = new Timer();

    private PlaybackTrack CurrentTrack;
    public int CurrentTrackIndex;
    public ArrayList<PlaybackTrack> TrackList;
    private boolean Completed = false;
    public BroadcastReceiver notificationReceiver;

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
                    pausePlaying();
                    break;
                case PLAYBACK_RESUME:
                    player.start();
                    UpdateSeekPosition(SeekPosition);
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

        UpdateNotification();
    }

    private void UpdateNotification() {
        Picasso.with(this).load(CurrentTrack.AlbumImageSmall).into(new Target() {
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Context ctx = getApplicationContext();

                Intent prevIntent = new Intent(getString(R.string.actionPrev));
                prevIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                prevIntent.putExtra(getString(R.string.intentMsgPlaybackPrev), true);

                Intent nextIntent = new Intent(getString(R.string.actionNext));
                nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                nextIntent.putExtra(getString(R.string.intentMsgPlaybackNext), true);

                Intent playIntent = new Intent(getString((player.isPlaying()) ? R.string.actionPause : R.string.actionPlay));
                playIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                playIntent.putExtra(getString(R.string.intentMsgPlaybackPlay), true);

                Intent playerIntent = new Intent(getApplicationContext(), MainActivity.class);
                playerIntent.setAction(Intent.ACTION_MAIN);
                playerIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                playerIntent.putExtra(getString(R.string.intentMsgPlaybackAction), PlaybackBroadcastAction.PLAYBACK_UPDATE);
                playerIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                //playerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(playerIntent);
                stackBuilder.addNextIntent(prevIntent);
                stackBuilder.addNextIntent(nextIntent);
                stackBuilder.addNextIntent(playIntent);

                PendingIntent nextPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, nextIntent, 0);
                PendingIntent prevPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, prevIntent, 0);
                PendingIntent playPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, playIntent, 0);

                PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, playerIntent, 0);

                //PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                int playPauseDrawable = (player.isPlaying()) ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_circle_outline_white_24dp;
                String actionPlayPause = (player.isPlaying()) ? getString(R.string.actionPause) : getString(R.string.actionPlay);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.ic_headset_white_24dp)
                        .addAction(R.drawable.ic_skip_previous_white_24dp, getString(R.string.actionPrev), prevPendingIntent)
                        .addAction(playPauseDrawable, actionPlayPause, playPendingIntent)
                        .addAction(R.drawable.ic_skip_next_white_24dp, getString(R.string.actionNext), nextPendingIntent)
                        .setContentTitle(CurrentTrack.SongName)
                        .setContentText(CurrentTrack.Artist)
                        .setVisibility(Notification.VISIBILITY_PUBLIC);

                notificationBuilder.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(getResources().getInteger(R.integer.notificationStreamID), notificationBuilder.build());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
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

        RegisterNotificationReceiver();

        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    private void RegisterNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.actionNext));
        filter.addAction(getString(R.string.actionPrev));
        filter.addAction(getString(R.string.actionPlay));
        filter.addAction(getString(R.string.actionPause));

        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action == getString(R.string.actionNext)) {
                    nextTrack();
                }
                else if (action == getString(R.string.actionPrev)) {
                    prevTrack();
                }
                else if (action == getString(R.string.actionPlay)) {
                    startPlaying();
                }
                else if (action == getString(R.string.actionPause)) {
                    pausePlaying();
                }

                UpdateNotification();
            }
        };

        registerReceiver(notificationReceiver, filter);
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

        unregisterReceiver(notificationReceiver);
        dismissNotification();
    }

    private void dismissNotification() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(getResources().getInteger(R.integer.notificationStreamID));
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

        nextTrack();
    }

    private void nextTrack() {
        if (CurrentTrackIndex + 1 < TrackList.size()) {
            CurrentTrack = TrackList.get(++CurrentTrackIndex);
            startTrack();
        }
    }

    private void prevTrack() {
        if (CurrentTrackIndex - 1 >= 0) {
            CurrentTrack = TrackList.get(--CurrentTrackIndex);
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

    private void pausePlaying() {
        if (player.isPlaying()) {
            player.pause();
        }

        stopSeekMonitor();
        SendBroadcast(PlaybackBroadcastAction.PLAYBACK_STOPPED);
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

        handleNotification.obtainMessage(1).sendToTarget();
    }

    private Handler handleNotification = new Handler() {
        public void handleMessage(Message msg) {
            UpdateNotification();
        }
    };

    private void startSeekMonitor() {
        // We can update the seek less often and fake the funk in the UI application.
        seekTimer.scheduleAtFixedRate(new SeekMonitor(), 0, 5000);
    }

    private void stopSeekMonitor() {
        seekTimer.cancel();
        seekTimer = new Timer();
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return player.getAudioSessionId();
    }

    private class SeekMonitor extends TimerTask {
        public void run() {
            if (player.isPlaying())
                SendBroadcast(PlaybackBroadcastAction.PLAYBACK_UPDATE);
        }
    }


    private final IBinder playbackBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }
}
