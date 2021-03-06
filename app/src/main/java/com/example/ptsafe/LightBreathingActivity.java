package com.example.ptsafe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LightBreathingActivity extends AppCompatActivity {
    @BindView(R.id.tv_circle)
    TextView tvCircle;
    @BindView(R.id.tv_circle_txt)
    TextView tvCircleTxt;

    @BindView(R.id.tv_all)
    TextView tvAll;

    private int SHOW = 0;
    private int STATE = 0;
    boolean start = true;
    /**
     * countdown marker
     */
    public static final int COUNTDOWN_TIME_CODE = 99999;
    /**
     * countdown interval
     */
    public static final int DELAY_MILLIS = 1000;
    /**
     * countdown maximum
     */
    public int MAX_COUNT = 5;

    private CountDownTimer timer;
    private long timeStamp = 8640000;
    private boolean end = false;
    private MediaPlayer mMediaPlayer;
    private MediaPlayer focusMediaPlayer;
    private MediaPlayer relaxMediaPlayer;
    private MediaPlayer inMediaPlayer;
    private MediaPlayer outMediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_breathing);

        relaxMediaPlayer = new MediaPlayer();
        relaxMediaPlayer = MediaPlayer.create(this, R.raw.relax);
        relaxMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        relaxMediaPlayer.setLooping(false);
        focusMediaPlayer = new MediaPlayer();
        focusMediaPlayer = MediaPlayer.create(this, R.raw.focus);
        focusMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        focusMediaPlayer.setLooping(false);
        inMediaPlayer = new MediaPlayer();
        inMediaPlayer = MediaPlayer.create(this, R.raw.in);
        inMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        inMediaPlayer.setLooping(false);
        outMediaPlayer = new MediaPlayer();
        outMediaPlayer = MediaPlayer.create(this, R.raw.out);
        outMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        outMediaPlayer.setLooping(false);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.bensound_relaxing);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();
        ButterKnife.bind(this);

        timer = new CountDownTimer(60000*3, 1000) {
            public void onTick(long millisUntilFinished) {
                long secend = millisUntilFinished / 1000 / 60;
                if ((millisUntilFinished - secend * 60 * 1000) / 1000 < 10) {
                    tvAll.setText("0" + secend + ":" + "0"+(millisUntilFinished - secend * 60 * 1000) / 1000 + "");
                }
                else {
                    tvAll.setText("0" + secend + ":" + (millisUntilFinished - secend * 60 * 1000) / 1000 + "");
                }
            }

            public void onFinish() {
                end = true;
                tvCircle.setText("");
                tvCircleTxt.setText("well done!");
//                Toast.makeText(getApplicationContext(),"well done",Toast.LENGTH_SHORT).show();
            }
        };
        //create a handler
        CountdownTimeHandler handler = new CountdownTimeHandler(this);
        //Create a new message
        Message message = Message.obtain();
        message.what = COUNTDOWN_TIME_CODE;
        message.arg1 = MAX_COUNT;
        //Send a message for the first time
        handler.sendMessageDelayed(message, DELAY_MILLIS);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        focusMediaPlayer.stop();
        relaxMediaPlayer.stop();
        inMediaPlayer.stop();
        outMediaPlayer.stop();
        Intent intent = new Intent(LightBreathingActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("fromLightBreathing","Y");
        intent.putExtras(bundle);
        startActivity(intent);
    }


    public class CountdownTimeHandler extends Handler {
        /**
         * countdown minimum
         */
        public int MIN_COUNT = 0;
        //Create a weak reference to MainActivity
        final WeakReference<LightBreathingActivity> mWeakReference;

        public CountdownTimeHandler(LightBreathingActivity activity) {
            this.mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Get weak reference to MainActivity
            LightBreathingActivity activity = mWeakReference.get();
            switch (msg.what) {
                case COUNTDOWN_TIME_CODE:
                    int value = msg.arg1;
                    //Control of sending messages cyclically
                    if (value > MIN_COUNT) {
                        if (activity.SHOW > 1) {
                            if (activity.start){
                                activity.start = false;
                                activity.timer.start();
                            }
                            activity.tvCircle.setText(String.valueOf(value));

                        }
                        value--;
                        Message message = Message.obtain();
                        message.what = COUNTDOWN_TIME_CODE;
                        message.arg1 = value;
                        sendMessageDelayed(message, DELAY_MILLIS);
                    } else {
                        activity.tvCircle.setText("");
                        Message message = Message.obtain();
                        message.what = COUNTDOWN_TIME_CODE;
                        message.arg1 = activity.MAX_COUNT;
                        sendMessageDelayed(message, DELAY_MILLIS);
                        activity.SHOW += 1;
                        activity.STATE += 1;
                    }
                    if (activity.SHOW == 0) {

                        if (!activity.end){
                            activity.tvCircleTxt.setText("Relax and get comfortable");
                            relaxMediaPlayer.start();

                        }
                    } else if (activity.SHOW == 1) {
                        //activity.tvButtom.setVisibility(View.GONE);
                        if (!activity.end){
                            activity.tvCircleTxt.setText("focus on your breathing");
                            focusMediaPlayer.start();
                            relaxMediaPlayer.stop();
                        }

                    } else {
                        if (focusMediaPlayer.isPlaying()) {
                            focusMediaPlayer.stop();
                        }
                        if (activity.STATE % 2 == 0) {
                            if (!activity.end){
                                activity.tvCircleTxt.setText("breathe in ");
//                                inMediaPlayer.start();
                            }

                        } else {
                            if (!activity.end){
                                activity.tvCircleTxt.setText("breathe out ");
//                                outMediaPlayer.start();
                            }

                        }
                    }
                    break;
            }
        }
    }
}