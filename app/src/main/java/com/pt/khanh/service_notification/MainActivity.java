package com.pt.khanh.service_notification;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static ImageButton sButtonPlay;
    public static ImageButton sButtonPrev;
    public static ImageButton sButtonNext;
    private RecyclerView mRecyclerView;
    private TextView mTimeProgress;
    private TextView mTimeMax;
    private SeekBar mSeekBar;

    private MyService mService;
    private boolean mBindered = false;
    private ServiceConnection mConnection;
    private List<Song> mSongs = new ArrayList<>();
    private SongAdapter mAdapter;
    private int mCurrentIdSong;
    private int mCurrentDuration;

    private static final String TIME_FORMAT = "mm:ss";
    private static final String SONG_NAME_0 = "havana";
    private static final String SONG_NAME_1 = "you owe me";
    private static final String SONG_NAME_2 = "the middle";
    private static final int TIME_DELAY = 100; //miliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initDataRecyclerView();
        addListener();
        serviceConnection();
    }

    private void initView() {
        sButtonNext = findViewById(R.id.button_next);
        sButtonPlay = findViewById(R.id.button_play);
        sButtonPrev = findViewById(R.id.button_prev);
        mRecyclerView = findViewById(R.id.recycleview_mp3);
        mTimeMax = findViewById(R.id.text_time_max);
        mTimeProgress = findViewById(R.id.text_time_progress);
        mSeekBar = findViewById(R.id.seekbar_song);
        mTimeProgress.setText(getString(R.string.string_null));
        mTimeMax.setText(getString(R.string.string_null));
    }

    public void initDataRecyclerView() {
        mSongs = new ArrayList<>();
        mSongs.add(new Song(R.raw.the_middle, SONG_NAME_2));
        mSongs.add(new Song(R.raw.you_owe_me, SONG_NAME_1));
        mSongs.add(new Song(R.raw.havana, SONG_NAME_0));

        mAdapter = new SongAdapter(mSongs);
        RecyclerView.LayoutManager linearLayout = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setLayoutManager(linearLayout);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void addListener() {
        sButtonPlay.setOnClickListener(this);
        sButtonPrev.setOnClickListener(this);
        sButtonNext.setOnClickListener(this);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(MainActivity.this,
                mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(View view, int position) {
                mCurrentIdSong = position;
                mService.play(position);
                sButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                updateTime();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mCurrentDuration = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mService.seek(mCurrentDuration);
                sButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                updateTime();
            }
        });
    }

    private void serviceConnection() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MyService.SongBinder binder = (MyService.SongBinder) iBinder;
                mService = binder.getService();
                mBindered = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBindered = false;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intentService = new Intent(MainActivity.this, MyService.class);
        startService(intentService);
        bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
        if (MyService.sMedia.isPlaying()) {
            updateTime();
            sButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            sButtonPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {
        if (mBindered) {
            switch (view.getId()) {
                case R.id.button_play:
                    mService.play();
                    break;
                case R.id.button_next:
                    mService.playNext();
                    break;
                case R.id.button_prev:
                    mService.playPrevious();
                    break;
            }
        }
    }

    public void updateTime() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
                mTimeProgress.setText(sdf.format(mService.getCurrentPositionSong()));
                mSeekBar.setProgress(mService.getCurrentPositionSong());
                mSeekBar.setMax(mService.getDurationSong());
                mTimeMax.setText(sdf.format(mService.getDurationSong()));
                handler.postDelayed(this, TIME_DELAY);
            }
        }, TIME_DELAY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBindered) {
            unbindService(mConnection);
            mBindered = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
