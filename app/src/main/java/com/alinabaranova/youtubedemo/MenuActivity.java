package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    ListView highlightOptions;
    ListView gameOptions;
    boolean highlightOptionsAreShown;
    boolean gameOptionsAreShown;

    int songId;

    public void toKaraokeActivity(View view) {

        Intent intent = new Intent(getApplicationContext(), KaraokeActivity.class);
        intent.putExtra("songId", songId);

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

        Intent intent = getIntent();
        songId = intent.getIntExtra("songId", -1);

        Map<String, String> topics = new HashMap<>();
        topics.put("prep", "Prepositions");
        topics.put("pref", "Verb prefixes");
        topics.put("conj", "Verb forms");
        topics.put("passive", "Passive");

        // load database
        getApplicationContext().deleteDatabase("app.db");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        ArrayList<String> optionsList = new ArrayList<>(); // arraylist for topics that the song has constructions for
        // check for which topics song has constructions
        Cursor c  = database.rawQuery("SELECT * from constructions WHERE song_id=" + songId, null);

        if (c.getCount() > 0) {
            int constrTypeIndex = c.getColumnIndex("constr_type");

            while (c.moveToNext()) {
                String constrType = c.getString(constrTypeIndex);
                optionsList.add(topics.get(constrType));
            }
        }
        c.close();

        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),
                android.R.layout.simple_list_item_1, optionsList);

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
                        color = "#FFFF00";
                        break;

                    case "Verb prefixes":
                        constrType = "pref";
                        color = "#00FFFF";
                        break;

                    case "Verb forms":
                        constrType = "conj";
                        color = "#FF00FF";
                        break;

                    default:
                        constrType = "passive";
                        color = "#FFFFFF";
                }

                Intent newIntent = new Intent(getApplicationContext(), GameActivity.class);

                newIntent.putExtra("constrType", constrType);
                newIntent.putExtra("color", color);
                newIntent.putExtra("songId", songId);

                startActivity(newIntent);

            }
        });

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
                        color = "#FFFF00";
                        break;

                    case "Verb prefixes":
                        constrType = "pref";
                        color = "#00FFFF";
                        break;

                    case "Verb forms":
                        constrType = "conj";
                        color = "#FF00FF";
                        break;

                    default:
                        constrType = "passive";
                        color = "#FFFFFF";
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

        // start activity StartActivity
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);

    }

}
