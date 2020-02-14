package com.alinabaranova.youtubedemo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TestActivity extends AppCompatActivity {

//    YouTubePlayerView mYouTubePlayerView;
//    YouTubePlayer.OnInitializedListener mOnInitializedListener;
//    YouTubePlayer.PlayerStateChangeListener mPlayerStateChangeListener;

    YouTubePlayerSupportFragment youTubePlayerFragment;
    YouTubePlayer myYouTubePlayer;

    ScrollViewWithMaxHeight scrollView;
    LinearLayout textLinearLayout;

    ArrayList<Integer> times = new ArrayList<>();
    ArrayList<String> lines = new ArrayList<>();

    ArrayList<Integer> lineIds = new ArrayList<>(); // ids for changing background of TextViews
    ArrayList<Integer> fullIds = new ArrayList<>(); // ids for focusing ScrollView on TextViews

    JSONArray constructions;    // array of constructions for song

    String color;   // color for highlighting

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
            String textFilename = "adam-angst_splitter-von-granaten.txt";
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
                color = "#00FFFF";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // layout for all elements lower than video
        LinearLayout bigLayout = findViewById(R.id.linearLayout);

        // create resizable scroll view
        scrollView = new ScrollViewWithMaxHeight(getApplicationContext());
        scrollView.setMaxHeight(800);

        loadGameText();     // add text views to text linear layout
        scrollView.addView(textLinearLayout);   // add text linear layout to scroll view
        bigLayout.addView(scrollView);      // add scroll view to main linear layout

        youTubePlayerFragment = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_player_fragment);
        youTubePlayerFragment.initialize(YouTubeConfig.getApiKey(), new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

                myYouTubePlayer = youTubePlayer;
                youTubePlayer.loadVideo("xLetZ-36TYs");

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });
    }
}
