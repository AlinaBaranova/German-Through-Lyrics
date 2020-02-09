package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class KaraokeActivity extends YouTubeBaseActivity {

    YouTubePlayerView mYouTubePlayerView;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    YouTubePlayer.PlayerStateChangeListener mPlayerStateChangeListener;
    YouTubePlayer myYouTubePlayer;

    ScrollView scrollView;

    ArrayList<Integer> times = new ArrayList<>();
    ArrayList<String> lines = new ArrayList<>();

    ArrayList<Integer> lineIds = new ArrayList<>(); // ids for changing background of TextViews
    ArrayList<Integer> fullIds = new ArrayList<>(); // ids for focusing ScrollView on TextViews

    int textViewsSeen = 22; // number of TextViews seen on the screen (can be different for different devices!)
    int currentLineNumber = 0; // number for changing background of TextViews
    int currentTextViewNumber = 0; // number for focusing ScrollView on TextViews

    private void videoTimer() {

        // when video starts, get current time of video every 10 milliseconds

        final Handler handler = new Handler();

        Runnable run = new Runnable() {

            public void run() {

                // rounding number of seconds works best, because value of .getCurrentTimeMillis updates only once or twice in a second
                if (Math.round(myYouTubePlayer.getCurrentTimeMillis()/1000.0) == times.get(currentLineNumber)) {

                    // if there is line before current, stop highlighting it
                    if (currentLineNumber > 0) {

                        (findViewById(lineIds.get(currentLineNumber-1))).setBackgroundColor(Color.parseColor("#20AD65"));

                    }

                    // highlight current line
                    (findViewById(lineIds.get(currentLineNumber))).setBackgroundColor(Color.parseColor("#00FF7D"));


                    // set focus on line so that current line is in center
                    int focusPoint = currentTextViewNumber - textViewsSeen/2 + 2;

                    if (focusPoint < 0) {

                        focusPoint = 0;

                    } else if (lines.size()-focusPoint <= 22) {

                        focusPoint = lines.size()-1;
                    }

                    scrollView.smoothScrollTo(0, (findViewById(fullIds.get(focusPoint))).getTop());

                    // increment number for changing background of TextViews and number for focusing ScrollView on TextViews
                    if (currentLineNumber < times.size()-1) {

                        currentLineNumber++;
                        currentTextViewNumber++;

                        if (((TextView) findViewById(fullIds.get(currentTextViewNumber+1))).getText().toString().equals("")) {

                            currentTextViewNumber++;
                        }

                    }

                }

                handler.postDelayed(this, 10);

            }
        };

        // if text loaded correctly, run timer
        if (lines.size() > 0 && times.size() > 0 && lineIds.size() > 0) {

            handler.post(run);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_karaoke);

        scrollView = findViewById(R.id.scrollView);

        final Intent intent = getIntent();

        try {

            // load text
            String textFilename = intent.getStringExtra("textFilename");
            KaraokeText myText = new KaraokeText(getAssets().open(textFilename));
            times = myText.getTimes();
            lines = myText.getLines();

            // fill linearLayout with dynamically created TextViews - lines of lyrics
            LinearLayout linearLayout = findViewById(R.id.linearLayout);
            for (int c = 0; c < lines.size(); c++) {

                TextView textView = new TextView(getApplicationContext());
                String line = lines.get(c);
                String key = Integer.toString(c);
                textView.setText(line);

                // set id and add it to both ArrayLists of ids
                textView.setId(c);
                if (! line.equals("")) {

                    // c and textView.getId() are not the same i.e. findViewById(c) doesn't work
                    lineIds.add(textView.getId());

                }
                fullIds.add(textView.getId());

                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setBackgroundColor(Color.parseColor("#20AD65"));
                textView.setTextColor(Color.parseColor("#000000"));

                linearLayout.addView(textView);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mYouTubePlayerView = findViewById(R.id.youtubePlay);

        mPlayerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(String s) {
            }

            @Override
            public void onAdStarted() {

            }

            @Override
            public void onVideoStarted() {

                videoTimer();

            }

            @Override
            public void onVideoEnded() {

            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {

            }
        };

        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

                String videoId = intent.getStringExtra("videoId");
                youTubePlayer.loadVideo(videoId);
                youTubePlayer.setPlayerStateChangeListener(mPlayerStateChangeListener);

                myYouTubePlayer = youTubePlayer;

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }

        };
        mYouTubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);

    }

    @Override
    public void onBackPressed() {
        // start activity MenuActivity, otherwise app stops

        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(intent);

    }
}
