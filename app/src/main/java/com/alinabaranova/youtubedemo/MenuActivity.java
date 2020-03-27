package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MenuActivity extends AppCompatActivity {

    ListView highlightOptions;      // listview with grammatical topics for highlight mode
    ListView gameOptions;           // listview with grammatical topics for game mode

    boolean highlightOptionsAreShown;       // true if listview for highlight mode is shown
    boolean gameOptionsAreShown;            // true if listview for game mode is shown

    Intent intent;
    int songId;

    public void toKaraokeActivity(View view) {

        Intent intent = new Intent(getApplicationContext(), KaraokeActivity.class);
        intent.putExtra("songId", songId);

        startActivity(intent);

    }

    public void openOptions(View view) {

        Button buttonClicked = (Button) view;

        // if button "Highlight mode" is clicked
        if (buttonClicked.getId() == R.id.highlightButton) {
            // hide highlight options if they are shown
            if (highlightOptionsAreShown) {
                highlightOptions.setVisibility(View.GONE);
                highlightOptionsAreShown = false;
            } else {
                // show highlight options if they are hidden; hide game options if they are shown
                highlightOptions.setVisibility(View.VISIBLE);
                highlightOptionsAreShown = true;
                if (gameOptionsAreShown) {
                    gameOptions.setVisibility(View.GONE);
                    gameOptionsAreShown = false;
                }
            }

        } else if (buttonClicked.getId() == R.id.gameButton){
            // if button "Game mode" is clicked
            // hide highlight options if they are shown
            if (gameOptionsAreShown) {
                gameOptions.setVisibility(View.GONE);
                gameOptionsAreShown = false;
            } else {
                // show highlight options if they are hidden; hide highlight options if they are shown
                gameOptions.setVisibility(View.VISIBLE);
                gameOptionsAreShown = true;
                if (highlightOptionsAreShown) {
                    highlightOptions.setVisibility(View.GONE);
                    highlightOptionsAreShown = false;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // hide bar with app name
        Objects.requireNonNull(getSupportActionBar()).hide();

        intent = getIntent();
        songId = intent.getIntExtra("songId", -1);

        Map<String, String> topics = new HashMap<>();
        topics.put("prep", "Prepositions");
        topics.put("pref", "Verb prefixes");
        topics.put("conj", "Verb forms");
        topics.put("passive", "Passive");

        // load database
//        getApplicationContext().deleteDatabase("app.db");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // load image
        ImageView imageView = findViewById(R.id.imageView);

        Cursor c = database.rawQuery("SELECT album_cover FROM songs WHERE song_id=" + songId, null);
        if (c.moveToFirst()) {
            byte[] imgByte = c.getBlob(0);
            Bitmap image = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            imageView.setImageBitmap(image);
        }

        // check for which topics song has constructions
        ArrayList<String> optionsList = new ArrayList<>();  // arraylist for topics that the song has constructions for
        c  = database.rawQuery("SELECT constr_type from constructions WHERE song_id=" + songId, null);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String constrType = c.getString(0);
                optionsList.add(topics.get(constrType));
            }
        }

        // get artist name and song name
        TextView textViewArtist = findViewById(R.id.textViewArtist);
        TextView textViewSong = findViewById(R.id.textViewSong);

        c = database.rawQuery("SELECT song_name, artist FROM songs WHERE song_id=" + songId, null);
        if (c.moveToFirst()) {
            textViewArtist.setText(c.getString(1));
            textViewSong.setText(c.getString(0));
        }
        c.close();

        // create array adapter with topics that the song has constructions for
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),
                android.R.layout.simple_list_item_1, optionsList);

        // find listview for game mode, set its boolean to false, set adapter and clicklistener
        gameOptionsAreShown = false;
        gameOptions = findViewById(R.id.gameOptions);
        gameOptions.setAdapter(arrayAdapter);

        gameOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);

                String constrType;
                String color;
                switch (selectedItem) {
                    case "Prepositions":
                        constrType = "prep";
                        color = "#F9E79F";
                        break;

                    case "Verb prefixes":
                        constrType = "pref";
                        color = "#A2D9CE";
                        break;

                    case "Verb forms":
                        constrType = "conj";
                        color = "#D2B4DE";
                        break;

                    default:
                        constrType = "passive";
                        color = "#F5B7B1";
                }

                Intent newIntent = new Intent(getApplicationContext(), GameActivity.class);

                newIntent.putExtra("constrType", constrType);
                newIntent.putExtra("color", color);
                newIntent.putExtra("songId", songId);

                startActivity(newIntent);

            }
        });

        // find listview for highlight mode, set its boolean to false, set adapter and clicklistener
        highlightOptionsAreShown = false;
        highlightOptions = findViewById(R.id.highlightOptions);
        highlightOptions.setAdapter(arrayAdapter);

        highlightOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);

                String constrType;
                String color;
                switch (selectedItem) {
                    case "Prepositions":
                        constrType = "prep";
                        color = "#F9E79F";
                        break;

                    case "Verb prefixes":
                        constrType = "pref";
                        color = "#A2D9CE";
                        break;

                    case "Verb forms":
                        constrType = "conj";
                        color = "#D2B4DE";
                        break;

                    default:
                        constrType = "passive";
                        color = "#F5B7B1";
                }

                Intent newIntent = new Intent(getApplicationContext(), HighlightActivity.class);

                newIntent.putExtra("constrType", constrType);
                newIntent.putExtra("color", color);
                newIntent.putExtra("songId", songId);

                startActivity(newIntent);

            }
        });

    }

    @Override
    public void onBackPressed() {

        Intent newIntent;

//        String prevAct = intent.getStringExtra("prevAct");      // name of previous activity
//
//        Log.i("Previous activity", prevAct);
//
//        // previous activity is filtered activity
//        if (prevAct.equals("FilteredActivity")) {
//
//            newIntent = new Intent(getApplicationContext(), FilteredActivity.class);
//            // get name of filter used in filtered activity
//            String sortType = intent.getStringExtra("sortType");
//            newIntent.putExtra("sortType", sortType);
//
//            // depending on filter, put values to intent
//            if (sortType.equals("constr")) {
//                newIntent.putExtra("constrType", intent.getStringExtra("constrType"));
//            } else {
//                newIntent.putExtra("genre", intent.getStringExtra("genre"));
//            }
//
//        } else {
//            // previous activity is start activity
//            newIntent = new Intent(getApplicationContext(), StartActivity.class);
//        }

        newIntent = new Intent(getApplicationContext(), StartActivity.class);

        startActivity(newIntent);

    }

}
