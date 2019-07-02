package com.litesnap.open.player;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.litesnap.open.player.bean.Video;
import com.litesnap.open.player.listener.PlayerListenerAdapter;
import com.litesnap.open.player.manager.ThumableManager;
import com.litesnap.open.player.utils.ControlAnimation;
import com.litesnap.open.player.utils.ExoPlayerUtil;
import com.litesnap.open.player.utils.ScaleUtil;
import com.litesnap.open.player.utils.TimeToStringUril;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final String BEAN = "bean";
    public static final String CURRENT = "current";
    public static final int STATU_LOADING = 0;
    public static final int STATU_PAUSE = 1;
    public static final int STATU_PLAY = 2;

    private Uri mUri;
    private Handler mHandler;
    private SimpleExoPlayer mPlayer;
    private int mStatus;
    private ControlAnimation mAnimaiton;
    private boolean mIsSeekNow;
    private boolean mIsStopTask;
    private boolean mIsShowControl = true;
    private ExecutorService mExec;
    private Timer mTimer;
    private Video mBean;
    private long mDefaultCurrectPosition;

    private TextureView mTextureView;
    private SeekBar mSeekBar;
    private View mPlayerView;
    private View mPauseView;
    private View mReplayView;
    private TextView mCurrentView;
    private TextView mDurationView;
    private View mDivVideoScreenView;
    private View mDivControlView;
    private ProgressBar mProgressBar;
    private ImageView mMarkView;
    private Drawable mThumDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBean = (Video) getIntent().getSerializableExtra(BEAN);
        mDefaultCurrectPosition = mBean.getCurrent();

        initialView();
        initial();
        setViewListener();
        setPlayerListener();
        readyVideo();
        monitorBar();

        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onPreDraw() {
                ViewGroup.MarginLayoutParams videoScreenlLP = (ViewGroup.MarginLayoutParams) mDivVideoScreenView.getLayoutParams();
                videoScreenlLP.bottomMargin = mSeekBar.getHeight() / 2;
                mDivVideoScreenView.setLayoutParams(videoScreenlLP);
                getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    private void initialView(){
        mSeekBar = findViewById(R.id.seek_bar);
        mTextureView = findViewById(R.id.texture_view);
        mPlayerView = findViewById(R.id.play);
        mPauseView = findViewById(R.id.pause);
        mReplayView = findViewById(R.id.replay);
        mCurrentView = findViewById(R.id.current);
        mDurationView = findViewById(R.id.duration);
        mDivVideoScreenView = findViewById(R.id.div_video_screen);
        mProgressBar = findViewById(R.id.progress_bar);
        mDivControlView = findViewById(R.id.div_control);
        mMarkView = findViewById(R.id.mark);

        mDivControlView.setVisibility(View.GONE);
        mDivControlView.setAlpha(0f);
        mProgressBar.setVisibility(View.GONE);

        mThumDrawable = mSeekBar.getThumb();

        if (ThumableManager.getDrawable() != null){
            mMarkView.setImageDrawable(ThumableManager.getDrawable());
        }
        ViewCompat.setTransitionName(mMarkView, String.valueOf(mBean.getUUID()));
    }

    private void setViewListener(){
        mPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setPlayWhenReady(true);
                setPlayerStatu(STATU_PLAY);
            }
        });

        mPauseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setPlayWhenReady(false);
                setPlayerStatu(STATU_PAUSE);
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentView.setText(TimeToStringUril.stringForTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mThumDrawable.setAlpha(255);
                mIsSeekNow = true;
                mIsShowControl = false;
                mDivControlView.callOnClick();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDefaultCurrectPosition = seekBar.getProgress();
                mPlayer.seekTo(mDefaultCurrectPosition);
                mIsSeekNow = false;
            }
        });

        mDivControlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsShowControl = !mIsShowControl;
                mAnimaiton.startAnimation(mDivControlView, mThumDrawable, mIsShowControl);
            }
        });
    }

    private void initial(){
        mHandler = new Handler();
        mExec = Executors.newCachedThreadPool();
        mTimer = new Timer();
        mAnimaiton = new ControlAnimation();
        mPlayer = ExoPlayerUtil.newInstance(VideoActivity.this);
        mUri = Uri.parse(mBean.getUrl());

        mPlayer.setVideoTextureView(mTextureView);
    }

    private void readyVideo(){
        mExec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MediaSource source = ExoPlayerUtil.newMediaSource(VideoActivity.this, mUri);
                            mPlayer.prepare(source);
                            mIsShowControl = true;
                            mProgressBar.setVisibility(View.VISIBLE);
                            mDivControlView.setVisibility(View.VISIBLE);
                            mAnimaiton.startAnimation(mDivControlView, mThumDrawable, mIsShowControl);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void monitorBar(){
        mExec.execute(new Runnable() {
            @Override
            public void run() {
                while (!mIsStopTask){
                    try {
                        if (mIsSeekNow){
                            continue;
                        }

                        TimeUnit.MILLISECONDS.sleep(200);

                        if (mIsSeekNow){
                            continue;
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mSeekBar.setProgress((int) mPlayer.getCurrentPosition());
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mIsShowControl){
                    mIsShowControl = false;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAnimaiton.startAnimation(mDivControlView, mThumDrawable, mIsShowControl);
                        }
                    });
                }
            }
        }, 7000, 7000);
    }

    private void setPlayerListener(){
        mPlayer.addVideoListener(new SimpleExoPlayer.VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                float scale = ScaleUtil.matchingSize(mDivVideoScreenView.getWidth(), mDivVideoScreenView.getHeight(), width, height);
                ViewGroup.LayoutParams lp = mTextureView.getLayoutParams();
                lp.width = (int) (width * scale);
                lp.height = (int) (height * scale);
                mTextureView.setLayoutParams(lp);

                mSeekBar.setMax((int) mPlayer.getDuration());
                mCurrentView.setText(TimeToStringUril.stringForTime((int) mPlayer.getCurrentPosition()));
                mDurationView.setText(TimeToStringUril.stringForTime((int) mPlayer.getDuration()));

                mPlayer.seekTo(mDefaultCurrectPosition);
                mPlayer.setPlayWhenReady(true);
            }

            @Override
            public void onRenderedFirstFrame() {
            }
        });

        mPlayer.addListener(new PlayerListenerAdapter(){
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                boolean isLoading = playbackState == SimpleExoPlayer.DISCONTINUITY_REASON_SEEK_ADJUSTMENT;
                boolean isPause = !playWhenReady && playbackState == SimpleExoPlayer.DISCONTINUITY_REASON_INTERNAL;
                boolean isPlay = playWhenReady && playbackState == SimpleExoPlayer.DISCONTINUITY_REASON_INTERNAL;

                if (isLoading){
                    mIsShowControl = true;
                    mAnimaiton.startAnimation(mDivControlView, mThumDrawable, mIsShowControl);
                    setPlayerStatu(STATU_LOADING);
                }

                if (isPause){
                    setPlayerStatu(STATU_PAUSE);
                }else if (isPlay){
                    if (playWhenReady){
                        mMarkView.setVisibility(View.GONE);
                    }
                    setPlayerStatu(STATU_PLAY);
                }
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                super.onLoadingChanged(isLoading);
            }
        });
    }

    public void setPlayerStatu(int statu){
        mStatus = statu;
        switch (statu){
            case STATU_LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                mPlayerView.setVisibility(View.GONE);
                mPauseView.setVisibility(View.GONE);
                break;
            case STATU_PAUSE:
                mProgressBar.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.VISIBLE);
                mPauseView.setVisibility(View.GONE);
                break;
            case STATU_PLAY:
                mProgressBar.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.GONE);
                mPauseView.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void finishAfterTransition() {
        mMarkView.setVisibility(View.VISIBLE);
        Intent intent = new Intent();
        intent.putExtra(CURRENT, mPlayer.getCurrentPosition());
        setResult(Activity.RESULT_OK, intent);
        super.finishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        Bitmap bitmap = mTextureView.getBitmap();
        if (mMarkView.getVisibility() != View.VISIBLE){
            mMarkView.setImageBitmap(bitmap);
            ThumableManager.setDrawable(mMarkView.getDrawable());
        }

        mMarkView.setVisibility(View.VISIBLE);
        mDivControlView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        mPlayer.setPlayWhenReady(false);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.setPlayWhenReady(false);
        mTimer.cancel();
        mTimer = null;
        mIsStopTask = true;
    }
}
