package com.pt.khanh.service_notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.ImageButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    public static MediaPlayer sMedia = new MediaPlayer();
    public List<Song> mSongs = new ArrayList<>();
    private final IBinder mBinder = new SongBinder();
    private int mCurrentPosition;

    private static final int ONE = 1;
    private static final int ZERO = 0;
    private static final int REQUEST_CODE = 0;
    private static final String SONG_NAME_0 = "havana";
    private static final String SONG_NAME_1 = "you owe me";
    private static final String SONG_NAME_2 = "the middle";
    private static final String ACTION_PREV = "prev";
    private static final String ACTION_NEXT = "next";
    private static final String ACTION_PLAY = "play";

    private WeakReference<ImageButton> mButtonPlay;

    public MyService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class SongBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSongs.add(new Song(R.raw.the_middle, SONG_NAME_2));
        mSongs.add(new Song(R.raw.you_owe_me, SONG_NAME_1));
        mSongs.add(new Song(R.raw.havana, SONG_NAME_0));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        mButtonPlay = new WeakReference<>(MainActivity.sButtonPlay);
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void handleIntent(Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (action == null) return;
        switch (action) {
            case ACTION_NEXT:
                playNext();
                break;
            case ACTION_PLAY:
                play();
                break;
            case ACTION_PREV:
                playPrevious();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void createNotificationPlay(int position) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(),
                (int) System.currentTimeMillis(), intent, REQUEST_CODE);

        Intent prevIntent = new Intent(this, MyService.class);
        prevIntent.setAction(ACTION_PREV);
        PendingIntent pPrevIntent = PendingIntent.getService(this, REQUEST_CODE, prevIntent, REQUEST_CODE);

        Intent nextIntent = new Intent(this, MyService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pNextIntent = PendingIntent.getService(this, REQUEST_CODE, nextIntent, REQUEST_CODE);

        Intent playIntent = new Intent(this, MyService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent pPlayIntent = PendingIntent.getService(this, REQUEST_CODE, playIntent, REQUEST_CODE);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(mSongs.get(position).getName())
                .setSmallIcon(R.drawable.ic_music_note_black_24dp)
                .setContentIntent(pIntent)
                .addAction(R.drawable.ic_skip_previous_black_24dp, getString(R.string.string_null), pPrevIntent)
                .addAction(R.drawable.ic_pause_black_24dp, getString(R.string.string_null), pPlayIntent)
                .addAction(R.drawable.ic_skip_next_black_24dp, getString(R.string.string_null), pNextIntent).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(REQUEST_CODE, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void createNotificationPause(int position) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(),
                (int) System.currentTimeMillis(), intent, REQUEST_CODE);

        Intent prevIntent = new Intent(this, MyService.class);
        prevIntent.setAction(ACTION_PREV);
        PendingIntent pPrevIntent = PendingIntent.getService(this, REQUEST_CODE, prevIntent, REQUEST_CODE);

        Intent nextIntent = new Intent(this, MyService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pNextIntent = PendingIntent.getService(this, REQUEST_CODE, nextIntent, REQUEST_CODE);

        Intent playIntent = new Intent(this, MyService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent pPlayIntent = PendingIntent.getService(this, REQUEST_CODE, playIntent, REQUEST_CODE);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(mSongs.get(position).getName())
                .setSmallIcon(R.drawable.ic_music_note_black_24dp)
                .setContentIntent(pIntent)
                .addAction(R.drawable.ic_skip_previous_black_24dp, getString(R.string.string_null), pPrevIntent)
                .addAction(R.drawable.ic_play_arrow_black_24dp, getString(R.string.string_null), pPlayIntent)
                .addAction(R.drawable.ic_skip_next_black_24dp, getString(R.string.string_null), pNextIntent).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(REQUEST_CODE, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void play() {
        if (sMedia.isPlaying()) {
            sMedia.pause();
            mButtonPlay.get().setImageResource(R.drawable.ic_play_arrow_black_24dp);
            createNotificationPause(mCurrentPosition);
        } else {
            sMedia.start();
            mButtonPlay.get().setImageResource(R.drawable.ic_pause_black_24dp);
            createNotificationPlay(mCurrentPosition);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void play(int position) {
        mCurrentPosition = position;
        createNotificationPlay(position);
        sMedia.reset();
        if (sMedia.isPlaying()) {
            sMedia.stop();
        }
        sMedia = MediaPlayer.create(MyService.this, mSongs.get(position).getId());
        sMedia.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void playNext() {
        mButtonPlay.get().setImageResource(R.drawable.ic_pause_black_24dp);
        if (mCurrentPosition != (mSongs.size() - ONE)) {
            mCurrentPosition++;
        } else {
            mCurrentPosition = ZERO;
        }
        play(mCurrentPosition);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void playPrevious() {
        mButtonPlay.get().setImageResource(R.drawable.ic_pause_black_24dp);
        if (mCurrentPosition != ZERO) {
            mCurrentPosition--;
        } else {
            mCurrentPosition = mSongs.size() - ONE;
        }
        play(mCurrentPosition);
    }

    public void seek(int currentPosition) {
        sMedia.seekTo(currentPosition);
        sMedia.start();
    }

    public int getCurrentPositionSong() {
        return sMedia.getCurrentPosition();
    }

    public int getDurationSong() {
        return sMedia.getDuration();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sMedia.release();
    }
}
