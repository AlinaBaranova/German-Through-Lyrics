package com.alinabaranova.youtubedemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;import android.os.Bundle;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends YouTubeBaseActivity {

    YouTubePlayerView mYouTubePlayerView;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    YouTubePlayer.PlayerStateChangeListener mPlayerStateChangeListener;
    YouTubePlayer myYouTubePlayer;

    ArrayList<Integer> times = new ArrayList<>();
    ArrayList<String> lines = new ArrayList<>();

    ArrayList<Integer> lineIds = new ArrayList<>(); // ids for changing background of TextViews
    ArrayList<Integer> fullIds = new ArrayList<>(); // ids for focusing ScrollView on TextViews

    int textViewsSeen = 22; // number of TextViews seen on the screen (can be different for different devices!)
    int currentLineNumber = 0; // number for changing background of TextViews
    int currentTextViewNumber = 0; // number for focusing ScrollView on TextViews

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ScrollView scrollView = findViewById(R.id.scrollView);

        try {

            // load text
            KaraokeText myText = new KaraokeText(getAssets().open("rammstein_mutter.txt"));
            times = myText.getTimes();
            lines = myText.getLines();

            // fill linearLayout with dynamically created TextViews - lines of lyrics
            LinearLayout linearLayout = findViewById(R.id.linearLayout);
            for (int c = 0; c < myText.getLines().size(); c++) {

                TextView textView = new TextView(getApplicationContext());
                String line = myText.getLines().get(c);
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

                final Handler handler = new Handler();

                Runnable run = new Runnable() {

                    public void run() {

                        if (Math.round(myYouTubePlayer.getCurrentTimeMillis()/1000.0) == times.get(currentLineNumber)) {

                            if (currentLineNumber > 0) {

                                (findViewById(lineIds.get(currentLineNumber-1))).setBackgroundColor(Color.parseColor("#20AD65"));

                            }

                            (findViewById(lineIds.get(currentLineNumber))).setBackgroundColor(Color.parseColor("#00FF7D"));

                            int focusPoint = currentTextViewNumber - textViewsSeen/2 + 2;

                            if (focusPoint < 0) {

                                focusPoint = 0;

                            } else if (lines.size()-focusPoint <= 22) {

                                focusPoint = lines.size()-1;
                            }

                            scrollView.smoothScrollTo(0, (findViewById(fullIds.get(focusPoint))).getTop());

                            if (currentLineNumber < times.size()-1) {

                                currentLineNumber++;
                                currentTextViewNumber++;

                                if (((TextView) findViewById(fullIds.get(currentTextViewNumber))).getText().toString().equals("")) {

                                    currentTextViewNumber++;
                                }

                            }

                        }

                        handler.postDelayed(this, 10);

                    }
                };

                if (lines.size() > 0 && times.size() > 0 && lineIds.size() > 0) {

                    handler.post(run);

                }

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

                youTubePlayer.loadVideo("gNdnVVHfseA");
                youTubePlayer.setPlayerStateChangeListener(mPlayerStateChangeListener);

                myYouTubePlayer = youTubePlayer;

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }

        };
        mYouTubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);

    }
}
