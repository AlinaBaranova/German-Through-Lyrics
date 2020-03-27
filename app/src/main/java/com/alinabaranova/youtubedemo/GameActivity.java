package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GameActivity extends AppCompatActivity
        implements View.OnClickListener {

    YouTubePlayer myYouTubePlayer;
    YouTubePlayer.PlayerStateChangeListener mPlayerStateChangeListener;

    ScrollViewWithMaxHeight scrollView;
    LinearLayout textLinearLayout;

    ArrayList<Integer> times = new ArrayList<>();
    ArrayList<String> lines = new ArrayList<>();

    ArrayList<Integer> lineIds = new ArrayList<>(); // ids for changing background of TextViews
    ArrayList<Integer> fullIds = new ArrayList<>(); // ids for focusing ScrollView on TextViews

    JSONArray constructions;    // array of constructions for song
    int questionNumber = 0;     // number of current blank
    int questionLineNumber;     // number of line with current blank
    String rightOption;         // right option for current blank
    ArrayList<int[]> rowsAndCols;   // arraylist for adding buttons to gridlayout in a right way
    GridLayout gridLayout;          // GridLayout (contains buttons with answer options)
    LinearLayout bigLayout;

    LinearLayout controlLayout;     // layout for "try again" and "skip" or "play again" and "go back" buttons
    Button controlButton1;
    Button controlButton2;          // buttons in control layout
    String color;   // color for highlighting

    int textViewsSeen = 22; // number of TextViews seen on the screen (can be different for different devices!)
    int currentLineNumber = 0; // number for changing background of TextViews
    int currentTextViewNumber = 0; // number for focusing ScrollView on TextViews
    // array for checking if stop after certain line has already been made or not
    List<Integer> textViewNumbersSeen = new ArrayList<>();

    Intent intent;
    int songId;
    String constrType;

    SQLiteDatabase database;

    /**
     * Loads song text and fills it with blanks.
     */
    private void loadGameText() {

        try {

            // load arrays of times and lines
            Cursor cursor = database.rawQuery("SELECT * FROM songs WHERE song_id=" + songId, null);

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

            // load json containing constructions
            constrType = intent.getStringExtra("constrType");
            cursor = database.rawQuery("SELECT * FROM constructions WHERE " +
                    "song_id=" + songId + " AND constr_type='" + constrType + "'", null);

            constructions = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                constructions = new JSONArray(cursor.getString(cursor.getColumnIndex("constr_json")));
                cursor.close();
            }

            // get color code for highlighting
            color = intent.getStringExtra("color");

            // fill linearLayout with dynamically created TextViews - lines of lyrics
            textLinearLayout = new LinearLayout(getApplicationContext());
            textLinearLayout.setOrientation(LinearLayout.VERTICAL);

            int highlightCount = 0;     // number of current blank
            JSONObject curDict = constructions.getJSONObject(highlightCount);   // current construction
            int lineNumber = curDict.getInt("line");    // number of line contaning current blank

            for (int c = 0; c < lines.size(); c++) {

                TextView textView = new TextView(getApplicationContext());
                String line = lines.get(c);
                String newLine = line;
                if (c == lineNumber) {
                    try {

                        // get array of indexes; word between them should be replaced with a blank
                        JSONArray indexes = curDict.getJSONArray("index_blank");

                        newLine = "";

                        int firstIndex = indexes.getInt(0);
                        int lastIndex = indexes.getInt(1);

                        newLine += line.substring(0, firstIndex);
                        newLine += "<span style=\"background-color: " + color + "\">" + "&nbsp;...&nbsp;" + "</span>";
                        newLine += line.substring(lastIndex);

                        // increase linenumber
                        if (highlightCount < constructions.length()-1) {

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
                textView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                textView.setTextColor(Color.parseColor("#1F618D"));

                textView.setTextSize(16);

                textLinearLayout.addView(textView);

            }

            } catch(org.json.JSONException ex) {
                ex.printStackTrace();
            }

    }

    private void enableDisableButtons(boolean ifEnable) {

        // enable/disable buttons in GridLayout
        int buttonCount = gridLayout.getChildCount();
        for (int i=0; i < buttonCount; i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            button.setEnabled(ifEnable);
        }

    }

    /**
     * Timer for selecting line currently sung.
     */
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

                    // pause video if question hasn't been answered
                    if ((textViewNumbersSeen.contains(questionLineNumber) && currentTextViewNumber == questionLineNumber+1) ||
                            (((! lines.get(questionLineNumber).equals("") && ! textViewNumbersSeen.contains(questionLineNumber)) || (! lines.get(questionLineNumber+1).equals("") && ! textViewNumbersSeen.contains(questionLineNumber+1))) && currentTextViewNumber == questionLineNumber+2)) {

                        myYouTubePlayer.pause();        // pause video

                        // disable buttons in GridLayout
                        enableDisableButtons(false);

                        controlButton1.setText("Try again");
                        controlButton2.setText("Skip");

//                        bigLayout.setBackgroundColor(Color.parseColor("#ABB2B9"));
                        controlLayout.setVisibility(View.VISIBLE);      // show control layout


                    }

                    // increment number for changing background of TextViews and number for focusing ScrollView on TextViews
                    if (currentLineNumber < times.size()-1) {

                        // add current number of textview to array of text view numbers
                        textViewNumbersSeen.add(currentTextViewNumber);

                        currentLineNumber++;
                        currentTextViewNumber++;

                        if (((TextView) findViewById(fullIds.get(currentTextViewNumber+1))).getText().toString().equals("")) {

                            currentTextViewNumber++;
                        }

                    }

                }

                handler.postDelayed(this, 5);

            }
        };

        // if text loaded correctly, run timer
        if (lines.size() > 0 && times.size() > 0 && lineIds.size() > 0) {

            handler.post(run);

        }

    }

    private void insertAndSwitch() {

        if (questionNumber < constructions.length()) {
            // insert right answer
            try {
                // get line for which question has been answered
                String questionLine = lines.get(questionLineNumber);

                // highlight right option in line
                JSONArray indexes = constructions.getJSONObject(questionNumber).getJSONArray("index_blank");
                int firstIndex = (int) indexes.get(0);
                int lastIndex = (int) indexes.get(1);
                String questionTextViewText = questionLine.substring(0, firstIndex);
                questionTextViewText += "<span style=\"background-color: " + color + "\">" + questionLine.substring(firstIndex, lastIndex) + "</span>";
                questionTextViewText += questionLine.substring(lastIndex);

                // get TextView filled with the line with blank and fill it with created line
                TextView questionTextView = findViewById(fullIds.get(questionLineNumber));
                questionTextView.setText(Html.fromHtml(questionTextViewText));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        questionNumber++;
        if (questionNumber < constructions.length()) {
            fillGridLayout();
        }
        else {
            // get line number of first line with blank, so video can play further
            try {
                JSONObject curDict = constructions.getJSONObject(0);
                questionLineNumber = curDict.getInt("line");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // disable buttons
            enableDisableButtons(false);

            // change color of buttons
            int buttonCount = gridLayout.getChildCount();
            for (int i=0; i < buttonCount; i++) {
                Button button = (Button) gridLayout.getChildAt(i);
                button.getBackground().setColorFilter(Color.parseColor("#E5E8E8"), PorterDuff.Mode.MULTIPLY);
                button.setTextColor(Color.parseColor("#616A6B"));
            }
        }

    }

    /**
     * Gets called when one of the answer buttons is clicked.
     * @param v: (Button) button clicked
     */
    @Override
    public void onClick(View v) {

        // get text of button that was clicked on
        Button button = (Button) v;
        String answer = button.getText().toString();

        // if answer is right, show options for the next question
        if (answer.equals(rightOption)) {

            insertAndSwitch();

        }

    }

    public void chooseMethod(View view) {

        String buttonText = ((Button) view).getText().toString();
        switch (buttonText) {
            case "Try again":
                repeat(view);
                break;

            case "Skip":
                skip(view);
                break;

            case  "Go back":
                onBackPressed();
                break;

            default:
                Intent intent = new Intent(getApplicationContext(), GameActivity.class);

                intent.putExtra("constrType", constrType);
                intent.putExtra("color", color);
                intent.putExtra("songId", songId);

                startActivity(intent);
        }

    }

    public void skip(View view) {

        // insert right answer and show new options
        insertAndSwitch();

        // hide panel
        controlLayout.setVisibility(View.GONE);

        // enable buttons
        enableDisableButtons(true);

        // continue playing video
        myYouTubePlayer.play();

    }

    public void repeat(View view) {

        // get number of empty lines before line previous to line with blank
        int emptyLines = 0;
        for (int i=0; i < questionLineNumber; i++) {
            if (lines.get(i).equals("")) {
                emptyLines++;
            }
        }
        // get time for video to start playing from
        int millis = times.get(questionLineNumber - 1 - emptyLines) * 1000;

        // set currentLineNumber to line previous to line with blank
        int highlightIndex;
        if (currentLineNumber == times.size()-1) {
            highlightIndex = currentLineNumber;
        } else {
            highlightIndex = currentLineNumber - 1;
        }
        // stop highlighting line that is currently highlighted
        (findViewById(lineIds.get(highlightIndex))).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));

        // set current line number to line previous to line with blank
        currentLineNumber = questionLineNumber - 1 - emptyLines;
        // highlight currentLineNumber
        (findViewById(lineIds.get(currentLineNumber))).setBackgroundColor(Color.parseColor("#D6EAF8"));

        // set currentTextViewNumber to line previous to line with blank
        currentTextViewNumber = questionLineNumber - 2;
        // set focus on line using currentTextViewNumber
        int focusPoint = currentTextViewNumber - textViewsSeen/2 + 2;

        if (focusPoint < 0) {

            focusPoint = 0;

        } else if (lines.size()-focusPoint <= 22) {

            focusPoint = lines.size()-1;
        }

        scrollView.smoothScrollTo(0, (findViewById(fullIds.get(focusPoint))).getTop());

        // get subarray from textViewNumbersSeen until currentTextViewNumber included
        int endIndex;
        if (textViewNumbersSeen.contains(currentTextViewNumber)) {
            endIndex = textViewNumbersSeen.indexOf(currentTextViewNumber);
        } else {
            endIndex = textViewNumbersSeen.indexOf(currentTextViewNumber-1);
        }
        textViewNumbersSeen = textViewNumbersSeen.subList(0, endIndex);

        // enable buttons
        enableDisableButtons(true);

        // switch video to time of line previous to line with blank and play video
        myYouTubePlayer.seekToMillis(millis);
        myYouTubePlayer.play();

        // hide layout
        controlLayout.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // hide bar with app name
        Objects.requireNonNull(getSupportActionBar()).hide();

        // layout for all elements lower than video
        bigLayout = findViewById(R.id.linearLayout);

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
                    if (count < textLinearLayout.getChildCount()) {
                        TextView view = (TextView) textLinearLayout.getChildAt(count);
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

        // create resizable scroll view
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int scrollViewHeight = (int) Math.round(displayMetrics.heightPixels * 0.58);

        scrollView = new ScrollViewWithMaxHeight(getApplicationContext());
        scrollView.setMaxHeight(scrollViewHeight);
        scrollView.setVerticalScrollBarEnabled(false);

        intent = getIntent();
        songId = intent.getIntExtra("songId", -1);

        // open database
//        getApplicationContext().deleteDatabase("app.db");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        database = dbHelper.getReadableDatabase();

        loadGameText();     // add text views to text linear layout
        scrollView.addView(textLinearLayout);   // add text linear layout to scroll view
        bigLayout.addView(scrollView);      // add scroll view to main linear layout

        // rows and columns for placing buttons in gridlayout
        rowsAndCols = new ArrayList<>();
        rowsAndCols.add(new int[] {0, 0});
        rowsAndCols.add(new int[] {1, 0});
        rowsAndCols.add(new int[] {0, 1});
        rowsAndCols.add(new int[] {1, 1});

        // grid layout for buttons
        gridLayout = new GridLayout(getApplicationContext());
        fillGridLayout();

        // add grid layout to main linear layout
        bigLayout.addView(gridLayout);

        // initializing variables not included in bigLayout
        controlLayout = findViewById(R.id.controlLayout);
        controlButton1 = findViewById(R.id.controlButton1);
        controlButton1.getBackground().setColorFilter(Color.parseColor("#D6EAF8"), PorterDuff.Mode.MULTIPLY);
        controlButton2 = findViewById(R.id.controlButton2);
        controlButton2.getBackground().setColorFilter(Color.parseColor("#D6EAF8"), PorterDuff.Mode.MULTIPLY);
    }

    /**
     * Fills gridLayout after song text with answer options.
     */
    private void fillGridLayout() {

        // remove all previous answer options, if there are any
        gridLayout.removeAllViews();

        try {
            // get current construction
            JSONObject curDict = constructions.getJSONObject(questionNumber);

            // get line number with current blank
            questionLineNumber = curDict.getInt("line");

            JSONArray options = curDict.getJSONArray("distractors");    // get distractors

            // get the right option and add it to array of distractors
            rightOption = curDict.getString("right_option");
            options.put(rightOption);

            // transform JSONArray into ArrayList (to be able to shuffle it)
            ArrayList<String> optionsArray = new ArrayList<>();
            for (int i = 0; i < options.length(); i++) {

                optionsArray.add(options.getString(i));

            }
            Collections.shuffle(optionsArray);

            // set sizes of gridLayout
            gridLayout.setColumnCount(2);
            gridLayout.setRowCount(optionsArray.size() / 2);

            // fill gridlayout with buttons
            for (int i = 0; i < optionsArray.size(); i++) {

                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.columnSpec = GridLayout.spec(rowsAndCols.get(i)[0], 1, 1f);
                param.rowSpec = GridLayout.spec(rowsAndCols.get(i)[1], 1, 1f);

                Button button = new Button(getApplicationContext());
                button.setTransformationMethod(null);
                button.setText(optionsArray.get(i));
                button.setLayoutParams(param);
                button.getBackground().setColorFilter(Color.parseColor("#D6EAF8"), PorterDuff.Mode.MULTIPLY);
                button.setTextSize(16);
                button.setTextColor(Color.parseColor("#1F618D"));
                button.setOnClickListener(this);
                gridLayout.addView(button, i);

            }

        } catch (JSONException e) {
            e.printStackTrace();
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
