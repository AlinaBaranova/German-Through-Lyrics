package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class HighlightActivity extends YouTubeBaseActivity {

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

    Intent intent;
    int songId;

    LinearLayout controlLayout;     // layout for "try again" and "skip" or "play again" and "go back" buttons
    Button controlButton1;
    Button controlButton2;          // buttons in control layout

    SQLiteDatabase database;

    private void loadHighlightedText() {

        // load arrays of times and lines
        Cursor cursor = database.rawQuery("SELECT * FROM songs WHERE song_id=" + songId, null);

        try {
            JSONArray timesArray;
            JSONArray linesArray;

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                timesArray = new JSONArray(cursor.getString(cursor.getColumnIndex("times")));
                linesArray = new JSONArray(cursor.getString(cursor.getColumnIndex("lines")));

                for(int i=0; i < timesArray.length(); i++) {
                    times.add(timesArray.getInt(i));
                }
                for(int i=0; i < linesArray.length(); i++) {
                    lines.add(linesArray.getString(i));
                }
            }

            // load json containing constructions
            String constrType = intent.getStringExtra("constrType");
            cursor = database.rawQuery("SELECT * FROM constructions WHERE " +
                    "song_id=" + songId + " AND constr_type='" + constrType + "'", null);

            JSONArray constructions = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                constructions = new JSONArray(cursor.getString(cursor.getColumnIndex("constr_json")));
            }
            cursor.close();

            // get color code for highlighting
            String color = intent.getStringExtra("color");

            // fill linearLayout with dynamically created TextViews - lines of lyrics
            LinearLayout linearLayout = findViewById(R.id.linearLayout);

            int highlightCount = 0;

            JSONObject curDict = constructions.getJSONObject(highlightCount);
            int lineNumber = curDict.getInt("line");

            for (int c = 0; c < lines.size(); c++) {

                TextView textView = new TextView(getApplicationContext());
                String line = lines.get(c);
                String newLine = line;
                if (c == lineNumber) {
                    try {

                        JSONArray indexes = curDict.getJSONArray("indexes");

                        newLine = "";

                        int simpleIndex = 0;

                        for (int i=0; i < indexes.length(); i++) {

                            int firstIndex = (int)indexes.getJSONArray(i).get(0);
                            int lastIndex = (int)indexes.getJSONArray(i).get(1);

                            newLine += line.substring(simpleIndex, firstIndex);
                            newLine += "<span style=\"background-color: " + color + "\">" + line.substring(firstIndex, lastIndex + 1) + "</span>";

                            simpleIndex = lastIndex + 1;

                        }

                        newLine += line.substring(simpleIndex);

                        // increase linenumber
                        if (highlightCount < constructions.length()) {

                            highlightCount++;
                            curDict = constructions.getJSONObject(highlightCount);
                            lineNumber = curDict.getInt("line");

                        }

                    } catch (org.json.JSONException ex) {
                        ex.printStackTrace();
                    }

                }
                textView.setText(Html.fromHtml(newLine));

                // set id and add it to both ArrayLists of ids
                textView.setId(c);
                if (!line.equals("")) {

                    // c and textView.getId() are not the same i.e. findViewById(c) doesn't work
                    lineIds.add(textView.getId());

                }
                fullIds.add(textView.getId());

                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setBackgroundColor(Color.parseColor("#20AD65"));
                textView.setTextColor(Color.parseColor("#000000"));

                linearLayout.addView(textView);

            }

        } catch(org.json.JSONException ex) {
            ex.printStackTrace();
        }

    }

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
        setContentView(R.layout.activity_highlight);

        scrollView = findViewById(R.id.scrollView);

        intent = getIntent();
        // get id of song
        songId = intent.getIntExtra("songId", -1);

        // open database
        getApplicationContext().deleteDatabase("app.db");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        database = dbHelper.getReadableDatabase();

        loadHighlightedText();

        controlLayout = findViewById(R.id.controlLayout);
        controlButton1 = findViewById(R.id.controlButton1);
        controlButton2 = findViewById(R.id.controlButton2);

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

                controlButton1.setText("Play again");
                controlButton2.setText("Go back");
                controlLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {

            }
        };

        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

                // load video id from database
                Cursor cursor = database.rawQuery("SELECT * FROM songs WHERE song_id=" + songId, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    String videoId = cursor.getString(cursor.getColumnIndex("video_id"));

                    youTubePlayer.loadVideo(videoId);
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    youTubePlayer.setPlayerStateChangeListener(mPlayerStateChangeListener);

                    myYouTubePlayer = youTubePlayer;
                }
                cursor.close();

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
        intent.putExtra("songId", songId);
        startActivity(intent);

    }
}
