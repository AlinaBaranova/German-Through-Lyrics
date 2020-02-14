package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class GameActivity extends YouTubeBaseActivity
    implements View.OnClickListener {

    YouTubePlayerView mYouTubePlayerView;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    YouTubePlayer.PlayerStateChangeListener mPlayerStateChangeListener;
    YouTubePlayer myYouTubePlayer;

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

    String color;   // color for highlighting

    // needed for runnable in videoTimer (otherwise should be declared final)
    int highlightCount;
    JSONObject curDict;
    int lineNumber;

    int textViewsSeen = 22; // number of TextViews seen on the screen (can be different for different devices!)
    int currentLineNumber = 0; // number for changing background of TextViews
    int currentTextViewNumber = 0; // number for focusing ScrollView on TextViews

    Intent intent;

    /**
     * Loads file with json object.
     * @param filename: (String) name of file containing json object.
     * @return (String) contains json object
     */
    private String loadJSONFromAsset(String filename) {

        String json = null;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);

        } catch(IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    /**
     * Loads song text and fills it with blanks.
     */
    private void loadGameText() {

        try {

            // load text
            String textFilename = intent.getStringExtra("textFilename");
            KaraokeText myText = new KaraokeText(getAssets().open(textFilename));
            times = myText.getTimes();
            lines = myText.getLines();

            try {
                // load json file
//                String jsonFilename = intent.getStringExtra("jsonFilename");
                String jsonFilename = "adam-angst_splitter-von-granaten_pref.json";
                // load constructions for song
                constructions = new JSONArray(loadJSONFromAsset(jsonFilename));

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

                    textLinearLayout.addView(textView);

                }

            } catch(org.json.JSONException ex) {
                ex.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Timer for selecting line currently sung.
     */
    private void videoTimer() {

        // array for checking if stop after certain line has already been made or not
        final ArrayList<Integer> textViewNumbersSeen = new ArrayList<>();
        textViewNumbersSeen.add(currentTextViewNumber);

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

                    // pause video if question hasn't been answered
                    if ((textViewNumbersSeen.contains(questionLineNumber) && currentTextViewNumber == questionLineNumber+1) ||
                                ((! textViewNumbersSeen.contains(questionLineNumber) || ! textViewNumbersSeen.contains(questionLineNumber+1)) && currentTextViewNumber == questionLineNumber+2)) {

                            myYouTubePlayer.pause();

                    }

                    // increment number for changing background of TextViews and number for focusing ScrollView on TextViews
                    if (currentLineNumber < times.size()-1) {

                        currentLineNumber++;
                        currentTextViewNumber++;

                        if (((TextView) findViewById(fullIds.get(currentTextViewNumber+1))).getText().toString().equals("")) {

                            currentTextViewNumber++;
                        }

                        // add current number of textview to array of text view numbers
                        textViewNumbersSeen.add(currentTextViewNumber);

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
            } else {
                // get line number of first line with blank, so video can play further
                questionNumber = 0;
                try {
                    JSONObject curDict = constructions.getJSONObject(questionNumber);
                    questionLineNumber = curDict.getInt("line");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highlight);

        // layout for all elements lower than video
        LinearLayout bigLayout = findViewById(R.id.linearLayout);

        // create resizable scroll view
        scrollView = new ScrollViewWithMaxHeight(getApplicationContext());
//        scrollView.setMaxHeight(getResources().getDisplayMetrics().heightPixels/2);
        scrollView.setMaxHeight(800);

        intent = getIntent();

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
                button.setText(optionsArray.get(i));
                button.setLayoutParams(param);
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
        startActivity(intent);

    }
}
