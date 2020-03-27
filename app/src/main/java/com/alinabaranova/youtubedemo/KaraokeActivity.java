package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

public class KaraokeActivity extends AppCompatActivity {

    YouTubePlayer.PlayerStateChangeListener mPlayerStateChangeListener;
    YouTubePlayer myYouTubePlayer;

    ScrollView scrollView;

    ArrayList<Integer> times = new ArrayList<>();
    ArrayList<String> lines = new ArrayList<>();
    LinearLayout linearLayout;

    ArrayList<Integer> lineIds = new ArrayList<>(); // ids for changing background of TextViews
    ArrayList<Integer> fullIds = new ArrayList<>(); // ids for focusing ScrollView on TextViews

    int textViewsSeen; // number of TextViews seen on the screen
    int currentLineNumber = 0; // number for changing background of TextViews
    int currentTextViewNumber = 0; // number for focusing ScrollView on TextViews

    SQLiteDatabase database;
    int songId;

    LinearLayout controlLayout;     // layout for "play again" and "go back" buttons
    Button controlButton1;
    Button controlButton2;          // buttons in control layout

    private void videoTimer() {

        // when video starts, get current time of video every 10 milliseconds

        final Handler handler = new Handler();

        Runnable run = new Runnable() {

            public void run() {

                // rounding number of seconds works best, because value of .getCurrentTimeMillis updates only once or twice in a second
                if (Math.round(myYouTubePlayer.getCurrentTimeMillis()/1000.0) == times.get(currentLineNumber)) {

                    // if there is line before current, stop highlighting it
                    if (currentLineNumber > 0) {

                        (findViewById(lineIds.get(currentLineNumber-1))).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));

                    }

                    // highlight current line
                    (findViewById(lineIds.get(currentLineNumber))).setBackgroundColor(Color.parseColor("#D6EAF8"));


                    // set focus on line so that current line is in center
                    int focusPoint = currentTextViewNumber - textViewsSeen/2 + 2;

                    if (focusPoint < 0) {

                        focusPoint = 0;

                    } else if (lines.size()-focusPoint <= textViewsSeen) {

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

        // hide bar with app name
        Objects.requireNonNull(getSupportActionBar()).hide();

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

                Rect scrollBounds = new Rect();
                scrollView.getHitRect(scrollBounds);
                boolean visible = true;
                int count = 0;
                while (visible) {
                    if (count < linearLayout.getChildCount()) {
                        TextView view = (TextView) linearLayout.getChildAt(count);
                        if (! view.getLocalVisibleRect(scrollBounds)) {
                            visible = false;
                        }

                        count++;
                    }
                }

                textViewsSeen = count - 1;

                videoTimer();

            }

            @Override
            public void onVideoEnded() {

                controlButton1.setText("Play again");
                controlButton2.setText("Go back");
                controlLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {

            }
        };

        YouTubePlayerSupportFragment youTubePlayerFragment = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_player_fragment);
        Objects.requireNonNull(youTubePlayerFragment).initialize(YouTubeConfig.getApiKey(), new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

                Cursor cursor = database.rawQuery("SELECT * FROM songs WHERE song_id=" + songId, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    String videoId = cursor.getString(cursor.getColumnIndex("video_id"));
                    cursor.close();

                    youTubePlayer.loadVideo(videoId);
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    youTubePlayer.setPlayerStateChangeListener(mPlayerStateChangeListener);

                    myYouTubePlayer = youTubePlayer;
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });

        scrollView = findViewById(R.id.scrollView);

        final Intent intent = getIntent();
        songId = intent.getIntExtra("songId", -1);

        // open database
//        getApplicationContext().deleteDatabase("app.db");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        database = dbHelper.getReadableDatabase();

        // fill array of times and lines
        Cursor cursor = database.rawQuery("SELECT * FROM songs WHERE song_id=" + songId, null);

        try {
            JSONArray timesArray;
            JSONArray linesArray;

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                timesArray = new JSONArray(cursor.getString(cursor.getColumnIndex("times")));
                linesArray = new JSONArray(cursor.getString(cursor.getColumnIndex("lines")));
                cursor.close();

                for(int i=0; i < timesArray.length(); i++) {
                    times.add(timesArray.getInt(i));
                }
                for(int i=0; i < linesArray.length(); i++) {
                    lines.add(linesArray.getString(i));
                }
            }

            // fill linearLayout with dynamically created TextViews - lines of lyrics
            linearLayout = findViewById(R.id.linearLayout);
            for (int c = 0; c < lines.size(); c++) {

                TextView textView = new TextView(getApplicationContext());
                String line = lines.get(c);
                textView.setText(line);

                // set id and add it to both ArrayLists of ids
                textView.setId(c);
                if (! line.equals("")) {

                    // c and textView.getId() are not the same i.e. findViewById(c) doesn't work
                    lineIds.add(textView.getId());

                }
                fullIds.add(textView.getId());

                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                textView.setTextColor(Color.parseColor("#1F618D"));

                textView.setTextSize(16);

                linearLayout.addView(textView);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        controlLayout = findViewById(R.id.controlLayout);
        controlButton1 = findViewById(R.id.controlButton1);
        controlButton1.getBackground().setColorFilter(Color.parseColor("#D6EAF8"), PorterDuff.Mode.MULTIPLY);
        controlButton2 = findViewById(R.id.controlButton2);
        controlButton2.getBackground().setColorFilter(Color.parseColor("#D6EAF8"), PorterDuff.Mode.MULTIPLY);

    }

    public void chooseMethod(View view) {

        String buttonText = ((Button) view).getText().toString();
        if (buttonText.equals("Go back")) {
            onBackPressed();
        } else {
            Intent intent = new Intent(getApplicationContext(), KaraokeActivity.class);

            intent.putExtra("songId", songId);

            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        // start activity MenuActivity, otherwise app stops

        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.putExtra("songId", songId);
        startActivity(intent);

    }
}
