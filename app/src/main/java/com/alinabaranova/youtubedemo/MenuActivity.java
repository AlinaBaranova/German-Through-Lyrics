package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MenuActivity extends AppCompatActivity {

    ListView highlightOptions;
    ListView gameOptions;
    boolean highlightOptionsAreShown;
    boolean gameOptionsAreShown;

    public void toKaraokeActivity(View view) {

        String videoId = "qbjaVTKEdG0";
        String textFilename = "adam-angst_splitter-von-granaten.txt";

        Intent intent = new Intent(getApplicationContext(), KaraokeActivity.class);

        intent.putExtra("videoId", videoId);
        intent.putExtra("textFilename", textFilename);

        startActivity(intent);

    }

    public void openOptions(View view) {

        Button buttonClicked = (Button) view;

        if (buttonClicked.getId() == R.id.highlightButton) {
            if (highlightOptionsAreShown) {
                highlightOptions.setVisibility(View.GONE);
                highlightOptionsAreShown = false;
            } else {
                highlightOptions.setVisibility(View.VISIBLE);
                highlightOptionsAreShown = true;
            }

        } else if (buttonClicked.getId() == R.id.gameButton){
            if (gameOptionsAreShown) {
                gameOptions.setVisibility(View.GONE);
                gameOptionsAreShown = false;
            } else {
                gameOptions.setVisibility(View.VISIBLE);
                gameOptionsAreShown = true;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final String[] optionsList = {"Prepositions", "Verb prefixes", "Verb forms", "Passive"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),
                android.R.layout.simple_list_item_1, optionsList);

        gameOptionsAreShown = false;
        gameOptions = findViewById(R.id.gameOptions);
        gameOptions.setAdapter(arrayAdapter);

        gameOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);

                String videoId = "qbjaVTKEdG0";
                String songName = "adam-angst_splitter-von-granaten";
                String textFilename = songName + ".txt";

                String jsonFilename;
                String color;
                if (selectedItem.equals("Prepositions")) {
                    jsonFilename = songName + "_prep.json";
                    color = "#FFFF00";
                } else if (selectedItem.equals("Verb prefixes")) {
                    jsonFilename = songName + "_pref.json";
                    color = "#00FFFF";
                } else if (selectedItem.equals("Verb forms")) {
                    jsonFilename = songName + "_conj.json";
                    color = "#FF00FF";
                } else {
                    jsonFilename = songName + "_passive.json";
                    color = "#FFFFFF";
                }

                Intent intent = new Intent(getApplicationContext(), GameActivity.class);

                intent.putExtra("videoId", videoId);
                intent.putExtra("textFilename", textFilename);
                intent.putExtra("jsonFilename", jsonFilename);
                intent.putExtra("color", color);

                startActivity(intent);

            }
        });

        highlightOptionsAreShown = false;
        highlightOptions = findViewById(R.id.highlightOptions);
        highlightOptions.setAdapter(arrayAdapter);

        highlightOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);

                String videoId = "qbjaVTKEdG0";
                String songName = "adam-angst_splitter-von-granaten";
                String textFilename = songName + ".txt";

                String jsonFilename;
                String color;
                if (selectedItem.equals("Prepositions")) {
                    jsonFilename = songName + "_prep.json";
                    color = "#FFFF00";
                } else if (selectedItem.equals("Verb prefixes")) {
                    jsonFilename = songName + "_pref.json";
                    color = "#00FFFF";
                } else if (selectedItem.equals("Verb forms")) {
                    jsonFilename = songName + "_conj.json";
                    color = "#FF00FF";
                } else {
                    jsonFilename = songName + "_passive.json";
                    color = "#FFFFFF";
                }

                Intent intent = new Intent(getApplicationContext(), HighlightActivity.class);

                intent.putExtra("videoId", videoId);
                intent.putExtra("textFilename", textFilename);
                intent.putExtra("jsonFilename", jsonFilename);
                intent.putExtra("color", color);

                startActivity(intent);

            }
        });

    }
}
